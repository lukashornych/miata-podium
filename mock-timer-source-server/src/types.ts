export interface Racer {
  firstName: string;
  lastName: string;
  rfid: number;
  tag: string;
  category: string;
  make: string;
  model: string;
}

export interface Track {
  name: string;
  durationFrom: number;
  durationTo: number;
}

export interface LapTime {
  Id: number;
  RaceId: number;
  RFIDId: number;
  Time: string;
  TimePrev: string;
  Tag: string;
  LapTime: number;
  TimeS1: string;
  TimeS2: string;
  TimeS3: string;
  S1: number;
  S2: number;
  S3: number;
  Temp1: number;
  Temp2: number;
  Temp3: number;
  Round: number;
  CarNumber: number;
  Category: string;
  Make: string;
  Model: string;
  Tires: null;
  FirstName: string;
  LastName: string;
  Name: string;
  Date: string;
  IsRaceLap: number;
}

export interface ServerContext {
  racers: Racer[];
  track: Track;
  raceId: number;
  raceDate: string;
  temp1: number;
  temp2: number;
  temp3: number;
}

export interface WsClientMessage {
  type: "GET_DATA";
  payload: string;
}

export interface WsSuccessResponse {
  type: "SUCCESS";
  payload: LapTime[];
}

export interface WsErrorResponse {
  type: "ERROR";
  payload: string;
}
