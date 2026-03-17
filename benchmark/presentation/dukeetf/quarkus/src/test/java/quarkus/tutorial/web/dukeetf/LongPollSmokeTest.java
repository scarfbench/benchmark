package jakarta.tutorial.web.dukeetf;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@QuarkusTest
class LongPollSmokeTest {
  @Test
  void dukeetfRespondsWithinAFewSeconds() {
    given().when().get("/dukeetf")
      .then().statusCode(200)
      .header("Content-Type", startsWith("text/html"))
      .body(matchesRegex("\\s*[-+]?\\d+\\.?\\d*\\s*/\\s*-?\\d+\\s*"));
  }
}