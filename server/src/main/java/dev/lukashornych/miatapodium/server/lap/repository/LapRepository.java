package dev.lukashornych.miatapodium.server.lap.repository;

import dev.lukashornych.miatapodium.server.lap.model.Lap;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

/**
 * Repository for persisting and querying {@link Lap} aggregates.
 */
public interface LapRepository extends CrudRepository<Lap, Long> {

    /**
     * Checks whether a lap with the given external ID already exists for the specified race.
     * Used to enforce uniqueness of laps based on the upstream API's identifier within a race.
     *
     * @param externalId the external lap identifier from the API
     * @param raceId     the surrogate ID of the race
     * @return {@code true} if such a lap already exists
     */
    @Query("SELECT EXISTS(SELECT 1 FROM laps WHERE external_id = :externalId AND race_id = :raceId)")
    boolean existsByExternalIdAndRaceId(@Param("externalId") int externalId, @Param("raceId") long raceId);
}
