package videogamedb.scriptfundamentals;

import io.gatling.javaapi.core.ScenarioBuilder;
import io.gatling.javaapi.http.HttpDsl;
import io.gatling.javaapi.http.HttpProtocolBuilder;

import static data.EndpointEnum.VIDEO_GAME_ENDPOINT;
import static io.gatling.javaapi.core.CoreDsl.atOnceUsers;
import static io.gatling.javaapi.core.CoreDsl.scenario;

@Deprecated
public class MyFirstTest extends BaseSimulation {

    private static final HttpProtocolBuilder httpProtocol = buildHttpProtocol();
    private static final ScenarioBuilder scenario =
            scenario("My First Test")
                    .exec(HttpDsl.http("Get all games").get(VIDEO_GAME_ENDPOINT.getName()));

    public MyFirstTest() {
        setUp(scenario.injectOpen(atOnceUsers(1))).protocols(httpProtocol);
    }

}
