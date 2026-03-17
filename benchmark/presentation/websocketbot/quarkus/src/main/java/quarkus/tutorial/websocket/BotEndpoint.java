/*
 * Copyright (c), Eclipse Foundation, Inc. and its licensors.
 * SPDX-License-License: BSD-3-Clause
 */
package quarkus.tutorial.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.inject.Inject;
import jakarta.websocket.*;
import jakarta.websocket.server.ServerEndpoint;
import quarkus.tutorial.websocket.messages.ChatMessage;
import quarkus.tutorial.websocket.messages.InfoMessage;
import quarkus.tutorial.websocket.messages.JoinMessage;
import quarkus.tutorial.websocket.messages.Message;
import quarkus.tutorial.websocket.messages.UsersMessage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

@ServerEndpoint("/websocketbot")
public class BotEndpoint {
    private static final Logger logger = Logger.getLogger("BotEndpoint");

    @Inject
    BotBean botbean;

    @Inject
    ObjectMapper mapper;

    @OnOpen
    public void openConnection(Session session) {
        logger.log(Level.INFO, "Connection opened.");
    }

    @OnMessage
    public void message(Session session, String rawMessage) {
        try {
            // Validate incoming message
            if (!willDecode(rawMessage)) {
                logger.log(Level.INFO, "Invalid message: {0}", rawMessage);
                return;
            }

            // Decode incoming message
            Message msg = mapper.readValue(rawMessage, Message.class);
            logger.log(Level.INFO, "Received: {0}", msg.toString());

            if (msg instanceof JoinMessage) {
                JoinMessage jmsg = (JoinMessage) msg;
                session.getUserProperties().put("name", jmsg.getName());
                session.getUserProperties().put("active", true);
                sendAll(session, new InfoMessage(jmsg.getName() + " has joined the chat"));
                sendAll(session, new ChatMessage("Duke", jmsg.getName(), "Hi there!!"));
                sendAll(session, new UsersMessage(getUserList(session)));

            } else if (msg instanceof ChatMessage) {
                ChatMessage cmsg = (ChatMessage) msg;
                sendAll(session, cmsg);
                if (cmsg.getTarget().equals("Duke")) {
                    String resp = botbean.respond(cmsg.getMessage());
                    sendAll(session, new ChatMessage("Duke", cmsg.getName(), resp));
                }
            }
        } catch (Exception e) {
            logger.log(Level.INFO, "Error processing message: {0}", e.toString());
        }
    }

    @OnClose
    public void closedConnection(Session session) {
        session.getUserProperties().put("active", false);
        if (session.getUserProperties().containsKey("name")) {
            String name = (String) session.getUserProperties().get("name");
            sendAll(session, new InfoMessage(name + " has left the chat"));
            sendAll(session, new UsersMessage(getUserList(session)));
        }
        logger.log(Level.INFO, "Connection closed.");
    }

    @OnError
    public void error(Session session, Throwable t) {
        logger.log(Level.INFO, "Connection error: {0}", t.toString());
    }

    public synchronized void sendAll(Session session, Object msg) {
        try {
            String json = mapper.writeValueAsString(msg);
            for (Session s : session.getOpenSessions()) {
                if (s.isOpen()) {
                    s.getAsyncRemote().sendText(json);
                    logger.log(Level.INFO, "Sent: {0}", msg.toString());
                }
            }
        } catch (Exception e) {
            logger.log(Level.INFO, "Error sending message: {0}", e.toString());
        }
    }

    public List<String> getUserList(Session session) {
        List<String> users = new ArrayList<>();
        users.add("Duke");
        for (Session s : session.getOpenSessions()) {
            if (s.isOpen() && (boolean) s.getUserProperties().get("active")) {
                users.add((String) s.getUserProperties().get("name"));
            }
        }
        return users;
    }

    private boolean willDecode(String string) {
        try {
            // Parse JSON into a Map
            Map<String, Object> jsonMap = mapper.readValue(string, Map.class);
            Map<String, String> messageMap = new HashMap<>();
            jsonMap.forEach((key, value) -> messageMap.put(key, String.valueOf(value)));

            // Check required fields based on type
            if (!messageMap.containsKey("type")) {
                return false;
            }
            Set<String> keys = messageMap.keySet();
            switch (messageMap.get("type")) {
                case "join":
                    return messageMap.containsKey("name");
                case "chat":
                    return keys.containsAll(Arrays.asList("name", "target", "message"));
                default:
                    return false;
            }
        } catch (Exception e) {
            return false;
        }
    }
}