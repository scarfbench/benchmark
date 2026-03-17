package org.springframework.tutorial.simplegreeting.service;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.tutorial.simplegreeting.Informal;

@Service
@Scope("prototype") // just to be similar to @Dependent
@Informal
public class InformalGreeting extends Greeting {

    @Override
    public String greet(String name) {
        return "Hi, " + name + "!";
    }
}
