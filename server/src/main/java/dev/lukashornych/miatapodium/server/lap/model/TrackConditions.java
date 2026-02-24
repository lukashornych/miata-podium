package dev.lukashornych.miatapodium.server.lap.model;

import org.jspecify.annotations.NullMarked;

/**
 * Embedded value object representing the environmental conditions during a lap.
 * Captures ambient air temperature, track surface temperature, and relative humidity.
 */
@NullMarked
public record TrackConditions(
    float airTemp,
    float trackTemp,
    float humidity
) {
}
