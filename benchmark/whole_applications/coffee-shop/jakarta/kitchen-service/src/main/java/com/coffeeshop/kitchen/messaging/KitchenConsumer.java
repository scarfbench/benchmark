package com.coffeeshop.kitchen.messaging;

import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.eclipse.microprofile.reactive.messaging.Outgoing;

import jakarta.json.Json;
import jakarta.json.JsonObject;

import java.io.StringReader;

@ApplicationScoped
public class KitchenConsumer {

    @Incoming("kitchen")          // channel that reads topic: kitchen-commands
    @Outgoing("order-updates")    // channel that writes topic: order-updates
    public String cookFood(String orderJson) {
        System.out.println("[kitchen] received: " + orderJson);
        // Parse the incoming order (so we can carry orderId across)
        JsonObject in = Json.createReader(new StringReader(orderJson)).readObject();

        // If original event had orderId, propagate it
        JsonObject out = Json.createObjectBuilder()
                .add("status", "READY")
                .add("from", "kitchen")
                .add("orderId", in.containsKey("orderId") ? in.getJsonNumber("orderId").longValue() : -1)
                .build();

        // This returns a real JSON object string: {"status":"READY","from":"kitchen","orderId":...}
        return out.toString();
    }
}
