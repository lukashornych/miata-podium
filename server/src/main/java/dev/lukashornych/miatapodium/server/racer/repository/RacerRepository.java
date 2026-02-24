package dev.lukashornych.miatapodium.server.racer.repository;

import dev.lukashornych.miatapodium.server.racer.model.Racer;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

/**
 * Repository for persisting and querying {@link Racer} aggregates.
 */
public interface RacerRepository extends CrudRepository<Racer, Long> {

    /**
     * Finds a racer by their external identifier (RFID ID) from the upstream API.
     *
     * @param externalId the external racer identifier
     * @return the racer if found
     */
    Optional<Racer> findByExternalId(int externalId);
}
