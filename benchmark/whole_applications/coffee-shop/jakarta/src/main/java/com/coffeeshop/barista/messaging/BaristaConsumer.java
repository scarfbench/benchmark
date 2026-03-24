package com.coffeeshop.barista.messaging;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.json.bind.Jsonb;
import jakarta.json.bind.JsonbBuilder;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.eclipse.microprofile.reactive.messaging.Outgoing;
import org.eclipse.microprofile.reactive.messaging.Message;

import java.util.HashMap;
import java.util.Map;

@ApplicationScoped
public class BaristaConsumer {

    private final Jsonb jsonb = JsonbBuilder.create();

    // Consumes commands from orders-service (topic: barista-commands)
    @Incoming("barista")
    // Publishes order status updates (topic: order-updates)
    @Outgoing("barista-order-updates")
    public Message<String> makeCoffee(String orderJson) {
        try {
            System.out.println("[barista] received: " + orderJson);
            Map<String, Object> in = jsonb.fromJson(orderJson, Map.class);

            Object orderId = in.get("orderId");

            Map<String, Object> out = new HashMap<>();
            if (orderId != null) {
                out.put("orderId", orderId);
            }
            out.put("status", "READY");
            out.put("from", "barista");

            return Message.of(jsonb.toJson(out));

        } catch (Exception e) {
            System.err.println("[barista] parse/update error: " + e + " payload=" + orderJson);
            throw e;
        }
    }
}
