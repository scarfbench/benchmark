package org.quarkus.samples.petclinic.owner;

import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import jakarta.ws.rs.BeanParam;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.quarkus.samples.petclinic.system.TemplatesLocale;
import org.quarkus.samples.petclinic.visit.Visit;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import io.quarkus.qute.TemplateInstance;

@Path("/owners")
public class VisitResource {
    
    @Inject
    TemplatesLocale templates;

    @Inject
    Validator validator;

    @GET
    @Path("{ownerId}/pets/{petId}/visits/new")
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance createTemplate(@PathParam("petId") Long petId) {
        return templates.createOrUpdateVisitForm(Pet.findById(petId), null, new HashMap<>());
    }

    @POST
    @Path("{ownerId}/pets/{petId}/visits/new")
    @Produces(MediaType.TEXT_HTML)
    @Transactional
    public TemplateInstance processCreationForm(@PathParam("petId") Long petId,  @BeanParam Visit visit) {

        Pet pet = Pet.findById(petId);
        final Set<ConstraintViolation<Visit>> violations = validator.validate(visit);
        final Map<String, String> errors = new HashMap<>();
        if (!violations.isEmpty()) {
            
            for (ConstraintViolation<Visit> violation : violations) {
                errors.put(violation.getPropertyPath().toString(), violation.getMessage());
            }

            return templates.createOrUpdateVisitForm(pet, visit, errors);

        } else {
            
            visit.persist();

            pet.addVisit(visit);
            return templates.ownerDetails(pet.owner);
        }
    }

}
