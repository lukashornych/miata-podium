package dev.lukashornych.miatapodium.server.datafetcher.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.jspecify.annotations.NullMarked;

import java.time.Instant;

/**
 * DTO representing a single lap record from the upstream WebSocket API.
 * Field names use {@link JsonProperty} to map from the API's PascalCase naming convention.
 * The {@code Tires} field from the API is intentionally omitted per specification.
 */
@NullMarked
@JsonIgnoreProperties(ignoreUnknown = true)
public record LapDataPayload(
    @JsonProperty("Id") int id,
    @JsonProperty("RaceId") int raceId,
    @JsonProperty("RFIDId") int rfidId,
    @JsonProperty("Time") Instant time,
    @JsonProperty("TimePrev") Instant timePrev,
    @JsonProperty("Tag") String tag,
    @JsonProperty("LapTime") int lapTime,
    @JsonProperty("TimeS1") Instant timeS1,
    @JsonProperty("TimeS2") Instant timeS2,
    @JsonProperty("TimeS3") Instant timeS3,
    @JsonProperty("S1") int s1,
    @JsonProperty("S2") int s2,
    @JsonProperty("S3") int s3,
    @JsonProperty("Temp1") float temp1,
    @JsonProperty("Temp2") float temp2,
    @JsonProperty("Temp3") float temp3,
    @JsonProperty("Round") int round,
    @JsonProperty("CarNumber") int carNumber,
    @JsonProperty("Category") String category,
    @JsonProperty("Make") String make,
    @JsonProperty("Model") String model,
    @JsonProperty("FirstName") String firstName,
    @JsonProperty("LastName") String lastName,
    @JsonProperty("Name") String name,
    @JsonProperty("Date") Instant date,
    @JsonProperty("IsRaceLap") int isRaceLap
) {
}
