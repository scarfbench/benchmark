/*
 * Copyright (c), Eclipse Foundation, Inc. and its licensors.
 * SPDX-License-Identifier: BSD-3-Clause
 */
package quarkus.tutorial.websocket.messages;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/* Represents a join message for the chat */
public class JoinMessage extends Message {
    private String name;

    // No-arg constructor for Jackson
    public JoinMessage() {
    }

    @JsonCreator
    public JoinMessage(@JsonProperty("name") String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /* For logging purposes */
    @Override
    public String toString() {
        return "[JoinMessage] " + name;
    }
}