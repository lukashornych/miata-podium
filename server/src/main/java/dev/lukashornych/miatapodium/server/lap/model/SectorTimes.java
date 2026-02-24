package dev.lukashornych.miatapodium.server.lap.model;

import org.jspecify.annotations.NullMarked;

import java.time.Instant;

/**
 * Embedded value object representing the three sector times of a lap.
 * Each sector has both a duration in milliseconds and an absolute timestamp
 * marking when the car crossed the sector checkpoint.
 */
@NullMarked
public record SectorTimes(
    int s1Ms,
    int s2Ms,
    int s3Ms,
    Instant timeS1,
    Instant timeS2,
    Instant timeS3
) {
}
