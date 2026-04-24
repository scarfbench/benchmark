package org.springframework.samples.petclinic.vets;

public class Specialty {
    public Integer id;
    public String name;

    public Specialty() {}

    public Specialty(Integer id, String name) {
        this.id = id;
        this.name = name;
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
}
