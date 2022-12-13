package handlers;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import server.Firebase;
import server.handlers.AddSongHandler;
import server.handlers.AddToLikedSongsHandler;
import spark.Spark;

public class TestAddSongHandler {

  /** Handles setup before any tests are called. */
  @BeforeAll
  public static void setup_before_everything() {

    // Set the Spark port number. This can only be done once, and has to
    // happen before any route maps are added. Hence using @BeforeClass.
    // Setting port 0 will cause Spark to use an arbitrary available port.
    Spark.port(0);
    Logger.getLogger("").setLevel(Level.WARNING); // empty name = root logger
  }

  /**
   * Shared state for all tests. We need to be able to mutate it (adding recipes etc.) but never
   * need to replace the reference itself. We clear this state out after every test runs.
   */
  private Map<String, Object> responseMap = new HashMap<>();

  /** Handles setup before each individual test is called. */
  @BeforeEach
  public void setup() {
    // Re-initialize state, etc. for _every_ test method run
    this.responseMap.clear();
  }

  /** Handles teardown after each individual test is run. */
  @AfterEach
  public void teardown() {
    // Gracefully stop Spark listening on both endpoints
    Spark.unmap("/addLike");
    Spark.stop();
    Spark.awaitStop(); // don't proceed until the server is stopped
  }

  /**
   * Helper to start a connection to a specific API endpoint/params
   *
   * @param apiCall the call string, including endpoint (NOTE: this would be better if it had more
   *     structure!)
   * @return the connection for the given URL, just after connecting
   * @throws IOException if the connection fails for some reason
   */
  private static HttpURLConnection tryRequest(String apiCall) throws IOException {
    // Configure the connection (but don't actually send the request yet)
    URL requestURL = new URL("http://localhost:" + Spark.port() + "/" + apiCall);
    HttpURLConnection clientConnection = (HttpURLConnection) requestURL.openConnection();

    // The default method is "GET", which is what we're using here.
    // If we were using "POST", we'd need to say so.
    // clientConnection.setRequestMethod("GET");

    clientConnection.connect();
    return clientConnection;
  }

  /**
   * Helper method for fuzz testing.
   * @return
   */
  private static int getRandomLatLon() {
    Random rand = new Random();
    return rand.nextInt(360000);
  }

  // number of trials for fuzz test
  static final int NUM_TRIALS = 50;

  @Test
  public void fuzzTesting() throws IOException {
    Firebase f = new Firebase();
    Spark.get("/add", new AddSongHandler(f));
    Spark.init();
    Spark.awaitInitialization();

    for (int i = 0; i < NUM_TRIALS; i++) {
      int lat = getRandomLatLon();
      int lon = getRandomLatLon();
      int token = getRandomLatLon();

      String url = "add?lat=" + lat + "&lon=" + lon + "&token=" + token;
      HttpURLConnection clientConnection = tryRequest(url);
      assertEquals(200, clientConnection.getResponseCode());
      clientConnection.disconnect();
    }
  }
}
