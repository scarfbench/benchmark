package quarkus.examples.tutorial;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class StandaloneBean {

    private static final String message = "Greetings!";

    public String returnMessage() {
        return message;
    }

}
