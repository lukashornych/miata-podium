package dev.lukashornych.miatapodium.server.datafetcher.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.lukashornych.miatapodium.server.datafetcher.client.LapDataWebSocketClient;
import dev.lukashornych.miatapodium.server.datafetcher.service.LapDataFetcherService;
import dev.lukashornych.miatapodium.server.lap.repository.LapRepository;
import dev.lukashornych.miatapodium.server.race.repository.RaceRepository;
import dev.lukashornych.miatapodium.server.racer.repository.RacerRepository;
import org.jspecify.annotations.NullMarked;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Spring configuration for the data fetcher module. Enables scheduling for periodic
 * polling and declares the WebSocket client and fetcher service beans.
 */
@NullMarked
@Configuration
@EnableScheduling
@EnableConfigurationProperties(DataFetcherProperties.class)
public class DataFetcherConfiguration {

    @Bean
    LapDataWebSocketClient lapDataWebSocketClient(DataFetcherProperties properties, ObjectMapper objectMapper) {
        return new LapDataWebSocketClient(properties.wsUrl(), objectMapper);
    }

    @Bean
    LapDataFetcherService lapDataFetcherService(
        LapDataWebSocketClient webSocketClient,
        RaceRepository raceRepository,
        RacerRepository racerRepository,
        LapRepository lapRepository
    ) {
        return new LapDataFetcherService(webSocketClient, raceRepository, racerRepository, lapRepository);
    }
}
