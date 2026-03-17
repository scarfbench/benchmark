package com.coffeeshop.orders.messaging;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.eclipse.microprofile.reactive.messaging.Incoming;

import com.coffeeshop.common.domain.OrderStatus;
import com.coffeeshop.orders.domain.OrderRepository;
import jakarta.json.bind.Jsonb;
import jakarta.json.bind.JsonbBuilder;
import jakarta.json.bind.JsonbConfig;
import java.util.Locale;

@ApplicationScoped
public class OrderUpdatesListener {

  private static final Jsonb JSONB = JsonbBuilder.create(new JsonbConfig());

  public static final class OrderUpdate {
    public Long orderId;
    public String status;
    public String from;
  }

  @Inject OrderRepository repo;

  @Incoming("order-updates")
  @Transactional
  public void apply(String payload) {
    try {
      if (payload == null) return;
      String json = payload.trim();

      // Unwrap if the payload is a *stringified* JSON object.
      int safety = 3;
      while (safety-- > 0
          && json.length() >= 2
          && json.charAt(0) == '"'
          && json.charAt(json.length() - 1) == '"') {
        json = JSONB.fromJson(json, String.class).trim();
      }

      OrderUpdate evt = JSONB.fromJson(json, OrderUpdate.class);
      if (evt == null || evt.status == null || evt.orderId == null) {
        // dev-friendly: ignore malformed/minimal events instead of killing the stream
        System.err.println("[order-updates] ignored event: " + json);
        return;
      }

      var entity = repo.find(evt.orderId);
      if (entity == null) return;
      entity.setStatus(OrderStatus.valueOf(evt.status.toUpperCase(Locale.ROOT)));

    } catch (Exception e) {
      // DEV: log and continue; do not rethrow or the stream will shut down.
      System.err.println("[order-updates] parse/update error: " + e + " payload=" + payload);
    }
  }
}

