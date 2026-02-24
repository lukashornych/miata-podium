package dev.lukashornych.miatapodium.server.datafetcher.service;

import dev.lukashornych.miatapodium.server.datafetcher.client.LapDataWebSocketClient;
import dev.lukashornych.miatapodium.server.datafetcher.model.LapDataPayload;
import dev.lukashornych.miatapodium.server.lap.model.Lap;
import dev.lukashornych.miatapodium.server.lap.model.SectorTimes;
import dev.lukashornych.miatapodium.server.lap.model.TrackConditions;
import dev.lukashornych.miatapodium.server.lap.repository.LapRepository;
import dev.lukashornych.miatapodium.server.race.model.Race;
import dev.lukashornych.miatapodium.server.race.model.RaceExternalId;
import dev.lukashornych.miatapodium.server.race.repository.RaceRepository;
import dev.lukashornych.miatapodium.server.racer.model.Racer;
import dev.lukashornych.miatapodium.server.racer.model.RacerExternalId;
import dev.lukashornych.miatapodium.server.racer.repository.RacerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NullMarked;
import org.springframework.data.jdbc.core.mapping.AggregateReference;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZoneOffset;

/**
 * Service that periodically polls the upstream WebSocket API for lap data and persists
 * new, unique laps into the database. Ensures that referenced races and racers are created
 * if they do not yet exist. Uniqueness of laps is determined by the combination of
 * external lap ID and race ID.
 */
@NullMarked
@Slf4j
@RequiredArgsConstructor
public class LapDataFetcherService {

    private final LapDataWebSocketClient webSocketClient;
    private final RaceRepository raceRepository;
    private final RacerRepository racerRepository;
    private final LapRepository lapRepository;

    /**
     * Polls the upstream WebSocket API for accumulated lap data and persists any new laps.
     * Runs on a fixed delay configured via {@code miatapodium.datafetcher.poll-interval-ms}.
     */
    @Scheduled(fixedDelayString = "${miatapodium.datafetcher.poll-interval-ms}")
    @Transactional
    public void pollAndPersist() {
        try {
            log.debug("Polling lap data from WebSocket");
            final var laps = webSocketClient.fetchLapData();
            log.debug("Received {} lap records", laps.size());

            int savedCount = 0;
            for (final var payload : laps) {
                if (processLapPayload(payload)) {
                    savedCount++;
                }
            }

            if (savedCount > 0) {
                log.info("Saved {} new laps", savedCount);
            }
        } catch (Exception e) {
            log.error("Failed to poll and persist lap data", e);
        }
    }

    private boolean processLapPayload(LapDataPayload payload) {
        final var race = ensureRaceExists(payload);
        final var racer = ensureRacerExists(payload);

        final var raceId = race.id();
        final var racerId = racer.id();
        if (raceId == null || racerId == null) {
            log.error("Race or racer ID is null after persistence — this should not happen");
            return false;
        }

        if (lapRepository.existsByExternalIdAndRaceId(payload.id(), raceId)) {
            return false;
        }

        final var lap = new Lap(
            null,
            payload.id(),
            AggregateReference.to(raceId),
            AggregateReference.to(racerId),
            payload.time(),
            payload.timePrev(),
            payload.lapTime(),
            new SectorTimes(
                payload.s1(),
                payload.s2(),
                payload.s3(),
                payload.timeS1(),
                payload.timeS2(),
                payload.timeS3()
            ),
            new TrackConditions(
                payload.temp1(),
                payload.temp2(),
                payload.temp3()
            ),
            payload.round(),
            payload.carNumber(),
            payload.category(),
            payload.make(),
            payload.model(),
            payload.tag(),
            payload.isRaceLap() == 1
        );

        lapRepository.save(lap);
        log.debug("Saved new lap: externalId={}, raceExternalId={}", payload.id(), payload.raceId());
        return true;
    }

    private Race ensureRaceExists(LapDataPayload payload) {
        return raceRepository.findByExternalId(payload.raceId())
            .orElseGet(() -> {
                final var date = payload.date().atZone(ZoneOffset.UTC).toLocalDate();
                final var race = Race.create(
                    new RaceExternalId(payload.raceId()),
                    payload.name(),
                    date
                );
                log.info("Creating new race: externalId={}, name={}, date={}", payload.raceId(), payload.name(), date);
                return raceRepository.save(race);
            });
    }

    private Racer ensureRacerExists(LapDataPayload payload) {
        return racerRepository.findByExternalId(payload.rfidId())
            .orElseGet(() -> {
                final var racer = Racer.create(
                    new RacerExternalId(payload.rfidId()),
                    payload.carNumber(),
                    payload.firstName(),
                    payload.lastName()
                );
                log.info("Creating new racer: externalId={}, name={} {}", payload.rfidId(), payload.firstName(), payload.lastName());
                return racerRepository.save(racer);
            });
    }
}
