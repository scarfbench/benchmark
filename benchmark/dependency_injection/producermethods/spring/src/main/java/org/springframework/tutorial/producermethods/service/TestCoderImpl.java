package org.springframework.tutorial.producermethods.service;

import org.springframework.stereotype.Service;

/**
 * Coder implementation that does nothing but display the values of the arguments.
 */
@Service("testCoder")
public class TestCoderImpl implements Coder {

    /**
     * Returns a string that displays the values of the arguments.
     *
     * @param s the input string
     * @param tval the number of characters to shift
     * @return string displaying argument values
     */
    @Override
    public String codeString(String s, int tval) {
        return "input string is " + s + ", shift value is " + tval;
    }
}
