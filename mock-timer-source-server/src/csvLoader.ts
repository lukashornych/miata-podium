import { readFileSync } from "fs";
import { join } from "path";
import { parse } from "csv-parse/sync";
import type { Racer, Track } from "./types";

function requireField(row: Record<string, string | undefined>, field: string, rowIndex: number): string {
  const value = row[field];
  if (value === undefined || value.trim() === "") {
    throw new Error(`Missing required field "${field}" at row ${rowIndex}`);
  }
  return value.trim();
}

export function loadRacers(): Racer[] {
  const filePath = join(process.cwd(), "racers.csv");
  const content = readFileSync(filePath, "utf-8");
  const rows = parse(content, {
    delimiter: ",",
    columns: true,
    skip_empty_lines: true,
    trim: true,
  }) as Record<string, string | undefined>[];

  return rows.map((row, index) => {
    const rfidStr = requireField(row, "rfid", index + 2);
    const rfid = parseInt(rfidStr, 10);
    if (isNaN(rfid)) {
      throw new Error(`Invalid rfid value "${rfidStr}" at row ${index + 2}`);
    }
    return {
      firstName: requireField(row, "firstName", index + 2),
      lastName: requireField(row, "lastName", index + 2),
      rfid,
      tag: requireField(row, "tag", index + 2),
      category: requireField(row, "category", index + 2),
      make: requireField(row, "make", index + 2),
      model: requireField(row, "model", index + 2),
    };
  });
}

export function loadTracks(): Track[] {
  const filePath = join(process.cwd(), "tracks.csv");
  const content = readFileSync(filePath, "utf-8");
  const rows = parse(content, {
    delimiter: ",",
    columns: true,
    skip_empty_lines: true,
    trim: true,
  }) as Record<string, string | undefined>[];

  return rows.map((row, index) => {
    const fromStr = requireField(row, "durationFrom", index + 2);
    const toStr = requireField(row, "durationTo", index + 2);
    const durationFrom = parseInt(fromStr, 10);
    const durationTo = parseInt(toStr, 10);
    if (isNaN(durationFrom)) {
      throw new Error(`Invalid durationFrom value "${fromStr}" at row ${index + 2}`);
    }
    if (isNaN(durationTo)) {
      throw new Error(`Invalid durationTo value "${toStr}" at row ${index + 2}`);
    }
    return {
      name: requireField(row, "name", index + 2),
      durationFrom,
      durationTo,
    };
  });
}

export function pickRandomTrack(tracks: Track[]): Track {
  if (tracks.length === 0) {
    throw new Error("No tracks available");
  }
  const index = Math.floor(Math.random() * tracks.length);
  const track = tracks[index];
  if (track === undefined) {
    throw new Error(`Track at index ${index} is undefined`);
  }
  return track;
}
