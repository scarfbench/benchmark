package io.quarkuscoffeeshop.coffeeshop.barista.api;

import io.vertx.mutiny.core.eventbus.Message;

public interface Barista {

    /**
     * Message with a JSON payload corresponding to OrderIn value object
     *
     * @param orderInMessage OrderIn
     * @see io.quarkuscoffeeshop.coffeeshop.domain.valueobjects.OrderIn
     */
    public void onOrderIn(final Message orderInMessage);

    public void onRemakeIn(final Message remakeMessage);

    public void onCancelOrder(final Message cancellationMessage);
}