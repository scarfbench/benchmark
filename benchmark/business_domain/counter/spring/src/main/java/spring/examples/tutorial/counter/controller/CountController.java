package spring.examples.tutorial.counter.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import spring.examples.tutorial.counter.service.CounterService;

@Controller
public class CountController {

    private final CounterService counterService;

    @Autowired
    public CountController(CounterService counterService) {
        this.counterService = counterService;
    }

    @GetMapping("/")
    public String index(Model model) {
        int hitCount = counterService.getHits();
        model.addAttribute("hitCount", hitCount);
        return "index";
    }
}
