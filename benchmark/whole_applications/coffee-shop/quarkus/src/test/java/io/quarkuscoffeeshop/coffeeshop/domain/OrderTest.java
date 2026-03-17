package io.quarkuscoffeeshop.coffeeshop.domain;

import io.quarkuscoffeeshop.coffeeshop.counter.domain.OrderEventResult;
import io.quarkuscoffeeshop.coffeeshop.domain.commands.PlaceOrderCommand;
import io.quarkuscoffeeshop.coffeeshop.utils.TestUtils;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class OrderTest {

    @Test
    public void testCreateOrderFromPlaceOrderCommand() {

        PlaceOrderCommand placeOrderCommand = TestUtils.mockPlaceOrderCommand();

        OrderEventResult orderEventResult = Order.from(placeOrderCommand);
        assertNotNull(orderEventResult);
        assertNotNull(orderEventResult.getOrder());
        assertTrue(orderEventResult.getOrder().getBaristaLineItems().isPresent());
        assertFalse(orderEventResult.getOrder().getKitchenLineItems().isPresent());
    }
}
