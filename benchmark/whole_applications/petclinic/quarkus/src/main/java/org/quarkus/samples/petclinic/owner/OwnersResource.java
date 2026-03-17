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
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import org.quarkus.samples.petclinic.system.TemplatesLocale;
import org.quarkus.samples.petclinic.visit.Visit;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import io.quarkus.qute.TemplateInstance;

@Path("/owners")
public class OwnersResource {

    @Inject
    TemplatesLocale templates;

    @Inject
    Validator validator;

    @GET
    @Path("/find")
    @Produces(MediaType.TEXT_HTML)
    /**
     * Renders the findOwners.html
     * 
     * @return
     */
    public TemplateInstance findTemplate() {
        return templates.findOwners(Collections.EMPTY_LIST);
    }

    @GET
    @Path("new")
    @Produces(MediaType.TEXT_HTML)
    /**
     * Renders the createOrUpdateOwnerForm.html
     * 
     * @return
     */
    public TemplateInstance createTemplate() {
        return templates.createOrUpdateOwnerForm(null, new HashMap<>());
    }

    @GET
    @Path("{ownerId}/edit")
    @Produces(MediaType.TEXT_HTML)
    /**
     * Renders the createOrUpdateOwnerForm.html
     * 
     * @return
     */
    public TemplateInstance editTemplate(@PathParam("ownerId") Long ownerId) {
        return templates.createOrUpdateOwnerForm(Owner.findById(ownerId), new  HashMap<>());
    }

    @GET
    @Path("{ownerId}")
    @Produces(MediaType.TEXT_HTML)
    /**
     * Renders the createOrUpdateOwnerForm.html
     * 
     * @return
     */
    public TemplateInstance showOwner(@PathParam("ownerId") Long ownerId) {
        return templates.ownerDetails(Owner.findById(ownerId));
    }

    @POST
    @Path("new")
    @Produces(MediaType.TEXT_HTML)
    @Transactional
    /**
     * Renders the createOrUpdateOwnerForm.html
     * 
     * @return
     */
    public TemplateInstance processCreationForm(@BeanParam Owner owner) {
        final Set<ConstraintViolation<Owner>> violations = validator.validate(owner);
        final Map<String, String> errors = new HashMap<>();
        if (!violations.isEmpty()) {
            
            for (ConstraintViolation<Owner> violation : violations) {
                errors.put(violation.getPropertyPath().toString(), violation.getMessage());
            }

            return templates.createOrUpdateOwnerForm(null, errors);

        } else {
            owner.persist();
            return templates.ownerDetails(owner);
        }
    }
    
    @POST
    @Path("{ownerId}/edit")
    @Transactional
    @Produces(MediaType.TEXT_HTML)
    /**
     * Renders the createOrUpdateOwnerForm.html
     * 
     * @return
     */
    public TemplateInstance processUpdateOwnerForm(@BeanParam Owner owner, @PathParam("ownerId") Long ownerId) {
        final Set<ConstraintViolation<Owner>> violations = validator.validate(owner);
        final Map<String, String> errors = new HashMap<>();
        if (!violations.isEmpty()) {
            
            for (ConstraintViolation<Owner> violation : violations) {
                errors.put(violation.getPropertyPath().toString(), violation.getMessage());
            }

            return templates.createOrUpdateOwnerForm(owner, errors);

        } else {
            // We need to reattach the Owner object. Since method is transactional, the update occurs automatically.
            return templates.ownerDetails(owner.attach());
        }
    }

    @GET
    @Produces(MediaType.TEXT_HTML)
    /**
     * Process the findOwners form
     */
    public TemplateInstance processFindForm(@QueryParam("lastName") String lastName) {

        Collection<Owner> owners = null;

        // allow parameterless GET request for /owners to return all records
        if (lastName == null || "".equals(lastName.trim())) {
            owners = Owner.listAll(); // empty string signifies broadest possible search
        } else {
            owners = Owner.findByLastName(lastName);
        }

        // find owners by last name
        if (owners.isEmpty()) {
            // no owners found
            return templates.findOwners(Arrays.asList("lastName not found"));
        }
        if (owners.size() == 1) {
            // 1 owner found
            Owner owner = owners.iterator().next();
            return templates.ownerDetails(setVisits(owner));
        }
        
        return templates.ownersList(owners);

    }

    protected Owner setVisits(Owner owner) {
        for (Pet pet : owner.pets) {
            pet.setVisitsInternal(Visit.findByPetId(pet.id));
        }
        return owner;
    }

    @Path("/api/list")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<Owner> listOwners() {
        return Owner.listAll();
    }

}
