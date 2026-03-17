package io.quarkuscoffeeshop.coffeeshop.web;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.InjectSpy;
import io.quarkuscoffeeshop.coffeeshop.counter.api.OrderService;
import io.quarkuscoffeeshop.coffeeshop.domain.commands.PlaceOrderCommand;
import io.quarkuscoffeeshop.coffeeshop.utils.TestUtils;
import io.quarkuscoffeeshop.utils.JsonUtil;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.ws.rs.core.MediaType;

import static io.restassured.RestAssured.given;
import static org.mockito.ArgumentMatchers.any;

@QuarkusTest
public class CoffeeshopApiResourceIT {

    private static final Logger LOGGER = LoggerFactory.getLogger(CoffeeshopApiResourceIT.class);

    @InjectSpy
    OrderService orderService;

    @Test
    public void testJson() {
        String json = "{\"id\":\"1e08c459-7e9e-463d-9c19-608688d1a63e\",\"orderSource\":\"COUNTER\",\"location\":\"ATLANTA\",\"loyaltyMemberId\":\"StarshipCaptain\",\"baristaItems\":[{\"name\":\"Jeremy\",\"item\":\"COFFEE_BLACK\",\"price\":3.50}],\"kitchenItems\":[]}";
        String jsop =                                 "{\"commandType\":\"PLACE_ORDER\",\"id\":\"985176b9-fb9a-4bec-87b5-501240f65b96\",\"orderSource\":\"COUNTER\",\"location\":\"ATLANTA\",\"loyaltyMemberId\":\"StarshipCaptain\",\"baristaItems\":[{\"item\":\"COFFEE_BLACK\",\"name\":\"Kirk\",\"price\":3.0}],\"kitchenItems\":null,\"timestamp\":1616611945.403978000}";

        given()
                .contentType(MediaType.APPLICATION_JSON)
                .body(JsonUtil.toJson(jsop))
                .when()
                .post("/api/order")
                .then()
                .assertThat()
                .statusCode(202);

        Mockito.verify(orderService, Mockito.times(1)).onOrderIn(any(PlaceOrderCommand.class));
    }

    @Test
    public void testPlaceOrder() {

        PlaceOrderCommand placeOrderCommand = TestUtils.mockPlaceOrderCommand();

        LOGGER.info("placeOrderCommand: {}", placeOrderCommand);
        LOGGER.info("Testing place order");
        System.out.println(JsonUtil.toJson(placeOrderCommand));

        given()
                .contentType(MediaType.APPLICATION_JSON)
                .body(JsonUtil.toJson(TestUtils.mockPlaceOrderCommand()))
                .when()
                .post("/api/order")
                .then()
                .assertThat()
                .statusCode(202);

        Mockito.verify(orderService, Mockito.times(1)).onOrderIn(any(PlaceOrderCommand.class));
    }

}
