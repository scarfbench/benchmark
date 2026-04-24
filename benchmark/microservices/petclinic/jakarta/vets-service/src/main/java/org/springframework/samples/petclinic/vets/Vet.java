package org.springframework.samples.petclinic.vets;

import java.util.ArrayList;
import java.util.List;

public class Vet {
    public Integer id;
    public String firstName;
    public String lastName;
    public List<Specialty> specialties = new ArrayList<>();

    public Vet() {}

    public Vet(Integer id, String firstName, String lastName) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    public List<Specialty> getSpecialties() { return specialties; }
    public void setSpecialties(List<Specialty> specialties) { this.specialties = specialties; }
}
