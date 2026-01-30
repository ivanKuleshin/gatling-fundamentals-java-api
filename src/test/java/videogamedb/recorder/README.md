# Recorder Directory

This directory contains Gatling simulations that were automatically generated using the **Gatling Recorder** tool. The
recorder captures HTTP interactions, and converts them into executable Gatling test scripts.

## Gatling Recorder Overview

The Gatling Recorder is a standalone application that allows you to record HTTP traffic from your browser or through a
proxy. It captures requests and responses, then generates Gatling simulation code automatically. This is particularly
useful for quickly prototyping tests based on real user interactions or API calls, without writing code from scratch.
You can download and run the recorder from the Gatling website. To run the recorder via Maven in this project, use:
`mvn gatling:recorder`

## Simulations

### RecordedSimulation.java

- **Source**: Browser recording (Chrome browser interactions).
- **Base URL**: `https://www.videogamedb.uk`
- **Scenario**: Simulates a user performing CRUD operations on the Video Game Database API:
    1. GET all video games (`/api/videogame`)
    2. GET a specific game (`/api/videogame/2`)
    3. Authenticate (POST `/api/authenticate`)
    4. Create a new game (POST `/api/videogame`)
    5. Update a game (PUT `/api/videogame/3`)
    6. Delete a game (DELETE `/api/videogame/3`)
- **Headers**: Includes authorization tokens.
- **Bodies**: Request bodies are stored in `src/test/resources/videogamedb/recordedsimulation/` as JSON files (e.g.,
  `0002_request.json` for authentication).
- **Load**: Runs with 1 user at once.

### RecordedSimulationProxy.java

- **Source**: Proxy recording (likely from Postman or a similar API client).
- **Base URL**: `https://videogamedb.uk`
- **Scenario**: Similar CRUD operations as above, but with slight variations (e.g., deletes game ID 2 instead of 3).
- **Headers**: Includes authorization.
- **Bodies**: Request bodies in `src/test/resources/videogamedb/recordedsimulationproxy/`.
- **Load**: Runs with 1 user at once.

## How to Run

1. Ensure the Video Game Database API is running (e.g., at `https://videogamedb.uk` or locally).
2. Run the simulations via Maven: `mvn gatling:test -Dgatling.simulationClass=videogamedb.recorder.RecordedSimulation` (
   or `RecordedSimulationProxy`).
3. View reports in `target/gatling/` after execution.

## Notes

- These are **auto-generated** scripts and may include unnecessary headers or pauses that can be cleaned up for
  production use.
- Authentication tokens are hardcoded and may expire; update them as needed.
- Pauses between requests simulate real user think time but can be adjusted.
- The recorder is useful for quickly capturing existing workflows, but manual scripting is recommended for complex or
  scalable load tests.
