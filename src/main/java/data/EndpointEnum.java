package data;

public enum EndpointEnum {
    AUTHENTICATE_ENDPOINT("/authenticate"),
    VIDEO_GAME_ENDPOINT("/videogame");

    private final String name;

    EndpointEnum(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return name;
    }
}
