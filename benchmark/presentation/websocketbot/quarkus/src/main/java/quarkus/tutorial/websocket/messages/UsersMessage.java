/*
 * Copyright (c), Eclipse Foundation, Inc. and its licensors.
 * SPDX-License-Identifier: BSD-3-Clause
 */
package quarkus.tutorial.websocket.messages;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/* Represents the list of users currently connected to the chat */
public class UsersMessage extends Message {
    private List<String> userlist;

    // No-arg constructor for Jackson
    public UsersMessage() {
    }

    @JsonCreator
    public UsersMessage(@JsonProperty("userlist") List<String> userlist) {
        this.userlist = userlist;
    }

    public List<String> getUserList() {
        return userlist;
    }

    public void setUserList(List<String> userlist) {
        this.userlist = userlist;
    }

    /* For logging purposes */
    @Override
    public String toString() {
        return "[UsersMessage] " + userlist.toString();
    }
}