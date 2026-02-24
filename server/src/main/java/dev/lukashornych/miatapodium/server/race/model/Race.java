package dev.lukashornych.miatapodium.server.race.model;

import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDate;

/**
 * Aggregate root representing a race (a single trackday event at a racetrack).
 * Identified externally by the {@link RaceExternalId} from the upstream API.
 */
@NullMarked
@Table("races")
public record Race(
    @Id @Nullable Long id,
    int externalId,
    String name,
    LocalDate date
) {

    /**
     * Creates a new unpersisted race from the given external data.
     *
     * @param externalId the external race identifier from the API
     * @param name       the racetrack name
     * @param date       the trackday date
     * @return a new {@link Race} instance with a {@code null} surrogate id
     */
    public static Race create(RaceExternalId externalId, String name, LocalDate date) {
        return new Race(null, externalId.value(), name, date);
    }
}
