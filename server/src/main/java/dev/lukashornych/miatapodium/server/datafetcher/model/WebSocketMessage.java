package dev.lukashornych.miatapodium.server.datafetcher.model;

import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

/**
 * Generic envelope for messages exchanged over the upstream lap data WebSocket API.
 * Used for both requests (e.g., {@code GET_DATA}) and responses (e.g., {@code SUCCESS}).
 *
 * @param type    the message type identifier
 * @param payload the message payload, which varies by message type
 * @param <T>     the type of the payload
 */
@NullMarked
public record WebSocketMessage<T>(
    String type,
    @Nullable T payload
) {

    /**
     * Creates a {@code GET_DATA} request message to poll accumulated lap data from the API.
     *
     * @return a new request message with an empty string payload
     */
    public static WebSocketMessage<String> getDataRequest() {
        return new WebSocketMessage<>("GET_DATA", "");
    }
}
