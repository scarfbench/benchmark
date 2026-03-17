package io.quarkuscoffeeshop.coffeeshop.counter;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkuscoffeeshop.coffeeshop.utils.TestUtils;
import io.quarkuscoffeeshop.coffeeshop.counter.api.OrderService;
import io.quarkuscoffeeshop.coffeeshop.counter.domain.OrderEventResult;
import io.quarkuscoffeeshop.coffeeshop.domain.ItemStatus;
import io.quarkuscoffeeshop.coffeeshop.domain.Order;
import io.quarkuscoffeeshop.coffeeshop.domain.OrderStatus;
import io.quarkuscoffeeshop.coffeeshop.domain.commands.PlaceOrderCommand;
import io.quarkuscoffeeshop.coffeeshop.domain.valueobjects.OrderIn;
import io.quarkuscoffeeshop.coffeeshop.domain.valueobjects.OrderUp;
import io.quarkuscoffeeshop.coffeeshop.domain.valueobjects.OrderUpdate;
import io.quarkuscoffeeshop.utils.JsonUtil;
import io.vertx.mutiny.core.eventbus.EventBus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.inject.Inject;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import jakarta.transaction.Transactional;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static io.quarkuscoffeeshop.coffeeshop.infrastructure.EventBusTopics.ORDERS_UP;
import static io.quarkuscoffeeshop.coffeeshop.infrastructure.EventBusTopics.WEB_UPDATES;
import static io.quarkuscoffeeshop.utils.JsonUtil.fromJson;
import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
public class OrderServiceTest {

    Logger logger = LoggerFactory.getLogger(OrderServiceTest.class);

    @Inject
    OrderService orderService;

    @Inject
    EventBus eventBus;

    List<OrderUpdate> webUpdates = new ArrayList<>();

    @BeforeEach
    public void setUp() {

        eventBus.consumer(WEB_UPDATES)
                .handler(message -> {
                    logger.info("message received: {}", message.body().toString());
                    assertNotNull(message);
                    webUpdates.add(fromJson(message.body().toString(), OrderUpdate.class));
                });
    }

    @Test
    /**
     * Verify that the appropriate message is sent on the Event Bus
     */
    public void testOnOrderIn() {

        PlaceOrderCommand placeOrderCommand = TestUtils.mockPlaceOrderCommand();
        assertNotNull(orderService);
        orderService.onOrderIn(placeOrderCommand);

        assertTrue(Order.count() >= 1);

        Order order = Order.findById(placeOrderCommand.getId());
        assertNotNull(order);
        assertEquals(ItemStatus.IN_PROGRESS, order.getBaristaLineItems().get().get(0).getItemStatus());
        assertEquals(OrderStatus.IN_PROGRESS, order.getOrderStatus());

        try {
            Thread.sleep(6000);
        } catch (InterruptedException e) {
            assertNull(e);
        }
    }

    @Test @Transactional
    public void testOnOrderUp() {

        PlaceOrderCommand placeOrderCommand = TestUtils.mockPlaceOrderCommand();
        OrderEventResult orderEventResult = Order.from(placeOrderCommand);
        orderEventResult.getOrder().persist();

        // create the OrderUp value object that would be returned by the Barista
        OrderIn orderIn = orderEventResult.getBaristaTickets().get().get(0);
        OrderUp orderUp = new OrderUp(
                orderIn.orderId,
                orderIn.itemId,
                orderIn.item,
                orderIn.name,
                Instant.now(),
                "Igor");

        // send the OrderUp value object to the appropriate channel where it should trigger the OrderService.onOrderUp method
        eventBus.publish(ORDERS_UP, JsonUtil.toJson(orderUp));

        // give the OrderService time to process the Order
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            assertNull(e);
        }

        Order order = Order.findById(placeOrderCommand.getId());
        assertNotNull(order);
        assertEquals(1, order.getBaristaLineItems().get().size());
        assertEquals(ItemStatus.FULFILLED, order.getBaristaLineItems().get().get(0).getItemStatus());
        assertEquals(0, order.getKitchenLineItems().get().size());
        assertEquals(OrderStatus.FULFILLED, order.getOrderStatus());
    }
}
