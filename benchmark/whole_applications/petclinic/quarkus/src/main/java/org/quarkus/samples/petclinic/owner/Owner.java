package org.quarkus.samples.petclinic.owner;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotEmpty;
import jakarta.ws.rs.FormParam;
import org.quarkus.samples.petclinic.model.Person;

@Entity
@Table(name = "owners")
public class Owner extends Person {

    @Column(name = "address")
    @NotEmpty
    @FormParam("address")
    public String address;

    @Column(name = "city")
    @NotEmpty
    @FormParam("city")
    public String city;

    @Column(name = "telephone")
    @NotEmpty
    @Digits(fraction = 0, integer = 10)
    @FormParam("telephone")
    public String telephone;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "owner", fetch = FetchType.EAGER) // it is eager but probably it could be changed to lazy and make some lazy load before rendering template but we might end up in similar performance
    public Set<Pet> pets;

    public static Collection<Owner> findByLastName(String name) {
        return Owner.list("lastName", name);
    }

    public Owner attach() {
        return getEntityManager().merge(this);
    }

    @Override
    public String toString() {
        return "Owner [address=" + address + ", city=" + city + ", pets=" + pets + ", telephone=" + telephone + "]";
    }

    public void addPet(Pet pet) {
        getPetsInternal().add(pet);
        pet.owner = this;
    }

    protected Set<Pet> getPetsInternal() {
		if (this.pets == null) {
			this.pets = new HashSet<>();
		}
		return this.pets;
	}

}
