package dev.lukashornych.miatapodium.server.lap.model;

import dev.lukashornych.miatapodium.server.race.model.Race;
import dev.lukashornych.miatapodium.server.racer.model.Racer;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.springframework.data.annotation.Id;
import org.springframework.data.jdbc.core.mapping.AggregateReference;
import org.springframework.data.relational.core.mapping.Embedded;
import org.springframework.data.relational.core.mapping.Table;

import java.time.Instant;

/**
 * Aggregate root representing a single lap recorded at a racetrack.
 * References {@link Race} and {@link Racer} via {@link AggregateReference} to maintain
 * aggregate boundaries. Contains timing data, sector splits, track conditions, and
 * car/category metadata.
 */
@NullMarked
@Table("laps")
public record Lap(
    @Id @Nullable Long id,
    int externalId,
    AggregateReference<Race, Long> raceId,
    AggregateReference<Racer, Long> racerId,
    Instant time,
    Instant timePrev,
    int lapTimeMs,
    @Embedded.Nullable @Nullable SectorTimes sectorTimes,
    @Embedded.Nullable @Nullable TrackConditions trackConditions,
    int round,
    int carNumber,
    String category,
    String make,
    String model,
    String tag,
    boolean isRaceLap
) {
}
