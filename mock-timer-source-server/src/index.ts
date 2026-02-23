import { loadRacers, loadTracks, pickRandomTrack } from "./csvLoader";
import { LapStore } from "./store";
import { generateLap } from "./lapGenerator";
import { startServer } from "./server";
import type { ServerContext } from "./types";

function randomInt(min: number, max: number): number {
  return Math.floor(Math.random() * (max - min + 1)) + min;
}

function randomFloat(min: number, max: number, decimals: number): number {
  const value = Math.random() * (max - min) + min;
  return parseFloat(value.toFixed(decimals));
}

function buildRaceId(): number {
  const now = new Date();
  const year = now.getUTCFullYear();
  const month = String(now.getUTCMonth() + 1).padStart(2, "0");
  const day = String(now.getUTCDate()).padStart(2, "0");
  return parseInt(`${year}${month}${day}`, 10);
}

function buildRaceDate(): string {
  const now = new Date();
  return new Date(Date.UTC(now.getUTCFullYear(), now.getUTCMonth(), now.getUTCDate())).toISOString();
}

function main(): void {
  const racers = loadRacers();
  const tracks = loadTracks();
  const track = pickRandomTrack(tracks);

  console.log(`Track: ${track.name} (${track.durationFrom}ms – ${track.durationTo}ms)`);
  console.log(`Racers: ${racers.length}`);

  const store = new LapStore();

  const context: ServerContext = {
    racers,
    track,
    raceId: buildRaceId(),
    raceDate: buildRaceDate(),
    temp1: randomInt(10, 30),
    temp2: randomFloat(10, 30, 2),
    temp3: randomInt(70, 110),
  };

  console.log(`Race ID: ${context.raceId}`);
  console.log(`Temperatures: ${context.temp1}°C / ${context.temp2}°C / ${context.temp3}%`);

  const { httpServer } = startServer(store);

  const interval = setInterval(() => {
    const lapTime = new Date();
    const racer = context.racers[Math.floor(Math.random() * context.racers.length)]!;
    const lap = generateLap(racer, context, store, lapTime);
    store.addLaps([lap]);
    console.log(`Generated lap for ${racer.firstName} ${racer.lastName} at ${lapTime.toISOString()} (total: ${store.getAllLaps().length})`);
  }, 10_000);

  function shutdown(): void {
    console.log("\nShutting down...");
    clearInterval(interval);
    httpServer.close(() => {
      console.log("Server closed.");
      process.exit(0);
    });
  }

  process.on("SIGINT", shutdown);
  process.on("SIGTERM", shutdown);
}

main();
