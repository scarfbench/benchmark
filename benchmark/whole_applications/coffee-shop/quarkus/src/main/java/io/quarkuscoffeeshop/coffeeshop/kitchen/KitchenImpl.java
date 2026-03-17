package io.quarkuscoffeeshop.coffeeshop.kitchen;

import io.quarkus.vertx.ConsumeEvent;
import io.quarkuscoffeeshop.coffeeshop.domain.Item;
import io.quarkuscoffeeshop.coffeeshop.domain.valueobjects.OrderIn;
import io.quarkuscoffeeshop.coffeeshop.domain.valueobjects.OrderUp;
import io.quarkuscoffeeshop.coffeeshop.kitchen.domain.KitchenOrder;
import io.quarkuscoffeeshop.coffeeshop.kitchen.domain.KitchenOrderRepository;
import io.quarkuscoffeeshop.utils.JsonUtil;
import io.smallrye.common.annotation.Blocking;
import io.vertx.mutiny.core.eventbus.EventBus;
import io.vertx.mutiny.core.eventbus.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.time.Instant;

import static io.quarkuscoffeeshop.coffeeshop.infrastructure.EventBusTopics.*;

@ApplicationScoped
public class KitchenImpl implements Kitchen {

    private static final Logger LOGGER = LoggerFactory.getLogger(KitchenImpl.class);

    String madeBy = "Sulu";

    @Inject
    EventBus eventBus;

    @Inject
    KitchenOrderRepository kitchenOrderRepository;

    @ConsumeEvent(KITCHEN_IN)
    @Blocking
    @Transactional
    public void onOrderIn(final Message message) {
        OrderIn orderIn = JsonUtil.fromJson(message.body().toString(), OrderIn.class);
        KitchenOrder kitchenOrder = new KitchenOrder(orderIn.orderId, orderIn.item, Instant.now());
        LOGGER.debug("order in : {}", orderIn);
        try {
            Thread.sleep(calculateDelay(orderIn.item));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        OrderUp orderUp = new OrderUp(
                orderIn.orderId,
                orderIn.itemId,
                orderIn.item,
                orderIn.name,
                Instant.now(),
                madeBy);
        kitchenOrder.setTimeUp(Instant.now());
        kitchenOrderRepository.persist(kitchenOrder);
        eventBus.<OrderUp>publish(ORDERS_UP, JsonUtil.toJson(orderUp));

    }

    private int calculateDelay(final Item item) {
        switch (item) {
            case CROISSANT:
                return 7000;
            case CROISSANT_CHOCOLATE:
                return 7000;
            case CAKEPOP:
                return 5000;
            case MUFFIN:
                return 7000;
            default:
                return 3000;
        }
    }
}
