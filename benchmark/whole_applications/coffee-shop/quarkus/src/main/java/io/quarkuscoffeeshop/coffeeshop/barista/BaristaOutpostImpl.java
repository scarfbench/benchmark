package io.quarkuscoffeeshop.coffeeshop.barista;

import io.quarkus.runtime.Startup;
import io.quarkus.vertx.ConsumeEvent;
import io.quarkuscoffeeshop.coffeeshop.barista.api.Barista;
import io.quarkuscoffeeshop.coffeeshop.barista.domain.BaristaRepository;
import io.quarkuscoffeeshop.coffeeshop.domain.valueobjects.OrderIn;
import io.quarkuscoffeeshop.coffeeshop.domain.valueobjects.OrderUp;
import io.quarkuscoffeeshop.utils.JsonUtil;
import io.smallrye.common.annotation.Blocking;
import io.vertx.core.eventbus.EventBus;
import io.vertx.mutiny.core.eventbus.Message;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import static io.quarkuscoffeeshop.coffeeshop.infrastructure.EventBusTopics.BARISTA_IN;
import static io.quarkuscoffeeshop.coffeeshop.infrastructure.EventBusTopics.ORDERS_UP;

@Startup @ApplicationScoped
public class BaristaOutpostImpl implements Barista {

    static final Logger LOGGER = LoggerFactory.getLogger(BaristaOutpostImpl.class);

    @Channel("barista-outpost")
    Emitter<OrderIn> baristaEmitter;

    @Inject
    EventBus eventBus;

    @ConsumeEvent(BARISTA_IN) @Blocking @Transactional
    public void onOrderIn(final Message orderInMessage) {
        baristaEmitter.send(JsonUtil.fromJsonToOrderIn(orderInMessage.body().toString()));
    }

    @Override
    public void onRemakeIn(final Message remakeMessage) {
        baristaEmitter.send(JsonUtil.fromJsonToOrderIn(remakeMessage.body().toString()));
    }

    @Override
    public void onCancelOrder(final Message cancellationMessage) {
        baristaEmitter.send(JsonUtil.fromJsonToOrderIn(cancellationMessage.body().toString()));
    }

    @Incoming("orders-up")  @Blocking @Transactional
    public void onOrderUp(OrderUp orderUp) {
        LOGGER.debug("OrderUp: {}", orderUp);
        eventBus.<OrderUp>publish(ORDERS_UP, JsonUtil.toJson(orderUp));
    }

}
