import http from "http";
import express from "express";
import { WebSocketServer, WebSocket } from "ws";
import type { LapStore } from "./store";
import type { WsClientMessage, WsSuccessResponse, WsErrorResponse } from "./types";

function isWsClientMessage(data: unknown): data is WsClientMessage {
  return (
    typeof data === "object" &&
    data !== null &&
    "type" in data &&
    (data as Record<string, unknown>)["type"] === "GET_DATA"
  );
}

export interface ServerHandle {
  wss: WebSocketServer;
  httpServer: http.Server;
}

export function startServer(store: LapStore): ServerHandle {
  const app = express();
  const httpServer = http.createServer(app);
  const port = process.env["PORT"] ?? "3010";

  const wss = new WebSocketServer({ server: httpServer, path: "/ws" });

  wss.on("connection", (ws: WebSocket) => {
    ws.on("message", (raw) => {
      let parsed: unknown;
      try {
        parsed = JSON.parse(raw.toString());
      } catch {
        const errorResponse: WsErrorResponse = { type: "ERROR", payload: "Invalid JSON" };
        ws.send(JSON.stringify(errorResponse));
        return;
      }

      if (!isWsClientMessage(parsed)) {
        const errorResponse: WsErrorResponse = { type: "ERROR", payload: "Unknown message type" };
        ws.send(JSON.stringify(errorResponse));
        return;
      }

      const response: WsSuccessResponse = {
        type: "SUCCESS",
        payload: store.getAllLaps(),
      };
      ws.send(JSON.stringify(response));
    });
  });

  httpServer.listen(parseInt(port, 10), () => {
    console.log(`Server listening on port ${port}`);
    console.log(`WebSocket endpoint: ws://localhost:${port}/ws`);
  });

  return { wss, httpServer };
}
