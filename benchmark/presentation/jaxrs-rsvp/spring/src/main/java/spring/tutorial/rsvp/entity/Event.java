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
package spring.tutorial.rsvp.entity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlElementWrapper;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlTransient;
import com.fasterxml.jackson.annotation.JsonManagedReference;


@NamedQuery(
    name = "rsvp.entity.Event.getAllUpcomingEvents",
    query = "SELECT e FROM Event e"
)
@XmlRootElement(name = "Event")
@XmlAccessorType(XmlAccessType.FIELD)
@Entity
public class Event implements Serializable {

    private static final long serialVersionUID = -5584404843358199527L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    protected String name;

    @ManyToOne
    private Person owner;

    protected String location;

    @Temporal(TemporalType.DATE)
    private Date eventDate;

    @ManyToMany
    @XmlElementWrapper(name = "invitees")
    @XmlElement(name = "person")
    protected List<Person> invitees;

    @OneToMany(mappedBy = "event")
    @XmlElementWrapper(name = "responses")
    @XmlElement(name = "response")
    @JsonManagedReference
    private List<Response> responses;

    public Event() {
        this.invitees = new ArrayList<>();
        this.responses = new ArrayList<>();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Person getOwner() {
        return owner;
    }

    public void setOwner(Person owner) {
        this.owner = owner;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public Date getEventDate() {
        return eventDate;
    }

    public void setEventDate(Date eventDate) {
        this.eventDate = eventDate;
    }

    @XmlTransient
    public List<Person> getInvitees() {
        return invitees;
    }

    public void setInvitees(List<Person> invitees) {
        this.invitees = invitees;
    }

    @XmlTransient
    public List<Response> getResponses() {
        return responses;
    }

    public void setResponses(List<Response> responses) {
        this.responses = responses;
    }


    @Override
    public int hashCode() {
        return (id != null ? id.hashCode() : 0);
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Event)) return false;
        Event other = (Event) obj;
        if (this.id == null && other.id != null) return false;
        return this.id == null || this.id.equals(other.id);
    }

    @Override
    public String toString() {
        return "rsvp.entity.Event[id=" + id + "]";
    }
}
