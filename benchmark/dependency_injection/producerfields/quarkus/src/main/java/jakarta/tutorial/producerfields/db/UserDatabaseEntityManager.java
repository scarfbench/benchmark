/*
 * Copyright (c), Eclipse Foundation, Inc. and its licensors.
 *
 * All rights reserved.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse
 * Distribution License v1.0, which is available at
 * https://www.eclipse.org/org/documents/edl-v10.php
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */
package jakarta.tutorial.producerfields.db;

import jakarta.enterprise.inject.Produces;
import jakarta.inject.Singleton;
import jakarta.persistence.EntityManager;

@Singleton
public class UserDatabaseEntityManager {

    // field producer does not work
    @Produces
    @UserDatabase
    public EntityManager produceUserEntityManager(EntityManager em) {
        return em;
    }
}
