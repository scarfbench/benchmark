package spring.examples.tutorial.standalone.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import spring.examples.tutorial.standalone.service.StandaloneService;

@RestController
public class GreetController {

    @Autowired
    private StandaloneService standaloneService;

    record GreetResponse(String message) {
    }

    @GetMapping("/greet")
    public ResponseEntity<GreetResponse> greet() {
        return ResponseEntity.ok(new GreetResponse(standaloneService.returnMessage()));
    }
}
