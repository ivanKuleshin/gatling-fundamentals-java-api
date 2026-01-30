package videogamedb.scriptfundamentals;

import io.gatling.javaapi.core.ChainBuilder;
import io.gatling.javaapi.core.Simulation;
import io.gatling.javaapi.http.HttpProtocolBuilder;
import io.netty.handler.codec.http.HttpResponseStatus;
import utils.SessionUtils;

import java.util.concurrent.ThreadLocalRandom;

import static data.EndpointEnum.AUTHENTICATE_ENDPOINT;
import static io.gatling.javaapi.core.CoreDsl.StringBody;
import static io.gatling.javaapi.core.CoreDsl.exec;
import static io.gatling.javaapi.core.CoreDsl.jmesPath;
import static io.gatling.javaapi.http.HttpDsl.http;
import static io.gatling.javaapi.http.HttpDsl.status;

public abstract class BaseSimulation extends Simulation {

    protected static final String BASE_URL = "https://videogamedb.uk/api";
    protected static final String ACCEPT_JSON = "application/json";
    protected static final String CONTENT_TYPE_JSON = "application/json";
    protected static final String AUTH_HEADER = "Authorization";

    protected static final String JWT_TOKEN_KEY = "jwtToken";

    private static final String AUTH_BODY = """
            {
              "password": "admin",
              "username": "admin"
            }
            """;

    protected static String buildBearerToken(String token) {
        return "Bearer " + token;
    }

    protected static String getAuthValue() {
        return buildBearerToken(SessionUtils.buildSessionKey(JWT_TOKEN_KEY));
    }

    protected static HttpProtocolBuilder buildHttpProtocol() {
        return http
                .baseUrl(BASE_URL)
                .acceptHeader(ACCEPT_JSON)
                .contentTypeHeader(CONTENT_TYPE_JSON);
    }

    protected static final ChainBuilder authenticate =
            exec(http("Authenticate")
                    .post(AUTHENTICATE_ENDPOINT.getName())
                    .body(StringBody(AUTH_BODY))
                    .check(status().is(HttpResponseStatus.OK.code()))
                    .check(status().not(HttpResponseStatus.UNAUTHORIZED.code()))
                    .check(jmesPath("token").saveAs(JWT_TOKEN_KEY)));

    protected static String getRandomGameId() {
        // random game ID between 1 and 10
        return String.valueOf(ThreadLocalRandom.current().nextInt(1, 11));
    }
}
