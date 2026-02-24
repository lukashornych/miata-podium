package dev.lukashornych.miatapodium.server.datafetcher.config;

import org.jspecify.annotations.NullMarked;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties for the lap data fetcher.
 * Bound from the {@code miatapodium.datafetcher} prefix in application properties.
 *
 * @param wsUrl          the WebSocket URL of the upstream lap data API
 * @param pollIntervalMs the interval in milliseconds between successive data polls
 */
@NullMarked
@ConfigurationProperties(prefix = "miatapodium.datafetcher")
public record DataFetcherProperties(
    String wsUrl,
    long pollIntervalMs
) {
}
