package jakarta.tutorial.billpayment.payment;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.RestAssured;
import io.restassured.filter.cookie.CookieFilter;
import io.restassured.response.Response;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@QuarkusTest
public class PaymentFlowTest {

    @Test
    public void testDebitCardPaymentFlow() {
        CookieFilter cookieFilter = new CookieFilter();

        // Step 1: GET the index page
        Response getResp = RestAssured
                .given()
                .filter(cookieFilter)
                .when()
                .get("/index.xhtml")
                .then()
                .statusCode(200)
                .extract().response();

        // Step 2: Extract JSF ViewState
        String viewState = getResp.htmlPath()
                .getString("**.findAll { it.@name == 'jakarta.faces.ViewState' }[0].@value");

        assertThat("ViewState should not be null", viewState, notNullValue());

        // Step 3: POST the form with Debit selection and amount
        Response postResp = RestAssured
                .given()
                .filter(cookieFilter)
                .formParam("paymentForm:amt", "123.45") // Input amount
                .formParam("paymentForm:opt", "1") // Debit card
                .formParam("paymentForm:submit", "Pay") // Button clicked
                .formParam("paymentForm_SUBMIT", "1") // REQUIRED for JSF
                .formParam("jakarta.faces.ViewState", viewState)
                .when()
                .post("/index.xhtml")
                .then()
                .statusCode(200)
                .extract().response();

        String responseHtml = postResp.asString();

        // Step 4: Assert user is on response.xhtml and sees correct content
        assertThat(responseHtml, containsString("Bill Payment: Result"));
        assertThat(responseHtml, containsString("Debit Card:"));
        assertThat(responseHtml, containsString("$123.45"));
    }

    @Test
    public void testCreditCardPaymentFlow() {
        CookieFilter cookieFilter = new CookieFilter();

        // GET page first
        Response getResp = RestAssured
                .given().filter(cookieFilter)
                .when().get("/index.xhtml")
                .then().statusCode(200).extract().response();

        String viewState = getResp.htmlPath()
                .getString("**.findAll { it.@name == 'jakarta.faces.ViewState' }[0].@value");

        // POST form for Credit Card
        Response postResp = RestAssured
                .given()
                .filter(cookieFilter)
                .formParam("paymentForm:amt", "999.99")
                .formParam("paymentForm:opt", "2") // Credit
                .formParam("paymentForm:submit", "Pay")
                .formParam("paymentForm_SUBMIT", "1") // REQUIRED for JSF
                .formParam("jakarta.faces.ViewState", viewState)
                .when()
                .post("/index.xhtml")
                .then()
                .statusCode(200)
                .extract().response();

        String html = postResp.asString();

        // Confirm response page contains expected content
        assertThat(html, containsString("Bill Payment: Result"));
        assertThat(html, containsString("Credit Card:"));
        assertThat(html, containsString("$999.99"));
    }
}
