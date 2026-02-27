# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project overview

This application represents a backend server for racetrack laps leaderboard. It stores racetrack data and provides GraphQL 
API for retrieving lap data by frontend clients.

## Architecture

### Technologies

The server application is written in Java 25 using Spring Boot 4 framework. 
It uses PostgreSQL database for storing data, and uses Flyway for database migrations. The database for local development
is Dockerized using the `docker-compose.yml` file. To access the database from the application, the Spring Data JDBC framework 
is used.

It exposes an external API through GraphQL (via the Spring GraphQL library).

### Code guidelines

The application should follow these basic rules:

- use strictly JSpecify annotations for nullability
- use immutable objects wherever possible
- use Lombok annotations wherever applicable to avoid boilerplate code
- use Domain-Driver Design principles (like value objects, entities, entity aggregates, repositories, services, factories)
- use final variables whenever possible
- use vertical layered architecture, meaning that each business domain should be separated into its own package and within it should use the traditional layered architecture
- do NOT use `var` keyword in Java code, use explicit types instead

When writing Spring components, follow these rules:

- explicitly declare dependencies @Bean annotation in configuration classes
- use constructor injection instead of setter injection

When updating a domain model:

- create an appropriate migration PostgreSQL script using Flyway

When writing tests:

- use JUnit 5 for unit tests
- use Spring Boot Test framework for integration tests
- use Testcontainers for integration tests that require a database

Each class should be documented using Javadoc about what it does and why.

### Database guidelines

- use explicit named constraints for primary keys, foreign keys, unique constraints, etc.

### External API guidelines

All external API endpoints should be written in GraphQL using the Spring GraphQL library.

### Business architecture

This application periodically downloads racetrack lap data from a simple WebSocket API. The API works by client polling all
accumulated lap data from the server and sending it to the client. The poll request message looks like this:

```json
{"type":"GET_DATA","payload":""}
```

and the response looks like this:

```json
{"type":"SUCCESS","payload":[...]}
```

where the payload is a JSON array of laps with metadata:

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
"IsRaceLap": 0
}
```

where the properties represent the following fields:

- `Id` - lap unique ID
- `RaceId` - unique ID of race (a single trackday)
- `RFIDId` - RFID tag ID - unique ID of the racer's RFID card (basically racer's ID)
- `Tag` - RFID tag
- `Time` - lap finish time
- `TimePrev` - lap start time
- `LapTime` - lap time in milliseconds, Negative values for invalid laps
- `TimeS1` - Timestamp when the car crossed the sector-1 checkpoint
- `TimeS2` - Timestamp when the car crossed the sector-2 checkpoint
- `TimeS3` - Timestamp when the car crossed the finish line (end of sector 3), Always equals Time
- `S1` - the first sector time in milliseconds
- `S2` - the second sector time in milliseconds
- `S3` - the third sector time in milliseconds
- `Temp1` - Ambient air temperature (°C)
- `Temp2` - Track surface temperature (°C)
- `Temp3` - Relative humidity (%)
- `Round` - 0-indexed lap number in the race for the given racer
- `CarNumber` - car number for the given racer (each racer has exactly one car number)
- `Category` - race category (Open, Ultimate, Racing, Touring)
- `Make` - car make
- `Model` - car model
- `Tires` - can be ignored
- `FirstName` - racer's first name
- `LastName` - racer's last name
- `Name` - race track name
- `Date` - trackday date
- `IsRaceLap` - Whether this lap counts toward final race standings, 1 = race lap, 0 = practice / non-scoring lap

This server application accepts the accumulated lap data from the WebSocket API, but stores only unique laps in the database.
This is checked based on uniqueness of the `id` + `raceId` fields in the database. If such a combination already exists,
the accepted lap data from the API is ignored. Otherwise, the lap data is stored in the database. 

The server separates the lap data into:

- race object
- racer object
- category enum
- lap object

The race object is extracted based on the `RaceId` identification and the server creates a new race in the database
if such a race does not exist yet (from the `RaceId`, `Name`, and `Date` fields).

The racer object is extracted based on the `RFIDId` identification and the server creates a new racer in the database
if such a racer does not exist yet (from the `RFIDId`, `CarNumber`, `FirstName`, and `LastName` fields).

The category enum is extracted based on the `Category` identification and the server creates a new category in the database
if such a category does not exist yet (from the `Category` field).

The lap stored in the database is linked to the race and racer objects and contains only the unique lap data fields (such
as timespans, car metadata, and lap number, etc.).

The stored lap data in the database are used by the server application to provide through GraphQL API to frontend clients
or for further calculations.

## Claude Code rules

Always use Context7 MCP when I need library/API documentation, code generation, setup or configuration steps without me having to explicitly ask.
