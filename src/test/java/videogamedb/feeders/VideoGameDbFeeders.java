package videogamedb.feeders;

import io.gatling.javaapi.core.ChainBuilder;
import io.gatling.javaapi.core.FeederBuilder;
import io.gatling.javaapi.core.ScenarioBuilder;
import io.gatling.javaapi.http.HttpProtocolBuilder;
import org.apache.commons.lang3.RandomStringUtils;
import videogamedb.scriptfundamentals.BaseSimulation;

import java.security.SecureRandom;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Stream;

import static data.EndpointEnum.VIDEO_GAME_ENDPOINT;
import static io.gatling.javaapi.core.CoreDsl.ElFileBody;
import static io.gatling.javaapi.core.CoreDsl.atOnceUsers;
import static io.gatling.javaapi.core.CoreDsl.bodyString;
import static io.gatling.javaapi.core.CoreDsl.csv;
import static io.gatling.javaapi.core.CoreDsl.exec;
import static io.gatling.javaapi.core.CoreDsl.feed;
import static io.gatling.javaapi.core.CoreDsl.jmesPath;
import static io.gatling.javaapi.core.CoreDsl.jsonFile;
import static io.gatling.javaapi.core.CoreDsl.scenario;
import static io.gatling.javaapi.http.HttpDsl.http;
import static io.gatling.javaapi.http.HttpDsl.status;

@SuppressWarnings("unused")
public class VideoGameDbFeeders extends BaseSimulation {

    private static final HttpProtocolBuilder httpProtocol = buildHttpProtocol();

    private static final int MIN_GAME_ID = 1;
    private static final int MAX_GAME_ID = 10;

    private static final FeederBuilder.FileBased<String> csvFeeder = csv("feeders/gameCsvFile.csv").circular();
    private static final FeederBuilder.FileBased<Object> jsonFeeder = jsonFile("feeders/gameJsonFile.json").random();
    private static final Iterator<Map<String, Object>> customGameIdFeeder =
            Stream.generate(() -> {
                Random rand = new SecureRandom();
                int gameId = rand.nextInt(MIN_GAME_ID, MAX_GAME_ID + 1);
                Map<String, Object> map = new HashMap<>();
                map.put("gameId", gameId);
                return map;
            }).iterator();

    private static final Iterator<Map<String, Object>> customCreateGameFeeder =
            Stream.generate(() -> {

                String gameName = RandomStringUtils.secure().nextAlphanumeric(5) + "-gameName";
                String releaseDate = randomDate().toString();
                int reviewScore = new SecureRandom().nextInt(100);
                String category = RandomStringUtils.secure().nextAlphanumeric(5) + "-category";
                String rating = RandomStringUtils.secure().nextAlphanumeric(4) + "-rating";

                Map<String, Object> gameMap = new HashMap<>();
                gameMap.put("gameName", gameName);
                gameMap.put("releaseDate", releaseDate);
                gameMap.put("reviewScore", reviewScore);
                gameMap.put("category", category);
                gameMap.put("rating", rating);
                return gameMap;
            }).iterator();

    private static final ChainBuilder createNewGame =
            feed(customCreateGameFeeder)
                    .exec(http("Create New Game using Custom Feeder and JSON template - #{gameName} and #{gameId}")
                            .post(VIDEO_GAME_ENDPOINT.getName())
                            .header(AUTH_HEADER, getAuthValue())
                            .body(ElFileBody("feeders/bodies/newGameTemplate.json")).asJson()
                            .check(status().is(200))
                            // id is always 0 when creating a new game
                            .check(jmesPath("id").exists())
                            .check(jmesPath("name").isEL("#{gameName}"))
                            .check(jmesPath("releaseDate").isEL("#{releaseDate}"))
                            .check(jmesPath("reviewScore").isEL("#{reviewScore}"))
                            .check(jmesPath("category").isEL("#{category}"))
                            .check(jmesPath("rating").isEL("#{rating}"))
                            .check(bodyString().saveAs("responseBody")))
                    .exec(session -> {
                        System.out.println(session.getString("responseBody"));
                        return session;
                    });

    // gameId and gameName come from csvFeeder
    private static final ChainBuilder getGameByIdCsv =
            feed(csvFeeder)
                    .exec(http("CSV Get Game by GameId - #{gameId}")
                            .get(VIDEO_GAME_ENDPOINT.getName() + "/#{gameId}")
                            .header(AUTH_HEADER, getAuthValue())
                            .check(status().is(200))
                            .check(jmesPath("name").isEL("#{gameName}"))
                    );

    // id and name come from jsonFeeder
    private static final ChainBuilder getGameByIdJson =
            feed(jsonFeeder)
                    .exec(http("JSON Get Game by GameId - #{id}")
                            .get(VIDEO_GAME_ENDPOINT.getName() + "/#{id}")
                            .header(AUTH_HEADER, getAuthValue())
                            .check(status().is(200))
                            .check(jmesPath("name").isEL("#{name}"))
                    );

    private static final ChainBuilder getGameByIdCustom =
            feed(customGameIdFeeder)
                    .exec(http("Custom Feeder Get Game by GameId - #{gameId}")
                            .get(VIDEO_GAME_ENDPOINT.getName() + "/#{gameId}")
                            .header(AUTH_HEADER, getAuthValue())
                            .check(status().is(200))
                            .check(jmesPath("id").isEL("#{gameId}"))
                    );

    private static final ScenarioBuilder scenario =
            scenario("Video Game Db With Feeders")
                    .exec(authenticate)
                    // we have only 4 lines in CSV, so the circular feeder will start from the beginning and use ID = 1 again
                    .repeat(5)
                    .on(exec(getGameByIdCsv).pause(1))
                    // we have 10 entries in JSON, so no repetition will occur
                    .repeat(10)
                    .on(exec(getGameByIdJson).pause(1))
                    .repeat(5)
                    .on(exec(getGameByIdCustom).pause(1))
                    .repeat(5)
                    .on(exec(createNewGame).pause(1));

    private static LocalDate randomDate() {
        int hundredYears = 100 * 365;
        return LocalDate.ofEpochDay(ThreadLocalRandom.current().nextInt(-hundredYears, hundredYears));
    }

    public VideoGameDbFeeders() {
        setUp(scenario.injectOpen(atOnceUsers(1))).protocols(httpProtocol);
    }
}
