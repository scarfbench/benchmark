/*
 * Copyright (c), Eclipse Foundation, Inc. and its licensors.
 *
 * All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v1.0, which is available at
 * https://www.eclipse.org/org/documents/edl-v10.php
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */
package spring.tutorial.web.websocketbot;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.springframework.beans.factory.annotation.Autowired;

import jakarta.websocket.EncodeException;
import jakarta.websocket.OnClose;
import jakarta.websocket.OnError;
import jakarta.websocket.OnMessage;
import jakarta.websocket.OnOpen;
import jakarta.websocket.Session;
import jakarta.websocket.server.ServerEndpoint;

import spring.tutorial.web.websocketbot.config.SpringEndpointConfigurator;
import spring.tutorial.web.websocketbot.decoders.MessageDecoder;
import spring.tutorial.web.websocketbot.encoders.ChatMessageEncoder;
import spring.tutorial.web.websocketbot.encoders.InfoMessageEncoder;
import spring.tutorial.web.websocketbot.encoders.JoinMessageEncoder;
import spring.tutorial.web.websocketbot.encoders.UsersMessageEncoder;
import spring.tutorial.web.websocketbot.messages.ChatMessage;
import spring.tutorial.web.websocketbot.messages.InfoMessage;
import spring.tutorial.web.websocketbot.messages.JoinMessage;
import spring.tutorial.web.websocketbot.messages.Message;
import spring.tutorial.web.websocketbot.messages.UsersMessage;
import spring.tutorial.web.websocketbot.service.BotService;
import org.springframework.stereotype.Component;


@Component
@ServerEndpoint(
        value = "/websocketbot",
        decoders = { MessageDecoder.class },
        encoders = { JoinMessageEncoder.class, ChatMessageEncoder.class,
                     InfoMessageEncoder.class, UsersMessageEncoder.class },
        configurator = SpringEndpointConfigurator.class
)
public class BotEndpoint {
    private static final Logger logger = Logger.getLogger("BotEndpoint");

    @Autowired
    private BotService botbean;

    @Autowired
    private Executor executor;

    @OnOpen
    public void openConnection(Session session) {
        logger.log(Level.INFO, "Connection opened.");
    }

    @OnMessage
    public void message(final Session session, Message msg) {
        logger.log(Level.INFO, "Received: {0}", msg.toString());

        if (msg instanceof JoinMessage) {
            JoinMessage jmsg = (JoinMessage) msg;
            session.getUserProperties().put("name", jmsg.getName());
            session.getUserProperties().put("active", true);
            logger.log(Level.INFO, "Received: {0}", jmsg.toString());
            sendAll(session, new InfoMessage(jmsg.getName() +
                    " has joined the chat"));
            sendAll(session, new ChatMessage("Duke", jmsg.getName(),
                    "Hi there!!"));
            sendAll(session, new UsersMessage(this.getUserList(session)));

        } else if (msg instanceof ChatMessage) {
            final ChatMessage cmsg = (ChatMessage) msg;
            logger.log(Level.INFO, "Received: {0}", cmsg.toString());
            sendAll(session, cmsg);
            if (cmsg.getTarget().compareTo("Duke") == 0) {
                executor.execute(new Runnable() {
                    @Override
                    public void run() {
                        String resp = botbean.respond(cmsg.getMessage());
                        sendAll(session, new ChatMessage("Duke",
                                cmsg.getName(), resp));
                    }
                });
            }
        }
    }

    @OnClose
    public void closedConnection(Session session) {
        session.getUserProperties().put("active", false);
        if (session.getUserProperties().containsKey("name")) {
            String name = session.getUserProperties().get("name").toString();
            sendAllExcept(session, new InfoMessage(name + " has left the chat"));
            sendAllExcept(session, new UsersMessage(this.getUserList(session)));
        }
        logger.log(Level.INFO, "Connection closed.");
    }

    @OnError
    public void error(Session session, Throwable t) {
        logger.log(Level.INFO, "Connection error ({0})", t.toString());
    }

    public synchronized void sendAll(Session session, Object msg) {
        try {
            for (Session s : session.getOpenSessions()) {
                if (s.isOpen()) {
                    s.getBasicRemote().sendObject(msg);
                    logger.log(Level.INFO, "Sent: {0}", msg.toString());
                }
            }
        } catch (IllegalStateException | IOException | EncodeException e) {
            logger.log(Level.INFO, e.toString());
        }
    }

    private synchronized void sendAllExcept(Session exclude, Object msg) {
        for (Session s : exclude.getOpenSessions()) {
            if (!s.isOpen() || s.equals(exclude)) 
                continue; 
            try {
                s.getBasicRemote().sendObject(msg);
                logger.log(Level.INFO, "Sent: {0}", msg.toString());
            } catch (IllegalStateException | IOException | EncodeException e) {
                logger.log(Level.INFO, e.toString());
            }
        }
    }

    public List<String> getUserList(Session session) {
        List<String> users = new ArrayList<>();
        users.add("Duke");
        for (Session s : session.getOpenSessions()) {
            if (s.isOpen() && Boolean.TRUE.equals(s.getUserProperties().get("active")))
                users.add(s.getUserProperties().get("name").toString());
        }
        return users;
    }
}
