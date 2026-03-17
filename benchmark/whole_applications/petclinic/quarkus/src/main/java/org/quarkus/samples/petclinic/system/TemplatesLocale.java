package org.quarkus.samples.petclinic.system;

import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import jakarta.enterprise.context.ApplicationScoped;
import org.quarkus.samples.petclinic.owner.Owner;
import org.quarkus.samples.petclinic.owner.Pet;
import org.quarkus.samples.petclinic.owner.PetType;
import org.quarkus.samples.petclinic.vet.Vet;
import org.quarkus.samples.petclinic.visit.Visit;

import io.quarkus.qute.TemplateInstance;

@ApplicationScoped
public class TemplatesLocale {
    
    public TemplateInstance welcome() {
        return Templates.welcome().setAttribute("locale", getConfiguredLocale());
    }

    public TemplateInstance error(String message) {
        return Templates.error(message).setAttribute("locale", getConfiguredLocale());
    }

    public TemplateInstance vetList(List<Vet> vets) {
        return Templates.vetList(vets).setAttribute("locale", getConfiguredLocale());
    }

    public TemplateInstance findOwners(List<String> errors) {
        return Templates.findOwners(errors).setAttribute("locale", getConfiguredLocale());
    }

    public TemplateInstance ownerDetails(Owner owner) {
        return Templates.ownerDetails(owner).setAttribute("locale", getConfiguredLocale());
    }

    public TemplateInstance createOrUpdateOwnerForm(Owner owner, Map<String, String> errors) {
        return Templates.createOrUpdateOwnerForm(owner, errors).setAttribute("locale", getConfiguredLocale());
    }

    public TemplateInstance ownersList(Collection<Owner> owners) {
        return Templates.ownersList(owners).setAttribute("locale", getConfiguredLocale());
    }

    public TemplateInstance createOrUpdatePetForm(Owner owner, Pet pet, List<PetType> petTypes, Map<String, String> errors) {
        return Templates.createOrUpdatePetForm(owner, pet, petTypes, errors).setAttribute("locale", getConfiguredLocale());
    }

    public TemplateInstance createOrUpdateVisitForm(Pet pet, Visit visit, Map<String, String> errors) {
        return Templates.createOrUpdateVisitForm(pet, visit, errors).setAttribute("locale", getConfiguredLocale());
    }

    protected Locale getConfiguredLocale() {
        return  Locale.getDefault();
    }

}
