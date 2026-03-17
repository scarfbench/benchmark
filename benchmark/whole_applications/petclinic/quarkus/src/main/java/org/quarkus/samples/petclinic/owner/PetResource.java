package org.quarkus.samples.petclinic.owner;


import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import jakarta.ws.rs.FormParam;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.quarkus.samples.petclinic.system.TemplatesLocale;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import io.quarkus.qute.TemplateInstance;

@Path("/owners")
public class PetResource {
    
    @Inject
    TemplatesLocale templates;

    @Inject
    Validator validator;

    @GET
    @Path("{ownerId}/pets/new")
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance createTemplate(@PathParam("ownerId") Long ownerId) {
        return templates.createOrUpdatePetForm(Owner.findById(ownerId), null, PetType.listAll(), new HashMap<>());
    }

    @GET
    @Path("{ownerId}/pets/{petId}/edit")
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance editTemplate(@PathParam("ownerId") Long ownerId, @PathParam("petId") Long petId) {
        return templates.createOrUpdatePetForm(Owner.findById(ownerId), Pet.findById(petId), PetType.listAll(), new HashMap<>());
    }

    @POST
    @Path("{ownerId}/pets/new")
    @Produces(MediaType.TEXT_HTML)
    @Transactional
    public TemplateInstance processCreationForm(@PathParam("ownerId") Long ownerId, @FormParam("name") String name, @FormParam("birthDate") LocalDate birthDate, @FormParam("type") String type) {
        Owner owner = Owner.findById(ownerId);
        
        Pet pet = new Pet();
        pet.birthDate = birthDate;
        pet.name = name;
        pet.type = PetType.parse(type);


        final Set<ConstraintViolation<Pet>> violations = validator.validate(pet);
        final Map<String, String> errors = new HashMap<>();
        if (!violations.isEmpty()) {
            
            for (ConstraintViolation<Pet> violation : violations) {
                errors.put(violation.getPropertyPath().toString(), violation.getMessage());
            }

            return templates.createOrUpdatePetForm(Owner.findById(ownerId), null, PetType.listAll(), errors);

        } else {

            pet.persist();
            owner.addPet(pet);
            return templates.ownerDetails(owner);
        }
    }

    @POST
    @Path("{ownerId}/pets/{petId}/edit")
    @Produces(MediaType.TEXT_HTML)
    @Transactional
    public TemplateInstance processUpdateForm(@PathParam("ownerId") Long ownerId, @PathParam("petId") Long petId, @FormParam("name") String name, @FormParam("birthDate") LocalDate birthDate, @FormParam("type") String type) {
        Pet pet = new Pet();
        pet.id = petId;
        pet.birthDate = birthDate;
        pet.name = name;
        pet.type = PetType.parse(type);

        final Set<ConstraintViolation<Pet>> violations = validator.validate(pet);
        final Map<String, String> errors = new HashMap<>();
        if (!violations.isEmpty()) {
            
            for (ConstraintViolation<Pet> violation : violations) {
                errors.put(violation.getPropertyPath().toString(), violation.getMessage());
            }

            Pet oldPet = Pet.findById(petId);
            return templates.createOrUpdatePetForm(oldPet.owner, oldPet, PetType.listAll(), errors);

        } else {
            return templates.ownerDetails(pet.attach().owner);
        }
    }

}
