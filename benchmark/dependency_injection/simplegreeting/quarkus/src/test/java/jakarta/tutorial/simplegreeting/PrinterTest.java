package jakarta.tutorial.simplegreeting;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.RestAssured;
import io.restassured.filter.cookie.CookieFilter;
import io.restassured.response.Response;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@QuarkusTest
public class PrinterTest {

    @Test
    public void testFlow() {
        CookieFilter cookieFilter = new CookieFilter();

        Response getResp = RestAssured
                .given()
                .filter(cookieFilter)
                .when()
                .get("/index.xhtml")
                .then()
                .statusCode(200)
                .extract().response();

        String viewState = getResp.htmlPath()
                .getString("**.findAll { it.@name == 'jakarta.faces.ViewState' }[0].@value");

        assertThat("ViewState should not be null", viewState, notNullValue());

        Response postResp = RestAssured
                .given()
                .filter(cookieFilter)
                .formParam("greetme:name", "John")
                .formParam("greetme:submit", "Say Hello")
                .formParam("greetme_SUBMIT", "1")
                .formParam("jakarta.faces.ViewState", viewState)
                .when()
                .post("/index.xhtml")
                .then()
                .statusCode(200)
                .extract().response();

        String responseHtml = postResp.asString();

        assertThat(responseHtml, containsString("Hi, John!"));
    }

}
