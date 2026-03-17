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
package quarkus.tutorial.mood;

import io.quarkus.runtime.ShutdownEvent;
import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Web application lifecycle listener.
 */
@ApplicationScoped
public class SimpleServletListener {

    static final Logger log =
            Logger.getLogger("mood.web.SimpleServletListener");

    
    void onStart(@Observes StartupEvent ev) {
        log.info("Context initialized");
    }

    void onStop(@Observes ShutdownEvent ev) {
        log.info("Context destroyed");
    }

    void attributeAdded(String name, Object value) {
        log.log(Level.INFO, "Attribute {0} has been added, with value: {1}",
                new Object[]{name, value});
    }

    void attributeRemoved(String name) {
        log.log(Level.INFO, "Attribute {0} has been removed", name);
    }

    void attributeReplaced(String name, Object value) {
        log.log(Level.INFO, "Attribute {0} has been replaced, with value: {1}",
                new Object[]{name, value});
    }

}
