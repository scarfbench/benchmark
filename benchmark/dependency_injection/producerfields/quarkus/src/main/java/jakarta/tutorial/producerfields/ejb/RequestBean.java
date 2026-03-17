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
package jakarta.tutorial.producerfields.ejb;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import jakarta.tutorial.producerfields.db.UserDatabase;
import jakarta.tutorial.producerfields.entity.ToDo;

@ApplicationScoped
public class RequestBean {

    @Inject
    @UserDatabase
    EntityManager em;

    @Transactional
    public ToDo createToDo(String inputString) {
        ToDo toDo;
        Date currentTime = Calendar.getInstance().getTime();

        try {
            toDo = new ToDo();
            toDo.setTaskText(inputString);
            toDo.setTimeCreated(currentTime);
            em.persist(toDo);
            return toDo;
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    @Transactional
    public List<ToDo> getToDos() {
        try {
            List<ToDo> toDos = em.createQuery(
                    "SELECT t FROM ToDo t ORDER BY t.timeCreated", ToDo.class)
                    .getResultList();
            return toDos;
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }
}
