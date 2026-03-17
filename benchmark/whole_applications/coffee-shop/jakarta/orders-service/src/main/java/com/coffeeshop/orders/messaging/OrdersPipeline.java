package com.coffeeshop.orders.messaging;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.json.bind.Jsonb;
import jakarta.json.bind.JsonbBuilder;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;

@ApplicationScoped
public class OrdersPipeline {

  @Inject @Channel("barista")
  Emitter<String> barista;

  @Inject @Channel("kitchen")
  Emitter<String> kitchen;

  private static final Jsonb JSONB = JsonbBuilder.create();

  public void publishToBarista(OrderCommand cmd) {
    System.out.println("[orders] sending barista command: " + cmd);
    barista.send(JSONB.toJson(cmd));
  }

  public void publishToKitchen(OrderCommand cmd) {
    System.out.println("[orders] sending kitchen command: " + cmd);
    kitchen.send(JSONB.toJson(cmd));
  }

  // simple DTO for messages
  public record OrderCommand(String target, long orderId, String item, int quantity) {}
}
