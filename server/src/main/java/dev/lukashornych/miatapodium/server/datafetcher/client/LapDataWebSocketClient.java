package dev.lukashornych.miatapodium.server.datafetcher.client;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.lukashornych.miatapodium.server.datafetcher.model.LapDataPayload;
import dev.lukashornych.miatapodium.server.datafetcher.model.WebSocketMessage;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

/**
 * WebSocket client that connects to the upstream lap data API and polls for accumulated lap data.
 * Uses the JDK standard {@link java.net.http.HttpClient} WebSocket API.
 * Manages a persistent connection with automatic reconnection on failure.
 */
@NullMarked
public class LapDataWebSocketClient {

    private static final Logger log = LoggerFactory.getLogger(LapDataWebSocketClient.class);
    private static final long CONNECT_TIMEOUT_SECONDS = 10;
    private static final long RESPONSE_TIMEOUT_SECONDS = 30;

    private final String wsUrl;
    private final ObjectMapper objectMapper;
    private final AtomicReference<@Nullable CompletableFuture<String>> pendingResponse = new AtomicReference<>();
    private volatile @Nullable WebSocket webSocket;

    public LapDataWebSocketClient(String wsUrl, ObjectMapper objectMapper) {
        this.wsUrl = wsUrl;
        this.objectMapper = objectMapper;
    }

    /**
     * Sends a {@code GET_DATA} request to the upstream API and returns the list of lap data records.
     * Establishes a connection if one does not already exist.
     *
     * @return the list of lap data payloads received from the API
     * @throws Exception if the connection, request, or deserialization fails
     */
    public List<LapDataPayload> fetchLapData() throws Exception {
        final var ws = ensureConnected();
        final var future = new CompletableFuture<String>();
        pendingResponse.set(future);

        final var requestJson = objectMapper.writeValueAsString(WebSocketMessage.getDataRequest());
        ws.sendText(requestJson, true);

        final var responseJson = future.get(RESPONSE_TIMEOUT_SECONDS, TimeUnit.SECONDS);
        final var response = objectMapper.readValue(
            responseJson,
            new TypeReference<WebSocketMessage<List<LapDataPayload>>>() {}
        );

        final var payload = response.payload();
        return payload != null ? payload : List.of();
    }

    /**
     * Closes the WebSocket connection if one is open.
     */
    public void close() {
        final var ws = this.webSocket;
        if (ws != null) {
            ws.sendClose(WebSocket.NORMAL_CLOSURE, "shutdown");
            this.webSocket = null;
        }
    }

    private synchronized WebSocket ensureConnected() throws Exception {
        var ws = this.webSocket;
        if (ws != null) {
            return ws;
        }

        log.info("Connecting to WebSocket at {}", wsUrl);
        ws = HttpClient.newHttpClient()
            .newWebSocketBuilder()
            .buildAsync(URI.create(wsUrl), new ResponseListener())
            .get(CONNECT_TIMEOUT_SECONDS, TimeUnit.SECONDS);
        this.webSocket = ws;
        log.info("Connected to WebSocket at {}", wsUrl);
        return ws;
    }

    /**
     * Internal WebSocket listener that accumulates text message fragments and completes
     * the pending response future when a full message is received.
     */
    private class ResponseListener implements WebSocket.Listener {

        private final StringBuilder buffer = new StringBuilder();

        @Override
        public @Nullable CompletionStage<?> onText(WebSocket webSocket, CharSequence data, boolean last) {
            buffer.append(data);
            if (last) {
                final var future = pendingResponse.getAndSet(null);
                if (future != null) {
                    future.complete(buffer.toString());
                }
                buffer.setLength(0);
            }
            webSocket.request(1);
            return null;
        }

        @Override
        public @Nullable CompletionStage<?> onClose(WebSocket webSocket, int statusCode, String reason) {
            log.info("WebSocket closed: {} {}", statusCode, reason);
            LapDataWebSocketClient.this.webSocket = null;
            return null;
        }

        @Override
        public void onError(WebSocket webSocket, Throwable error) {
            log.error("WebSocket error", error);
            LapDataWebSocketClient.this.webSocket = null;
            final var future = pendingResponse.getAndSet(null);
            if (future != null) {
                future.completeExceptionally(error);
            }
        }
    }
}
