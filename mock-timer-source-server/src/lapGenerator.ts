import type { LapTime, Racer, ServerContext } from "./types";
import type { LapStore } from "./store";

function randomInt(min: number, max: number): number {
  return Math.floor(Math.random() * (max - min + 1)) + min;
}

function splitIntoThree(total: number): [number, number, number] {
  const s1 = Math.floor(total * (0.20 + Math.random() * 0.20));
  const s2 = Math.floor((total - s1) * (0.25 + Math.random() * 0.20));
  const s3 = total - s1 - s2;
  return [s1, s2, s3];
}

export function generateLap(racer: Racer, context: ServerContext, store: LapStore, lapEndTime: Date): LapTime {
  const { track, raceId, raceDate, temp1, temp2, temp3 } = context;
  const lapTime = randomInt(track.durationFrom, track.durationTo);
  const lapStartMs = lapEndTime.getTime() - lapTime;
  const [s1, s2, s3] = splitIntoThree(lapTime);
  const timeS1 = new Date(lapStartMs + s1).toISOString();
  const timeS2 = new Date(lapStartMs + s1 + s2).toISOString();
  const timeS3 = lapEndTime.toISOString();
  const timePrev = new Date(lapStartMs).toISOString();

  return {
    Id: store.nextLapId(),
    RaceId: raceId,
    RFIDId: racer.rfid,
    Time: timeS3,
    TimePrev: timePrev,
    Tag: racer.tag,
    LapTime: lapTime,
    TimeS1: timeS1,
    TimeS2: timeS2,
    TimeS3: timeS3,
    S1: s1,
    S2: s2,
    S3: s3,
    Temp1: temp1,
    Temp2: temp2,
    Temp3: temp3,
    Round: store.nextRound(racer.rfid),
    CarNumber: racer.rfid - 100,
    Category: racer.category,
    Make: racer.make,
    Model: racer.model,
    Tires: null,
    FirstName: racer.firstName,
    LastName: racer.lastName,
    Name: track.name,
    Date: raceDate,
    IsRaceLap: 0,
  };
}

export function generateLapBatch(context: ServerContext, store: LapStore, batchTime: Date): LapTime[] {
  const laps = context.racers.map((racer) => generateLap(racer, context, store, batchTime));
  store.addLaps(laps);
  return laps;
}
