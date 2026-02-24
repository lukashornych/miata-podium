package dev.lukashornych.miatapodium.server.race.model;

import org.jspecify.annotations.NullMarked;

/**
 * Value object representing the external identifier of a race from the upstream lap data API.
 * Corresponds to the {@code RaceId} field in the API payload.
 */
@NullMarked
public record RaceExternalId(int value) {
}
