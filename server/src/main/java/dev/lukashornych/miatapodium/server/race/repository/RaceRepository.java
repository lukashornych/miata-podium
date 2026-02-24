package dev.lukashornych.miatapodium.server.race.repository;

import dev.lukashornych.miatapodium.server.race.model.Race;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

/**
 * Repository for persisting and querying {@link Race} aggregates.
 */
public interface RaceRepository extends CrudRepository<Race, Long> {

    /**
     * Finds a race by its external identifier from the upstream API.
     *
     * @param externalId the external race identifier
     * @return the race if found
     */
    Optional<Race> findByExternalId(int externalId);
}
