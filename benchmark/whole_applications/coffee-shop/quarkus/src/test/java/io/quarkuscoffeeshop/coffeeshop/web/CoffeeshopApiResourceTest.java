package io.quarkuscoffeeshop.coffeeshop.web;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkuscoffeeshop.coffeeshop.domain.commands.PlaceOrderCommand;
import io.quarkuscoffeeshop.coffeeshop.utils.TestUtils;
import io.quarkuscoffeeshop.utils.JsonUtil;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.ws.rs.core.MediaType;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertNull;

@QuarkusTest
public class CoffeeshopApiResourceTest {

    Logger LOGGER = LoggerFactory.getLogger(CoffeeshopApiResourceTest.class);

    @Test
    public void testPlaceOrder() {

        PlaceOrderCommand placeOrderCommand = TestUtils.mockPlaceOrderCommand();

        LOGGER.info("Testing place order with json: {}", JsonUtil.toJson(placeOrderCommand));
        given()
                .contentType(MediaType.APPLICATION_JSON)
                .when()
                .body(JsonUtil.toJson(placeOrderCommand))
                .post("/api/order")
                .then()
                .statusCode(202);
    }

    @Test
    public void testPlaceOrderForSingleCroissant() {

    }
}
