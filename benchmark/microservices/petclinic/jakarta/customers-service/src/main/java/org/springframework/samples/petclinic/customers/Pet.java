package org.springframework.samples.petclinic.customers;

import jakarta.json.bind.annotation.JsonbTransient;

public class Pet {
    public Integer id;
    public String name;
    public String birthDate;
    public PetType type;

    @JsonbTransient
    public Integer ownerId;

    public Pet() {}

    public Pet(Integer id, String name, String birthDate, PetType type, Integer ownerId) {
        this.id = id;
        this.name = name;
        this.birthDate = birthDate;
        this.type = type;
        this.ownerId = ownerId;
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getBirthDate() { return birthDate; }
    public void setBirthDate(String birthDate) { this.birthDate = birthDate; }
    public PetType getType() { return type; }
    public void setType(PetType type) { this.type = type; }

    @JsonbTransient
    public Integer getOwnerId() { return ownerId; }
    public void setOwnerId(Integer ownerId) { this.ownerId = ownerId; }
}
