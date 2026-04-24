package org.springframework.samples.petclinic.api.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.ArrayList;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class PetDetails {
    public Integer id;
    public String name;
    public String birthDate;
    public PetType type;
    public List<VisitDetails> visits = new ArrayList<>();

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getBirthDate() { return birthDate; }
    public void setBirthDate(String birthDate) { this.birthDate = birthDate; }
    public PetType getType() { return type; }
    public void setType(PetType type) { this.type = type; }
    public List<VisitDetails> getVisits() { return visits; }
    public void setVisits(List<VisitDetails> visits) { this.visits = visits != null ? visits : new ArrayList<>(); }
}
