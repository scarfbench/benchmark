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
    @Outgoing("order-updates")
    public Message<String> makeCoffee(String orderJson) {
        try {
            System.out.println("[barista] received: " + orderJson);
            // Parse whatever orders-service sent (JSON String)
            Map<String, Object> in = jsonb.fromJson(orderJson, Map.class);

            // Carry through orderId if present (original Quarkus did this)
            Object orderId = in.get("orderId");

            Map<String, Object> out = new HashMap<>();
            if (orderId != null) {
                out.put("orderId", orderId);
            }
            out.put("status", "READY");
            out.put("from", "barista");

            // Produce clean JSON (no backslashes)
            return Message.of(jsonb.toJson(out));

        } catch (Exception e) {
            System.err.println("[barista] parse/update error: " + e + " payload=" + orderJson);
            // Let the framework nack and surface the error rather than emitting malformed JSON
            throw e;
        }
    }
}
