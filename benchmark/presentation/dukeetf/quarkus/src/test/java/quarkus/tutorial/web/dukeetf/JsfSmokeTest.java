package jakarta.tutorial.web.dukeetf;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.startsWith;

@QuarkusTest
class JsfSmokeTest {
  @Test
  void mainXhtmlServed() {
    given().when().get("/main.xhtml")
      .then().statusCode(200)
      .header("Content-Type", startsWith("text/html"));
  }
}