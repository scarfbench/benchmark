/*
 * Copyright (c), Eclipse Foundation, Inc. and its licensors.
 * SPDX-License-Identifier: BSD-3-Clause
 */
package quarkus.tutorial.websocket.messages;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/* Represents a chat message */
public class ChatMessage extends Message {
    private String name;
    private String target;
    private String message;

    // No-arg constructor for Jackson
    public ChatMessage() {
    }

    @JsonCreator
    public ChatMessage(@JsonProperty("name") String name,
                       @JsonProperty("target") String target,
                       @JsonProperty("message") String message) {
        this.name = name;
        this.target = target;
        this.message = message;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    /* For logging purposes */
    @Override
    public String toString() {
        return "[ChatMessage] " + name + "-" + target + "-" + message;
    }
}