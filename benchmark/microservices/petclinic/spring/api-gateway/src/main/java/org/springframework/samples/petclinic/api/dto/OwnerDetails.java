package org.springframework.samples.petclinic.api.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.ArrayList;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class OwnerDetails {
    public Integer id;
    public String firstName;
    public String lastName;
    public String address;
    public String city;
    public String telephone;
    public List<PetDetails> pets = new ArrayList<>();

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }
    public String getTelephone() { return telephone; }
    public void setTelephone(String telephone) { this.telephone = telephone; }
    public List<PetDetails> getPets() { return pets; }
    public void setPets(List<PetDetails> pets) { this.pets = pets != null ? pets : new ArrayList<>(); }

    @JsonIgnore
    public List<Integer> getPetIds() {
        return pets.stream().map(p -> p.id).toList();
    }
}
