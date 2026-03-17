package com.coffeeshop.counter.api;

import com.coffeeshop.common.commands.PlaceOrderCommand;
import org.springframework.messaging.Message;

public interface OrderService {
    void onOrderIn(PlaceOrderCommand placeOrderCommand);
    void onOrderUp(Message<String> message);
}
