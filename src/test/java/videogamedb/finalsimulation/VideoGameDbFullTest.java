package videogamedb.finalsimulation;

import io.gatling.javaapi.core.ChainBuilder;
import io.gatling.javaapi.core.FeederBuilder;
import io.gatling.javaapi.core.ScenarioBuilder;
import io.gatling.javaapi.http.HttpProtocolBuilder;
import videogamedb.scriptfundamentals.BaseSimulation;

import static data.EndpointEnum.VIDEO_GAME_ENDPOINT;
import static io.gatling.javaapi.core.CoreDsl.ElFileBody;
import static io.gatling.javaapi.core.CoreDsl.atOnceUsers;
import static io.gatling.javaapi.core.CoreDsl.bodyString;
import static io.gatling.javaapi.core.CoreDsl.constantUsersPerSec;
import static io.gatling.javaapi.core.CoreDsl.exec;
import static io.gatling.javaapi.core.CoreDsl.feed;
import static io.gatling.javaapi.core.CoreDsl.jmesPath;
import static io.gatling.javaapi.core.CoreDsl.jsonFile;
import static io.gatling.javaapi.core.CoreDsl.nothingFor;
import static io.gatling.javaapi.core.CoreDsl.rampUsersPerSec;
import static io.gatling.javaapi.core.CoreDsl.scenario;
import static io.gatling.javaapi.http.HttpDsl.http;

@SuppressWarnings("unused")
public class VideoGameDbFullTest extends BaseSimulation {

    // HTTP PROTOCOL
    private static final HttpProtocolBuilder httpProtocol = buildHttpProtocol();

    // RUNTIME PARAMETERS
    private static final int USER_COUNT = Integer.parseInt(System.getProperty("USERS", "5"));
    private static final int RAMP_DURATION = Integer.parseInt(System.getProperty("RAMP_DURATION", "10"));
    private static final int TEST_DURATION = Integer.parseInt(System.getProperty("TEST_DURATION", "30"));

    // FEEDER FOR TEST - CSV, JSON etc.
    private static final FeederBuilder.FileBased<Object> jsonFeeder = jsonFile("feeders/gameJsonFile.json").random();

    // BEFORE BLOCK
    @Override
    public void before() {
        System.out.printf("Running test with %d users%n", USER_COUNT);
        System.out.printf("Ramping users over %d seconds%n", RAMP_DURATION);
        System.out.printf("Total test duration: %d seconds%n", TEST_DURATION);
    }

    // HTTP CALLS
    private static final ChainBuilder getAllVideoGames =
            exec(http("Get all video games")
                    .get(VIDEO_GAME_ENDPOINT.getName()));

    private static final ChainBuilder createNewGame =
            feed(jsonFeeder)
                    .exec(http("Create New Game - #{name}")
                            .post(VIDEO_GAME_ENDPOINT.getName())
                            .header(AUTH_HEADER, getAuthValue())
                            .body(ElFileBody("feeders/bodies/newGameTemplate.json")).asJson());

    private static final ChainBuilder getLastPostedGameById =
            exec(http("Get Last Posted Game by ID - #{name}")
                    .get(VIDEO_GAME_ENDPOINT.getName() + "/#{id}")
                    .check(jmesPath("name").isEL("#{name}")));

    private static final ChainBuilder deleteLastPostedGame =
            exec(http("Delete game - #{name}")
                    .delete(VIDEO_GAME_ENDPOINT.getName() + "/#{id}")
                    .header(AUTH_HEADER, getAuthValue())
                    .check(bodyString().is("Video game deleted")));

    // SCENARIO OR USER JOURNEY
    // 1. Get all video games
    // 2. Create a new game
    // 3. Get details of newly created game
    // 4. Delete newly created game
    private static final ScenarioBuilder scenario = scenario("Video game db - final simulation")
            .forever().on(
                    exec(getAllVideoGames)
                            .pause(2)
                            .exec(authenticate)
                            .pause(2)
                            .exec(createNewGame)
                            .pause(2)
                            .exec(getLastPostedGameById)
                            .pause(2)
                            .exec(deleteLastPostedGame)
            );

    private static final ScenarioBuilder myScenario = scenario("Video game db - final simulation")
            .repeat(5)
            .on(exec(getAllVideoGames).pause(1))
            .exec(authenticate)
            .pause(1)
            .exec(createNewGame)
            .pause(1)
            .exec(getLastPostedGameById)
            .pause(1)
            .exec(deleteLastPostedGame)
            .exec(getAllVideoGames).pause(1);

    // LOAD SIMULATION
    public VideoGameDbFullTest() {
        setUp(
                myScenario.injectOpen(
                        nothingFor(5),
                        rampUsersPerSec(1).to(USER_COUNT).during(RAMP_DURATION),
                        atOnceUsers(USER_COUNT * 2),
                        constantUsersPerSec(USER_COUNT).during(TEST_DURATION),
                        rampUsersPerSec(USER_COUNT).to(1).during(RAMP_DURATION * 2L)
                ).protocols(httpProtocol)
        );
    }

//    public VideoGameDbFullTest() {
//        setUp(
//                scenario.injectOpen(
//                        nothingFor(5),
//                        rampUsers(USER_COUNT).during(RAMP_DURATION)
//                ).protocols(httpProtocol)
//        ).maxDuration(TEST_DURATION);
//    }

    // AFTER BLOCK
    @Override
    public void after() {
        System.out.println("Stress test completed");
    }

}
