/*
 * Copyright (c), Eclipse Foundation, Inc. and its licensors.
 * SPDX-License-Identifier: BSD-3-Clause
 */
package quarkus.tutorial.websocket.messages;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/* Represents an information message, like
 * an user entering or leaving the chat */
public class InfoMessage extends Message {
    private String info;

    // No-arg constructor for Jackson
    public InfoMessage() {
    }

    @JsonCreator
    public InfoMessage(@JsonProperty("info") String info) {
        this.info = info;
    }

    public String getInfo() {
        return info;
    }

    public void setInfo(String info) {
        this.info = info;
    }

    /* For logging purposes */
    @Override
    public String toString() {
        return "[InfoMessage] " + info;
    }
}