package org.springframework.tutorial.decorators.service;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

@Service
@Primary
public class CoderDecorator implements Coder {

    private final Coder delegate;

    public CoderDecorator(@Qualifier("baseCoder") Coder delegate) {
        this.delegate = delegate;
    }

    @Override
    public String codeString(String s, int tval) {
        int len = s.length();

        return "\"" + s + "\" becomes " + "\"" + delegate.codeString(s, tval)
                + "\", " + len + " characters in length";
    }
}
