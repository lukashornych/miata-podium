package dev.lukashornych.miatapodium.server.datafetcher.service;

import com.github.javafaker.Faker;
import dev.lukashornych.miatapodium.server.datafetcher.client.LapDataWebSocketClient;
import dev.lukashornych.miatapodium.server.datafetcher.model.LapDataPayload;
import dev.lukashornych.miatapodium.server.lap.model.Lap;
import dev.lukashornych.miatapodium.server.lap.repository.LapRepository;
import dev.lukashornych.miatapodium.server.race.model.Race;
import dev.lukashornych.miatapodium.server.race.repository.RaceRepository;
import dev.lukashornych.miatapodium.server.racer.model.Racer;
import dev.lukashornych.miatapodium.server.racer.repository.RacerRepository;
import org.jspecify.annotations.NullMarked;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

/**
 * Integration test for {@link LapDataFetcherService} that verifies the full data ingestion
 * pipeline from lap data payloads to database persistence. Uses Testcontainers for a real
 * PostgreSQL instance and Mockito to replace the WebSocket client.
 *
 * <p>Verifies that:
 * <ul>
 *   <li>Race and racer records are created from incoming lap data without duplicates</li>
 *   <li>All unique laps are persisted with correct field values</li>
 *   <li>Duplicate data is properly ignored on subsequent polls</li>
 * </ul>
 */
@NullMarked
@SpringBootTest(properties = {
    "miatapodium.datafetcher.poll-interval-ms=999999999"
})
@Testcontainers
class LapDataFetcherServiceIntegrationTest {

    @Container
    @ServiceConnection
    static final PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:16")
        .withUsername("miatapodium")
        .withPassword("miatapodium")
        .withDatabaseName("miatapodium");

    @MockitoBean
    private LapDataWebSocketClient webSocketClient;

    @Autowired
    private LapDataFetcherService lapDataFetcherService;

    @Autowired
    private RaceRepository raceRepository;

    @Autowired
    private RacerRepository racerRepository;

    @Autowired
    private LapRepository lapRepository;

    @Test
    void pollAndPersist_shouldCorrectlyPersistLapData() throws Exception {
        // Generate test data
        List<LapDataPayload> payloads = generateTestLapData();
        when(webSocketClient.fetchLapData()).thenReturn(payloads);

        // First poll - should persist all data
        lapDataFetcherService.pollAndPersist();

        // Assert correct counts
        List<Race> races = new ArrayList<>();
        raceRepository.findAll().forEach(races::add);
        assertEquals(2, races.size(), "Should have exactly 2 races");

        List<Racer> racers = new ArrayList<>();
        racerRepository.findAll().forEach(racers::add);
        assertEquals(5, racers.size(), "Should have exactly 5 racers");

        List<Lap> laps = new ArrayList<>();
        lapRepository.findAll().forEach(laps::add);
        assertEquals(20, laps.size(), "Should have exactly 20 laps");

        // Assert race field values
        Optional<Race> race1 = raceRepository.findByExternalId(1);
        assertTrue(race1.isPresent(), "Race with externalId 1 should exist");
        assertEquals("Brno Circuit", race1.get().name());
        assertEquals(LocalDate.of(2025, 9, 13), race1.get().date());

        Optional<Race> race2 = raceRepository.findByExternalId(2);
        assertTrue(race2.isPresent(), "Race with externalId 2 should exist");
        assertEquals("Most Circuit", race2.get().name());
        assertEquals(LocalDate.of(2025, 9, 20), race2.get().date());

        // Assert racer field values
        Faker faker = new Faker(new Random(42));
        for (int i = 0; i < 5; i++) {
            String expectedFirstName = faker.name().firstName();
            String expectedLastName = faker.name().lastName();
            int rfidId = 101 + i;
            int expectedCarNumber = 50 + i;

            Optional<Racer> racer = racerRepository.findByExternalId(rfidId);
            assertTrue(racer.isPresent(), "Racer with externalId " + rfidId + " should exist");
            assertEquals(expectedCarNumber, racer.get().carNumber());
            assertEquals(expectedFirstName, racer.get().firstName());
            assertEquals(expectedLastName, racer.get().lastName());
        }

        // Assert lap field values (spot-check first lap)
        LapDataPayload firstPayload = payloads.get(0);
        Optional<Race> firstLapRace = raceRepository.findByExternalId(firstPayload.raceId());
        assertTrue(firstLapRace.isPresent());
        Long firstLapRaceId = firstLapRace.get().id();
        assertNotNull(firstLapRaceId);

        Lap firstLap = null;
        for (Lap lap : laps) {
            if (lap.externalId() == firstPayload.id() && lap.raceId().getId().equals(firstLapRaceId)) {
                firstLap = lap;
                break;
            }
        }
        assertNotNull(firstLap, "First lap should be found in database");
        assertEquals(firstPayload.lapTime(), firstLap.lapTimeMs());
        assertEquals(firstPayload.make(), firstLap.make());
        assertEquals(firstPayload.model(), firstLap.model());
        assertEquals(firstPayload.category(), firstLap.category());
        assertEquals(firstPayload.tag(), firstLap.tag());
        assertEquals(firstPayload.round(), firstLap.round());
        assertEquals(firstPayload.carNumber(), firstLap.carNumber());
        assertFalse(firstLap.isRaceLap());

        assertNotNull(firstLap.sectorTimes());
        assertEquals(firstPayload.s1(), firstLap.sectorTimes().s1Ms());
        assertEquals(firstPayload.s2(), firstLap.sectorTimes().s2Ms());
        assertEquals(firstPayload.s3(), firstLap.sectorTimes().s3Ms());
        assertEquals(firstPayload.timeS1(), firstLap.sectorTimes().timeS1());
        assertEquals(firstPayload.timeS2(), firstLap.sectorTimes().timeS2());
        assertEquals(firstPayload.timeS3(), firstLap.sectorTimes().timeS3());

        assertNotNull(firstLap.trackConditions());
        assertEquals(firstPayload.temp1(), firstLap.trackConditions().airTemp());
        assertEquals(firstPayload.temp2(), firstLap.trackConditions().trackTemp());
        assertEquals(firstPayload.temp3(), firstLap.trackConditions().humidity());

        // Second poll with same data - should not create duplicates
        lapDataFetcherService.pollAndPersist();

        List<Race> racesAfterSecondPoll = new ArrayList<>();
        raceRepository.findAll().forEach(racesAfterSecondPoll::add);
        assertEquals(2, racesAfterSecondPoll.size(), "Race count should remain 2 after second poll");

        List<Racer> racersAfterSecondPoll = new ArrayList<>();
        racerRepository.findAll().forEach(racersAfterSecondPoll::add);
        assertEquals(5, racersAfterSecondPoll.size(), "Racer count should remain 5 after second poll");

        List<Lap> lapsAfterSecondPoll = new ArrayList<>();
        lapRepository.findAll().forEach(lapsAfterSecondPoll::add);
        assertEquals(20, lapsAfterSecondPoll.size(), "Lap count should remain 20 after second poll");
    }

