# Injection Strategies for Load Testing

This section describes common methods for injecting users in Gatling simulations using `injectOpen()`. Each method
controls how virtual users (simulating real users) are added to the test over time. Below are examples with testing
values and brief descriptions of the resulting load behavior. These can be combined in sequences for complex profiles.

## Open vs Closed Models

When it comes to load model, systems behave in 2 different ways:

- Closed systems, where you control the concurrent number of users
  Open systems, where you control the arrival rate of users
  Make sure to use the proper load model that matches the load your live system experiences.
- Closed systems are systems where the number of concurrent users is capped. At full capacity, a new user can
  effectively
  enter the system only once another exits.

Typical systems that behave this way are:

- call center where all operators are busy
- ticketing websites where users get placed into a queue when the system is at full capacity

On the contrary, open systems have no control over the number of concurrent users: users keep on arriving even though
applications have trouble serving them. Most websites behave this way.

## Gatling Injection Models

**Open Model** (`injectOpen()`): Users arrive at a specified rate, execute their scenario once, and then leave the
system. The number of concurrent users varies depending on the arrival rate and scenario duration. This model is
suitable for testing systems that can handle variable load, such as web APIs or cloud services.

**Closed Model** (`injectClosed()`): A fixed number of users repeatedly execute the scenario in a loop, with think
times (pauses) between iterations. Concurrency remains constant throughout the test. This model is ideal for systems
with limited resources, like databases or legacy applications.

## Table of Contents

- [atOnceUsers](#atonceusers)
- [rampUsers](#rampusers)
- [constantUsersPerSec](#constantuserspersec)
- [rampUsersPerSec](#rampuserspersec)
- [nothingFor](#nothingfor)
- [maxDuration](#maxduration)
- [forever](#forever)
- [Combining Strategies](#combining-strategies)
- [Runtime Parameters](#runtime-parameters)

## atOnceUsers

Injects a fixed number of users all at once at the start of the injection phase.

- **Example**: `atOnceUsers(10)`
- **Load Behavior**: 10 users begin executing the scenario simultaneously. This creates an immediate spike in load,
  useful for testing sudden bursts or system limits. Total users: 10 (all active at once).

## rampUsers

Injects a fixed number of users gradually over a specified duration.

- **Example**: `rampUsers(20).during(10)`
- **Load Behavior**: Users are added steadily, reaching 20 total over 10 seconds (about 2 users per second). This
  simulates a smooth increase in traffic, like a growing user base. Total users: 20 (distributed over time).

## constantUsersPerSec

Injects users at a constant rate (users per second) for a specified duration.

- **Example**: `constantUsersPerSec(5).during(20)`
- **Load Behavior**: Users arrive at a steady 5 per second for 20 seconds, resulting in 100 total users. This models
  consistent traffic, such as steady API usage. Total users: 100 (rate-controlled).

## rampUsersPerSec

Ramps the injection rate from a starting value to an ending value over a duration.

- **Example**: `rampUsersPerSec(1).to(10).during(10)`
- **Load Behavior**: Starts at 1 user per second, increasing linearly to 10 users per second over 10 seconds. Total
  users: ~55 (average rate ~5.5/sec). This simulates accelerating load, like a viral event. Optional `.randomized()`
  adds jitter to avoid synchronized arrivals.

## nothingFor

Delays the start of injections by a specified time.

- **Example**: `nothingFor(5)`
- **Load Behavior**: No users are injected for 5 seconds, allowing a "warm-up" period. Often combined with other
  methods (e.g., `nothingFor(5), atOnceUsers(10)`).

## maxDuration

Limits the total duration of the simulation, ensuring it doesn't run indefinitely.

- **Example**: `.maxDuration(60)`
- **Load Behavior**: The test will terminate after 60 seconds, regardless of active users or injection status. This
  prevents open-model simulations from running too long and helps control test execution time. Combine with injections
  to cap overall load duration.

## forever

Makes the scenario loop indefinitely for each user.

- **Example**: `.forever().on(...)`
- **Load Behavior**: Each injected user repeats the sequence of actions forever until the simulation ends (e.g., via
  maxDuration or user completion). This simulates sustained, continuous activity per user, useful for long-running load
  tests without fixed iterations.

## Combining Strategies

Injections can be chained for complex profiles. For example:

```
// set up scenario with forever loop for each user
ScenarioBuilder myScenario = scenario("Video Game DB Load Test")
    .forever().on(
        exec(authenticate)
            .exec(getAllVideoGames)
            .pause(1)
            .exec(getSpecificGame)
            .pause(1)
            .exec(getAllVideoGames)
    );

// then use scenario in the simulation
setUp(
    myScenario.injectOpen(
        nothingFor(5),
        atOnceUsers(10),
        rampUsers(20).during(10),
        constantUsersPerSec(5).during(20),
        rampUsersPerSec(5).to(15).during(10)
    )
).maxDuration(60);
```

This creates a phased load: 5s delay, then 10 users at once,
followed by 20 ramped over 10s, then steady 5/sec for 20s, then ramping from 5 to 15 users/sec over 10s, with each user
looping actions forever, and the entire test capped at 60 seconds.

# Runtime Parameters

You can customize the simulation behavior at runtime using system properties. Pass them as JVM arguments when running
the test (e.g.,
`mvn gatling:test -Dgatling.simulationClass=videogamedb.simulation.VideoGameDbSimulations -DUSERS=20 -DRAMP_DURATION=10`).
This allows easy parameterization without modifying the code.

- `USERS`: Number of users injected at once via `atOnceUsers()` (default: 10).
- `RAMP_USERS`: Number of users to ramp up gradually via `rampUsers()` (default: 10).
- `RAMP_DURATION`: Duration in seconds for ramping users (default: 5).
- `TEST_DURATION`: Total test duration in seconds (default: 30). Can be used with `.maxDuration()` to cap the simulation
  length.

Example: `-DUSERS=15 -DRAMP_USERS=20 -DRAMP_DURATION=10 -DTEST_DURATION=60` runs with 15 at-once users, ramps 20 users
over 10 seconds, and limits the test to 60 seconds.
