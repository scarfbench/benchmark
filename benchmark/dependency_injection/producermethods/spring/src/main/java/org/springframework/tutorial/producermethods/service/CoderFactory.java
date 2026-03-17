package org.springframework.tutorial.producermethods.service;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service
public class CoderFactory {

    public final static int TEST = 1;
    public final static int SHIFT = 2;

    private final Coder shiftCoder;
    private final Coder testCoder;

    public CoderFactory(@Qualifier("shiftCoder") Coder shiftCoder,
            @Qualifier("testCoder") Coder testCoder) {
        this.shiftCoder = shiftCoder;
        this.testCoder = testCoder;
    }

    public Coder getCoder(int coderType) {
        switch (coderType) {
            case TEST:
                return testCoder;
            case SHIFT:
            default:
                return shiftCoder;
        }
    }
}
