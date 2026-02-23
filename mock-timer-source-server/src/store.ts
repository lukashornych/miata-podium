import type { LapTime } from "./types";

export class LapStore {
  private laps: LapTime[] = [];
  private nextId: number = 1;
  private roundCounters: Map<number, number> = new Map();

  addLaps(laps: LapTime[]): void {
    this.laps.push(...laps);
  }

  getAllLaps(): LapTime[] {
    return [...this.laps];
  }

  nextLapId(): number {
    return this.nextId++;
  }

  nextRound(rfid: number): number {
    const current = this.roundCounters.get(rfid) ?? 0;
    this.roundCounters.set(rfid, current + 1);
    return current;
  }
}
