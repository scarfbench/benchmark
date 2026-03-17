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
package jakarta.tutorial.producerfields.db;

import jakarta.ejb.Singleton;
import jakarta.enterprise.inject.Produces;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

@Singleton
public class UserDatabaseEntityManager {

    // Inject the container-managed EntityManager and expose it via a producer
    // method.
    @PersistenceContext
    private EntityManager em;

    @Produces
    @UserDatabase
    public EntityManager produceUserEntityManager() {
        return em;
    }
}
