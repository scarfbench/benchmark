/*
 * Copyright (c), Eclipse Foundation, Inc. and its licensors.
 * SPDX-License-Identifier: BSD-3-Clause
 */
package quarkus.tutorial.websocket.messages;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes({
    @JsonSubTypes.Type(value = ChatMessage.class, name = "chat"),
    @JsonSubTypes.Type(value = JoinMessage.class, name = "join"),
    @JsonSubTypes.Type(value = InfoMessage.class, name = "info"),
    @JsonSubTypes.Type(value = UsersMessage.class, name = "users")
})
public abstract class Message {
    // No fields or methods; subclasses define specific behavior
}