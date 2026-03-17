package spring.examples.tutorial.standalone.service;

import org.springframework.stereotype.Service;

@Service
public class StandaloneService {

    private static final String message = "Greetings!";

    public String returnMessage() {
        return message;
    }

}
