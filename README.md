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

## Running the VideoGameDB Application Locally

The Gatling simulations in this project are designed to test the [VideoGameDB](https://github.com/james-willett/VideoGameDB) application, which provides a REST API with endpoints supporting both JSON and XML.

To run the VideoGameDB application locally:

1. Clone the VideoGameDB repository:
   ```
   git clone https://github.com/james-willett/VideoGameDB.git
   cd VideoGameDB
   ```

2. Run the application using either **Gradle** or **Maven**:
   - **Gradle**: `./gradlew bootRun`
   - **Maven**: `mvn spring-boot:run`

3. Once running, the API will be available at [http://localhost:8080](http://localhost:8080)

4. Explore the API endpoints using Swagger UI at [http://localhost:8080/swagger-ui/index.html#/](http://localhost:8080/swagger-ui/index.html#/)

With the VideoGameDB application running locally, you can execute your Gatling performance tests against the local instance instead of the remote server.
