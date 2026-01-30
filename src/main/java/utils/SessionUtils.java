package utils;

/**
 * Utility class for handling Gatling context operations.
 */
public class SessionUtils {

    public static String buildSessionKey(String key) {
        return "#{" + key + "}";
    }
}
