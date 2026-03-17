package com.ibm.websphere.samples.daytrader;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;

@QuarkusTest
class TradeResourceTest {

    @Test
    void testMarketSummaryEndpoint() {
        given()
            .when().get("/rest/trade/market")
            .then()
            .statusCode(200);
    }
}
