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
    @Outgoing("kitchen-order-updates")    // channel that writes topic: order-updates
    public String cookFood(String orderJson) {
        System.out.println("[kitchen] received: " + orderJson);
        JsonObject in = Json.createReader(new StringReader(orderJson)).readObject();

        JsonObject out = Json.createObjectBuilder()
                .add("status", "READY")
                .add("from", "kitchen")
                .add("orderId", in.containsKey("orderId") ? in.getJsonNumber("orderId").longValue() : -1)
                .build();

        return out.toString();
    }
}
