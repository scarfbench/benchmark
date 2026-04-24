package org.springframework.samples.petclinic.customers;

public class PetType {
    public Integer id;
    public String name;

    public PetType() {}

    public PetType(Integer id, String name) {
        this.id = id;
        this.name = name;
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
}
