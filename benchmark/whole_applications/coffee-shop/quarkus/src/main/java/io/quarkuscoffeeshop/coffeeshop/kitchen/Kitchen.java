package io.quarkuscoffeeshop.coffeeshop.kitchen;

import io.quarkuscoffeeshop.coffeeshop.domain.valueobjects.OrderIn;
import io.quarkuscoffeeshop.coffeeshop.domain.valueobjects.OrderUp;
import io.vertx.mutiny.core.eventbus.Message;

public interface Kitchen {

    public void onOrderIn(Message message);
}
