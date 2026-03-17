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
package quarkus.tutorial.rsvp.ejb;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.logging.Logger;

import jakarta.annotation.PostConstruct;
import jakarta.persistence.EntityManager;
import quarkus.tutorial.rsvp.entity.Event;
import quarkus.tutorial.rsvp.entity.Person;
import quarkus.tutorial.rsvp.entity.Response;
import quarkus.tutorial.rsvp.util.ResponseEnum;
import jakarta.inject.Inject;
import jakarta.enterprise.context.ApplicationScoped;
import io.quarkus.runtime.Startup;
import jakarta.transaction.Transactional;
import jakarta.enterprise.event.Observes;
import io.quarkus.runtime.StartupEvent;

/**
 *
 * @author ievans
 */
@Startup
@ApplicationScoped
public class ConfigBean {

    @Inject 
    EntityManager em;

    private static final Logger logger = Logger.getLogger("quarkus.tutorial.rsvp.ejb.ConfigBean");

    @Transactional
    void onStart(@Observes StartupEvent ev) {
        // create the event owner
        Person dad = new Person();
        dad.setFirstName("Father");
        dad.setLastName("OfJava");
        em.persist(dad);

        // create the event
        Event event = new Event();
        event.setName("Duke's Birthday Party");
        event.setLocation("Top of the Mark");
        Calendar cal = new GregorianCalendar(2010, Calendar.MAY, 23, 19, 0);
        event.setEventDate(cal.getTime());
        em.persist(event);

        // set the relationships
        dad.getOwnedEvents().add(event);
        dad.getEvents().add(event);
        event.setOwner(dad);
        event.getInvitees().add(dad);
        Response dadsResponse = new Response(event, dad, ResponseEnum.ATTENDING);
        em.persist(dadsResponse);
        event.getResponses().add(dadsResponse);

        // create some invitees
        Person duke = new Person();
        duke.setFirstName("Duke");
        duke.setLastName("OfJava");
        em.persist(duke);
        
        Person tux = new Person();
        tux.setFirstName("Tux");
        tux.setLastName("Penguin");
        em.persist(tux);

        // set the relationships
        event.getInvitees().add(duke);
        duke.getEvents().add(event);
        Response dukesResponse = new Response(event, duke);
        em.persist(dukesResponse);
        event.getResponses().add(dukesResponse);
        duke.getResponses().add(dukesResponse);

        event.getInvitees().add(tux);
        tux.getEvents().add(event);
        Response tuxsResponse = new Response(event, tux);
        em.persist(tuxsResponse);
        event.getResponses().add(tuxsResponse);
        tux.getResponses().add(tuxsResponse);

    }
    
}
