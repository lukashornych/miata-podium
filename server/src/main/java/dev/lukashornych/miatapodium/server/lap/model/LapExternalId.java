package dev.lukashornych.miatapodium.server.lap.model;

import org.jspecify.annotations.NullMarked;

/**
 * Value object representing the external identifier of a lap from the upstream lap data API.
 * Corresponds to the {@code Id} field in the API payload. This identifier is unique only
 * within the scope of a single race.
 */
@NullMarked
public record LapExternalId(int value) {
}
