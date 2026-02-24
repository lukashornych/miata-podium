package dev.lukashornych.miatapodium.server.racer.model;

import org.jspecify.annotations.NullMarked;

/**
 * Value object representing the external identifier of a racer from the upstream lap data API.
 * Corresponds to the {@code RFIDId} field in the API payload (the racer's RFID card identifier).
 */
@NullMarked
public record RacerExternalId(int value) {
}
