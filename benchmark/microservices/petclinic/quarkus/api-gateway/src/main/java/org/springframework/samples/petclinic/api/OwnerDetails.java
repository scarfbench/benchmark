package org.springframework.samples.petclinic.api;

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

    @JsonIgnore
    public List<Integer> getPetIds() {
        List<Integer> out = new ArrayList<>();
        for (PetDetails p : pets) {
            if (p != null && p.id != null) out.add(p.id);
        }
        return out;
    }
}
