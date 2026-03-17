package org.springframework.tutorial.guessnumber.dto;

import org.springframework.validation.BindingResult;

public class UserNumberBean {

    private int number;
    private Integer userNumber;
    private int minimum;
    private int maximum;
    private int remainingGuesses;

    public UserNumberBean(int number, int maxNumber) {
        reset(number, maxNumber);
    }

    public void reset(int number, int maxNumber) {
        this.minimum = 0;
        this.userNumber = 0;
        this.remainingGuesses = 10;
        this.maximum = maxNumber;
        this.number = number;
    }

    public void check() {
        if (userNumber > number) {
            maximum = userNumber - 1;
        } else if (userNumber < number) {
            minimum = userNumber + 1;
        }

        if (remainingGuesses > 0) {
            remainingGuesses--;
        }
    }

    public void validateNumberRange(BindingResult result) {
        if (userNumber < minimum || userNumber > maximum) {
            result.rejectValue("userNumber", "range", "Invalid guess");
        }
    }

    public int getMinimum() {
        return minimum;
    }

    public void setMinimum(int minimum) {
        this.minimum = minimum;
    }

    public int getMaximum() {
        return maximum;
    }

    public void setMaximum(int maximum) {
        this.maximum = maximum;
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    public int getRemainingGuesses() {
        return remainingGuesses;
    }

    public void setRemainingGuesses(int remainingGuesses) {
        this.remainingGuesses = remainingGuesses;
    }

    public Integer getUserNumber() {
        return userNumber;
    }

    public void setUserNumber(Integer userNumber) {
        this.userNumber = userNumber;
    }

}
