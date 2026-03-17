package org.springframework.tutorial.producermethods.dto;

import org.springframework.tutorial.producermethods.service.CoderFactory;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public class CoderForm {

    private String inputString;

    @Max(26)
    @Min(0)
    @NotNull
    private int transVal;

    private String codedString;
    private int coderType = CoderFactory.SHIFT;

    public String getInputString() {
        return inputString;
    }

    public void setInputString(String inputString) {
        this.inputString = inputString;
    }

    public int getTransVal() {
        return transVal;
    }

    public void setTransVal(int transVal) {
        this.transVal = transVal;
    }

    public String getCodedString() {
        return codedString;
    }

    public void setCodedString(String codedString) {
        this.codedString = codedString;
    }

    public int getCoderType() {
        return coderType;
    }

    public void setCoderType(int coderType) {
        this.coderType = coderType;
    }

}
