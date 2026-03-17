package org.quarkus.samples.petclinic.owner;


import jakarta.ws.rs.FormParam;

public class OwnerForm {
    
    @FormParam("firstName")
    public String firstName;

    @FormParam("lastName")
    public String lastName;

    @Override
    public String toString() {
        return "OwnerForm [firstName=" + firstName + ", lastName=" + lastName + "]";
    }

}
