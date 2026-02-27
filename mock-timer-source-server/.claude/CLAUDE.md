# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project overview

It is a simple server for generated mocked racetrack lap times. It simulates a running racetrack session where racers
periodically do new lap times. It is used as a test server for a leaderboard application. 

## Functionality

There is a fixed list of racers that can race specified in the `racers.csv` file and fixed number of tracks in the `tracks.csv` file.

When the server starts, it picks a random track from the list of tracks. It then starts to generate lap times for the
available racers every 30 seconds starting from the start of the server's uptime. .

## Architecture

### Technologies

It is built using Node.js and Express.js. It uses strict typescript configuration and eslint linting.

### Application architecture

Each generated lap time is stored in a memory and destroyed after the server stops. Each lap time is stored as a single object:

```json
{
"Id": 77713,
"RaceId": 26,
"RFIDId": 133,
"Time": "2025-09-13T13:48:19.327Z",
"TimePrev": "2025-09-13T13:47:08.621Z",
"Tag": "E55202107040000000000050",
"LapTime": 70706,
"TimeS1": "2025-09-13T13:47:27.210Z",
"TimeS2": "2025-09-13T13:47:54.056Z",
"TimeS3": "2025-09-13T13:48:19.327Z",
"S1": 18589,
"S2": 26846,
"S3": 25271,
"Temp1": 15,
"Temp2": 16.81,
"Temp3": 91,
"Round": 0,
"CarNumber": 50,
"Category": "Open",
"Make": "Hyundai",
"Model": "i30N",
"Tires": null,
"FirstName": "Josef",
"LastName": "Joneš",
"Name": "Vysoké Mýto",
"Date": "2025-09-13T00:00:00.000Z",
"IsRaceLap": 1
}
```

The lap time data when generated are picked from the picked racer data and picked track data. The track times must be in
the track's duration range specified in the `tracks.csv` file as `durationFrom` and `durationTo` (in milliseconds).
The `RaceId` will be constructed as `{year}{month}{day}` generated once at the start of the server.

The server exposes a single WebSocket endpoint for clients to connect to on `/ws` path. The subprotocol behaves in the following way:

When a client sends the following message:

```json
{"type":"GET_DATA","payload":""}
```

the server response with a list of all generated lap times until the client request. The response looks like this:

```json
{"type":"SUCCESS","payload":[...]}
```

where each payload item is a single lap object:

```json
{
"Id": 77713,
"RaceId": 26,
"RFIDId": 133,
"Time": "2025-09-13T13:48:19.327Z",
"TimePrev": "2025-09-13T13:47:08.621Z",
"Tag": "E55202107040000000000050",
"LapTime": 70706,
"TimeS1": "2025-09-13T13:47:27.210Z",
"TimeS2": "2025-09-13T13:47:54.056Z",
"TimeS3": "2025-09-13T13:48:19.327Z",
"S1": 18589,
"S2": 26846,
"S3": 25271,
"Temp1": 15,
"Temp2": 16.81,
"Temp3": 91,
"Round": 0,
"CarNumber": 50,
"Category": "Open",
"Make": "Hyundai",
"Model": "i30N",
"Tires": null,
"FirstName": "Josef",
"LastName": "Joneš",
"Name": "Vysoké Mýto",
"Date": "2025-09-13T00:00:00.000Z",
"IsRaceLap": 1
}
```

### GraphQL Frontend API

The server application exposes a GraphQL API on `/graphql` path. It is read-only API and does not support any mutations
from the frontend.

It provides queries for retrieving lap data, track information, and race details. The API is designed to be used by the frontend to display
a detailed leaderboard.

The API provides the following queries:
**todo**
- `laps` - returns a list of all generated lap times
- `track` - returns a single track information
- `race` - returns a single race details