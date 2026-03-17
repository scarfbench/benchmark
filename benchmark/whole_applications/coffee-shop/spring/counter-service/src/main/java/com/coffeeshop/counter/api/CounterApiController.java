package com.coffeeshop.counter.api;

import com.coffeeshop.common.commands.PlaceOrderCommand;

import jakarta.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class CounterApiController {

    private static final Logger log = LoggerFactory.getLogger(CounterApiController.class);

    private final OrderService orderService;

    public CounterApiController(OrderService orderService) {
        this.orderService = orderService;
    }

    // POST /api/order  (JSON -> PlaceOrderCommand)
    @PostMapping(path = "/order", consumes = "application/json")
    public ResponseEntity<Void> placeOrder(@Valid @RequestBody PlaceOrderCommand command) {
        log.info("POST /api/order: {}", command);
        orderService.onOrderIn(command);
        return ResponseEntity.accepted().build();
    }

    // POST /api/order-up  (JSON as raw string -> Message<String>)
    // Sends the raw JSON to the service, which will parse it (like Quarkus did).
    @PostMapping(path = "/order-up", consumes = "application/json")
    public ResponseEntity<Void> orderUpJson(@RequestBody String json) {
        log.info("POST /api/order-up (json): {}", json);
        Message<String> msg = MessageBuilder.withPayload(json).build();
        orderService.onOrderUp(msg);
        return ResponseEntity.accepted().build();
    }

    // OPTIONAL: text/plain variant if you want to post a plain JSON string
    @PostMapping(path = "/order-up", consumes = "text/plain")
    public ResponseEntity<Void> orderUpText(@RequestBody String body) {
        log.info("POST /api/order-up (text): {}", body);
        Message<String> msg = MessageBuilder.withPayload(body).build();
        orderService.onOrderUp(msg);
        return ResponseEntity.accepted().build();
    }

    // Handy browser check
    @GetMapping("/ping")
    public String ping() { return "counter-service is running"; }
}
