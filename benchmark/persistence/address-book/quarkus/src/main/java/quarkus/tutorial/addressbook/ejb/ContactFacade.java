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
package quarkus.tutorial.addressbook.ejb;

import jakarta.persistence.EntityManager;
import quarkus.tutorial.addressbook.entity.Contact;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import jakarta.inject.Inject;

/**
 *
 * @author ian
 */

@ApplicationScoped
@Transactional
public class ContactFacade extends AbstractFacade<Contact> {
    @Inject EntityManager em;

    @Override protected EntityManager getEntityManager() { return em; }

    public ContactFacade() {
        super(Contact.class);
    }

}

