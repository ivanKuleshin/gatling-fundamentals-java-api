package videogamedb.simulation;

import io.gatling.javaapi.core.ChainBuilder;
import io.gatling.javaapi.core.ScenarioBuilder;
import io.gatling.javaapi.http.HttpProtocolBuilder;
import videogamedb.scriptfundamentals.BaseSimulation;

import static data.EndpointEnum.VIDEO_GAME_ENDPOINT;
import static io.gatling.javaapi.core.CoreDsl.atOnceUsers;
import static io.gatling.javaapi.core.CoreDsl.constantUsersPerSec;
import static io.gatling.javaapi.core.CoreDsl.exec;
import static io.gatling.javaapi.core.CoreDsl.nothingFor;
import static io.gatling.javaapi.core.CoreDsl.rampUsers;
import static io.gatling.javaapi.core.CoreDsl.rampUsersPerSec;
import static io.gatling.javaapi.core.CoreDsl.scenario;
import static io.gatling.javaapi.http.HttpDsl.http;

public class VideoGameDbSimulations extends BaseSimulation {

    private static final HttpProtocolBuilder httpProtocol = buildHttpProtocol();

    private static final int USER_COUNT = Integer.parseInt(System.getProperty("USERS", "10"));
    private static final int RAMP_USER_COUNT = Integer.parseInt(System.getProperty("RAMP_USERS", "10"));
    private static final int RAMP_DURATION = Integer.parseInt(System.getProperty("RAMP_DURATION", "5"));
    private static final int TEST_DURATION = Integer.parseInt(System.getProperty("TEST_DURATION", "30"));

    @Override
    public void before() {
        System.out.printf("Running test with %d users%n", USER_COUNT);
        System.out.printf("Ramping users over %d seconds%n", RAMP_DURATION);
        System.out.printf("Total test duration: %d seconds%n", TEST_DURATION);
    }

    private static final ChainBuilder getAllVideoGames =
            exec(http("Get all video games")
                    .get(VIDEO_GAME_ENDPOINT.getName()));

    private static final ChainBuilder getSpecificGame =
            exec(http("Get specific game")
                    .get(VIDEO_GAME_ENDPOINT.getName() + "/" + getRandomGameId()));

    private static final ScenarioBuilder scenario = scenario("Video game db - Section 7 code")
            .exec(authenticate)
            .exec(getAllVideoGames)
            .pause(1)
            .exec(getSpecificGame)
            .pause(1)
            .exec(getAllVideoGames);


    public VideoGameDbSimulations() {
        setUp(
                scenario
                        .injectOpen(
                                nothingFor(5),
                                rampUsersPerSec(1).to(RAMP_USER_COUNT).during(RAMP_DURATION),
                                constantUsersPerSec(USER_COUNT).during(RAMP_DURATION),
                                rampUsersPerSec(RAMP_USER_COUNT).to(1).during(RAMP_DURATION)
                        )
                        .protocols(httpProtocol));
    }
}