    private List<LapDataPayload> generateTestLapData() {
        Faker faker = new Faker(new Random(42));

        String[] raceNames = {"Brno Circuit", "Most Circuit"};
        String[] raceDates = {"2025-09-13T00:00:00.000Z", "2025-09-20T00:00:00.000Z"};
        String[] categories = {"Open", "Ultimate", "Racing", "Touring"};
        String[] makes = {"Mazda", "Hyundai", "BMW", "Toyota", "Honda"};
        String[] models = {"MX-5", "i30N", "M3", "GR86", "Civic Type R"};

        String[] firstNames = new String[5];
        String[] lastNames = new String[5];
        for (int i = 0; i < 5; i++) {
            firstNames[i] = faker.name().firstName();
            lastNames[i] = faker.name().lastName();
        }

        List<LapDataPayload> payloads = new ArrayList<>();
        int lapId = 1;

        for (int raceIdx = 0; raceIdx < 2; raceIdx++) {
            int raceExternalId = raceIdx + 1;
            Instant raceDate = Instant.parse(raceDates[raceIdx]);

            for (int racerIdx = 0; racerIdx < 5; racerIdx++) {
                int rfidId = 101 + racerIdx;
                int carNumber = 50 + racerIdx;

                for (int lapNum = 0; lapNum < 2; lapNum++) {
                    long baseTimeMs = 1694610000000L + (raceIdx * 604800000L) + (racerIdx * 300000L) + (lapNum * 90000L);
                    Instant timePrev = Instant.ofEpochMilli(baseTimeMs);
                    int s1 = 18000 + faker.number().numberBetween(0, 3000);
                    int s2 = 25000 + faker.number().numberBetween(0, 3000);
                    int s3 = 24000 + faker.number().numberBetween(0, 3000);
                    int lapTimeMs = s1 + s2 + s3;

                    Instant timeS1 = Instant.ofEpochMilli(baseTimeMs + s1);
                    Instant timeS2 = Instant.ofEpochMilli(baseTimeMs + s1 + s2);
                    Instant timeFinish = Instant.ofEpochMilli(baseTimeMs + lapTimeMs);

                    float airTemp = 15.0f + faker.number().numberBetween(0, 5);
                    float trackTemp = 16.0f + faker.number().numberBetween(0, 10);
                    float humidity = 85.0f + faker.number().numberBetween(0, 10);

                    String tag = "E55202107040000000000" + String.format("%03d", carNumber);

                    LapDataPayload payload = new LapDataPayload(
                        lapId,
                        raceExternalId,
                        rfidId,
                        timeFinish,
                        timePrev,
                        tag,
                        lapTimeMs,
                        timeS1,
                        timeS2,
                        timeFinish,
                        s1,
                        s2,
                        s3,
                        airTemp,
                        trackTemp,
                        humidity,
                        lapNum,
                        carNumber,
                        categories[racerIdx % categories.length],
                        makes[racerIdx],
                        models[racerIdx],
                        firstNames[racerIdx],
                        lastNames[racerIdx],
                        raceNames[raceIdx],
                        raceDate,
                        0
                    );

                    payloads.add(payload);
                    lapId++;
                }
            }
        }

        return payloads;
    }
}
