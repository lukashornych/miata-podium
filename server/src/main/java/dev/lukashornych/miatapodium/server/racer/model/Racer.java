package dev.lukashornych.miatapodium.server.racer.model;

import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

/**
 * Aggregate root representing a racer identified by their RFID card.
 * Each racer has a unique car number and is identified externally by the {@link RacerExternalId}.
 */
@NullMarked
@Table("racers")
public record Racer(
    @Id @Nullable Long id,
    int externalId,
    int carNumber,
    String firstName,
    String lastName
) {

    /**
     * Creates a new unpersisted racer from the given external data.
     *
     * @param externalId the external racer identifier (RFID ID) from the API
     * @param carNumber  the racer's car number
     * @param firstName  the racer's first name
     * @param lastName   the racer's last name
     * @return a new {@link Racer} instance with a {@code null} surrogate id
     */
    public static Racer create(RacerExternalId externalId, int carNumber, String firstName, String lastName) {
        return new Racer(null, externalId.value(), carNumber, firstName, lastName);
    }
}
