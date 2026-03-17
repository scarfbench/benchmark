package org.springframework.tutorial.producerfields.dto;

import jakarta.validation.constraints.NotNull;

public class ToDoForm {

    @NotNull
    private String inputString;

    public String getInputString() {
        return inputString;
    }

    public void setInputString(String inputString) {
        this.inputString = inputString;
    }
}
