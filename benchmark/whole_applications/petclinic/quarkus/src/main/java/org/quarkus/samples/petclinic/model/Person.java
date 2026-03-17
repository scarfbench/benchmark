package org.quarkus.samples.petclinic.model;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import jakarta.validation.constraints.NotEmpty;
import jakarta.ws.rs.FormParam;


@MappedSuperclass
public class Person extends PanacheEntity {
    
    @Column(name = "first_name")
    @NotEmpty
    @FormParam("firstName")
    public String firstName;

    @Column(name = "last_name")
    @NotEmpty
    @FormParam("lastName")
    public String lastName;

    @Override
    public String toString() {
        return "Person [firstName=" + firstName + ", lastName=" + lastName + "]";
    }

}
