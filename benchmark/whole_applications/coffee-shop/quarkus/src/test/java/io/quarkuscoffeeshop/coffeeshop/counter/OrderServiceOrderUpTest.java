package io.quarkuscoffeeshop.coffeeshop.counter;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkuscoffeeshop.coffeeshop.counter.domain.OrderEventResult;
import io.quarkuscoffeeshop.coffeeshop.domain.ItemStatus;
import io.quarkuscoffeeshop.coffeeshop.domain.Order;
import io.quarkuscoffeeshop.coffeeshop.domain.OrderStatus;
import io.quarkuscoffeeshop.coffeeshop.domain.commands.PlaceOrderCommand;
import io.quarkuscoffeeshop.coffeeshop.domain.valueobjects.OrderIn;
import io.quarkuscoffeeshop.coffeeshop.domain.valueobjects.OrderUp;
import io.quarkuscoffeeshop.coffeeshop.utils.TestUtils;
import io.quarkuscoffeeshop.utils.JsonUtil;
import io.vertx.mutiny.core.eventbus.EventBus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.time.Instant;

import static io.quarkuscoffeeshop.coffeeshop.infrastructure.EventBusTopics.ORDERS_UP;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

@QuarkusTest
public class OrderServiceOrderUpTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(OrderServiceOrderUpTest.class);

    String orderId;

    OrderEventResult orderEventResult;

    @Inject
    EventBus eventBus;

    @BeforeEach @Transactional
    public void setUp() {

        PlaceOrderCommand placeOrderCommand = TestUtils.mockPlaceOrderCommand();
        orderEventResult = Order.from(placeOrderCommand);
        orderId = orderEventResult.getOrder().getOrderId();
        orderEventResult.getOrder().persist();
    }

    @Test
    public void testOnOrderUp() {

        // create the OrderUp value object that would be returned by the Barista
        OrderIn orderIn = orderEventResult.getBaristaTickets().get().get(0);
        OrderUp orderUp = new OrderUp(
                orderId,
                orderIn.itemId,
                orderIn.item,
                orderIn.name,
                Instant.now(),
                "Igor");

        // send the OrderUp value object to the appropriate channel where it should trigger the OrderService.onOrderUp method
        eventBus.publish(ORDERS_UP, JsonUtil.toJson(orderUp));

        // give the OrderService time to process the Order
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            assertNull(e);
        }

        LOGGER.info("lookup order");
        Order order = Order.findById(orderId);
        assertNotNull(order);
        assertEquals(1, order.getBaristaLineItems().get().size());
        assertEquals(ItemStatus.FULFILLED, order.getBaristaLineItems().get().get(0).getItemStatus());
        assertEquals(0, order.getKitchenLineItems().get().size());
        assertEquals(OrderStatus.FULFILLED, order.getOrderStatus());
    }
}
