package spring.tutorial.web.servlet;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class Greeting {

    @GetMapping("/greeting")
    public String greet(@RequestParam String name) {
        return "Hello, " + name + "!";
    }
}
