package org.springframework.samples.petclinic.customers.web;

import org.springframework.samples.petclinic.customers.model.Pet;
import org.springframework.samples.petclinic.customers.model.PetType;

import java.util.Date;

public record PetDetails(long id,
                         String name,
                         String owner,
                         Date birthDate,
                         PetType type) {
    public PetDetails(Pet pet) {
        this(pet.getId(),
             pet.getName(),
             pet.getOwner().getFirstName() + " " + pet.getOwner().getLastName(),
             pet.getBirthDate(),
             pet.getType());
    }
}
