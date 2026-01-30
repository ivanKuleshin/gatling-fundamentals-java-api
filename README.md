Gatling Java API Udemy Course Code
============================================

## Running Simulations

### Prerequisites

- Java 21
- Maven 3.6+

### Run All Simulations with Aggregated Report

To execute all simulation classes and generate a single aggregated report:

```
mvn gatling:test
```

The combined report will be available at `target/gatling/aggregated/index.html`.

### Run a Specific Simulation

To run a single simulation class:

```
mvn gatling:test -Dgatling.simulationClass=videogamedb.scriptfundamentals.VideoGameDb
```

Replace `videogamedb.scriptfundamentals.VideoGameDb` with the fully qualified name of the desired simulation class.

### View Reports

- Open the generated `index.html` file in a web browser to view the performance results, charts, and statistics.
