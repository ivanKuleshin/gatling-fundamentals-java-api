import io.gatling.app.Gatling;

public class Engine {

  public static void main(String[] args) {
    String[] gatlingArgs = {
      "--simulation", "videogamedb.finalsimulation.VideoGameDbFullTest",
      "--results-folder", IDEPathHelper.resultsDirectory.toString()
    };
    Gatling.main(gatlingArgs);
  }
  // To run this main, you need to add the following JVM options:
  // --add-opens=java.base/java.lang=ALL-UNNAMED --add-opens=java.base/java.util=ALL-UNNAMED

  // or just use maven command:
  // ./mvnw gatling:test -Dgatling.simulationClass=videogamedb.simulation.VideoGameDbSimulations
}
