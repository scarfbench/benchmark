package org.springframework.tutorial.simplegreeting.service;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

@Service
@Scope("prototype") // just to be similar to @Dependent
public class Greeting {
    public String greet(String name) {
        return "Hello, " + name + ".";
    }
}
