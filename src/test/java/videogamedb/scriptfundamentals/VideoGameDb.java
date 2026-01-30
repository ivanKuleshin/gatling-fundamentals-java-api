package videogamedb.scriptfundamentals;

import io.gatling.javaapi.core.ChainBuilder;
import io.gatling.javaapi.core.ScenarioBuilder;
import io.gatling.javaapi.core.Session;
import io.gatling.javaapi.http.HttpProtocolBuilder;
import io.netty.handler.codec.http.HttpResponseStatus;

import java.time.Duration;
import java.util.List;

import static data.EndpointEnum.VIDEO_GAME_ENDPOINT;
import static io.gatling.javaapi.core.CoreDsl.StringBody;
import static io.gatling.javaapi.core.CoreDsl.atOnceUsers;
import static io.gatling.javaapi.core.CoreDsl.exec;
import static io.gatling.javaapi.core.CoreDsl.jmesPath;
import static io.gatling.javaapi.core.CoreDsl.jsonPath;
import static io.gatling.javaapi.core.CoreDsl.repeat;
import static io.gatling.javaapi.core.CoreDsl.scenario;
import static io.gatling.javaapi.http.HttpDsl.http;
import static io.gatling.javaapi.http.HttpDsl.status;
import static utils.SessionUtils.buildSessionKey;

@SuppressWarnings("unused")
public class VideoGameDb extends BaseSimulation {

    private static final HttpProtocolBuilder httpProtocol = buildHttpProtocol();

    private static final String CREATE_GAME_BODY = """
            {
              "category": "Platform",
              "name": "Mario",
              "rating": "Mature",
              "releaseDate": "2012-05-04",
              "reviewScore": 85
            }
            """;

    private static final String GAME_NAME_KEY = "gameName";
    private static final String NEW_GAME_NAME_KEY = "newGameName";

    private static final ChainBuilder createNewGame =
            exec(http("Create new game")
                    .post(VIDEO_GAME_ENDPOINT.getName())
                    .header(AUTH_HEADER, getAuthValue())
                    .body(StringBody(CREATE_GAME_BODY))
                    .check(status().in(
                            HttpResponseStatus.OK.code(),
                            HttpResponseStatus.CREATED.code()))
                    .check(jmesPath("name").saveAs(NEW_GAME_NAME_KEY))
            );

    private static final ChainBuilder getGameById =
            repeat(2, "myCounter").on(
                    exec(http("[#{myCounter}] Get game by ID, except new game - %s".formatted(buildSessionKey(NEW_GAME_NAME_KEY)))
                            .get(VIDEO_GAME_ENDPOINT + "/" + getRandomGameId())
                            .header(AUTH_HEADER, getAuthValue())
                            .check(status().is(HttpResponseStatus.OK.code()))
                            .check(jsonPath("$.name").saveAs(GAME_NAME_KEY))
                    )
            );

    private static final ChainBuilder getAllGames =
            exec(http("Get all games")
                    .get(VIDEO_GAME_ENDPOINT.getName())
                    .check(status().is(HttpResponseStatus.OK.code()))
                    // JSONPath checks
                    .check(jsonPath("$[?(@.id==1)].name").is("Resident Evil 4"))
                    .check(jsonPath("$[?(@.category=='Shooter' && @.name=='Doom')]").exists())
                    .check(jsonPath("$[*].name").findAll()
                            .transform(names -> names.contains("Minecraft")).is(true))
                    // JMESPath checks
                    .check(jmesPath("[? id == `3`].name").ofList().is(List.of("Tetris")))
                    .check(jmesPath("[? reviewScore > `90`].name").ofList()
                            .transform(list -> list.contains("Final Fantasy VII")).is(true))
                    .check(jmesPath("contains([? id == `3`].name, 'Tetris')").is("true"))
                    // check Gatling session variable in the response
                    .check(jmesPath("[*].name").ofList()
                            .transformWithSession((names, session) -> names.contains(session.getString(GAME_NAME_KEY)))
                            .is(true))
                    .check(jsonPath("$[*].name").findAll()
                            .transformWithSession((names, session) ->
                                    names.contains(session.getString(NEW_GAME_NAME_KEY)))
                            .is(false))
            );

    private static final ChainBuilder logSessionValues =
            exec(session -> {
                        System.out.println("Newly created game name: " + session.getString(NEW_GAME_NAME_KEY));
                        System.out.println("Fetched game name by ID: " + session.getString(GAME_NAME_KEY));

                        // Session instances are immutable,
                        // meaning that methods such as set return a new instance
                        // and leave the original instance unmodified!
                        Session newSession = session.set(GAME_NAME_KEY, "Overwritten value");
                        System.out.println("Overwritten Game Name value in session: " + newSession.getString(GAME_NAME_KEY));

                        return newSession;
                    }
            );

    private static final ScenarioBuilder scenario =
            scenario("Video Game Db - Section 5 code")
                    .exec(authenticate)
                    .pause(1, 5)
                    .exec(createNewGame)
                    .pause(2)
                    .exec(getGameById)
                    .pause(Duration.ofSeconds(3))
                    .exec(getAllGames)
                    .exec(logSessionValues);

    public VideoGameDb() {
        setUp(scenario.injectOpen(atOnceUsers(1))).protocols(httpProtocol);
    }
}
