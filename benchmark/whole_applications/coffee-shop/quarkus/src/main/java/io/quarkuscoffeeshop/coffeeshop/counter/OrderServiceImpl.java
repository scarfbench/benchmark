package io.quarkuscoffeeshop.coffeeshop.counter;

import io.quarkus.vertx.ConsumeEvent;
import io.quarkuscoffeeshop.coffeeshop.counter.api.OrderService;
import io.quarkuscoffeeshop.coffeeshop.counter.domain.OrderEventResult;
import io.quarkuscoffeeshop.coffeeshop.domain.Order;
import io.quarkuscoffeeshop.coffeeshop.domain.commands.PlaceOrderCommand;
import io.quarkuscoffeeshop.coffeeshop.domain.valueobjects.OrderUp;
import io.quarkuscoffeeshop.coffeeshop.infrastructure.OrderRepository;
import io.quarkuscoffeeshop.utils.JsonUtil;
import io.smallrye.common.annotation.Blocking;
import io.smallrye.mutiny.Uni;
import io.vertx.core.eventbus.EventBus;
import io.vertx.mutiny.core.eventbus.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.util.concurrent.CompletableFuture;

import static io.quarkuscoffeeshop.coffeeshop.infrastructure.EventBusTopics.*;
import static io.quarkuscoffeeshop.utils.JsonUtil.fromJsonToOrderUp;
import static io.quarkuscoffeeshop.utils.JsonUtil.toJson;

@ApplicationScoped
public class OrderServiceImpl implements OrderService {

    Logger LOGGER = LoggerFactory.getLogger(OrderServiceImpl.class);

    @Inject
    EventBus eventBus;

    @Inject
    OrderRepository orderRepository;

    @Transactional
    public void onOrderIn(final PlaceOrderCommand placeOrderCommand) {
        LOGGER.debug("PlaceOrderCommand received: {}", placeOrderCommand);
        OrderEventResult orderEventResult = Order.from(placeOrderCommand);

        orderRepository.persist(orderEventResult.getOrder());

        orderEventResult.getOrderUpdates().forEach(orderUpdate -> {
            eventBus.publish(WEB_UPDATES, JsonUtil.toJson(orderUpdate));
            LOGGER.debug("sent web update: {}", orderUpdate);
        });
        if (orderEventResult.getBaristaTickets().isPresent()) {
            orderEventResult.getBaristaTickets().get().forEach(baristaTicket -> {
                eventBus.send(BARISTA_IN, JsonUtil.toJson(baristaTicket));
                LOGGER.debug("sent to barista: {}", baristaTicket);
            });
        }
        if (orderEventResult.getKitchenTickets().isPresent()) {
            orderEventResult.getKitchenTickets().get().forEach(kitchenTicket -> {
                eventBus.send(KITCHEN_IN, JsonUtil.toJson(kitchenTicket));
                LOGGER.debug("sent to kitchen: {}", kitchenTicket);
            });
        }
    }

    private void saveOrder(final Order order) {
        orderRepository.persistAndFlush(order);
    }

    @Override
    @ConsumeEvent(ORDERS_UP)
    @Blocking
    @Transactional
    public void onOrderUp(final Message message) {

        LOGGER.debug("order up message: {}", message.body());
        OrderUp orderUp = fromJsonToOrderUp(message.body().toString());

        Order order = orderRepository.findById(orderUp.orderId);
        OrderEventResult orderEventResult = order.apply(orderUp);
        orderRepository.persistAndFlush(order);
        orderEventResult.getOrderUpdates().forEach(orderUpdate -> {
            eventBus.publish(WEB_UPDATES, JsonUtil.toJson(orderUpdate));
        });

    }

    private OrderEventResult applyOrderUp2(final OrderUp orderUp) {
        Order order = orderRepository.findById(orderUp.orderId);
        OrderEventResult orderEventResult = order.apply(orderUp);
        return orderEventResult;
    }

    @Transactional
    CompletableFuture<OrderEventResult> applyOrderUp(final OrderUp orderUp) {
        Order order = orderRepository.findById(orderUp.orderId);
        OrderEventResult orderEventResult = order.apply(orderUp);
        LOGGER.debug("After applying OrderUp event Order: {}", orderEventResult.getOrder());

        orderEventResult.getOrderUpdates().forEach(orderUpdate -> {
            eventBus.send(WEB_UPDATES, toJson(orderUpdate));
            LOGGER.debug("sent web update: {}", orderUpdate);
        });

        orderRepository.persist(orderEventResult.getOrder());
        LOGGER.debug("persisted order: {}", orderEventResult.getOrder());

        return CompletableFuture.completedFuture(orderEventResult);
    }


    public OrderServiceImpl() {
    }
}
