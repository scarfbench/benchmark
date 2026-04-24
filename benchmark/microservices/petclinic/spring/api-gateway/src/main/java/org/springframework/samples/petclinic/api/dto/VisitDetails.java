package org.springframework.samples.petclinic.api.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class VisitDetails {
    public Integer id;
    public Integer petId;
    public String date;
    public String description;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public Integer getPetId() { return petId; }
    public void setPetId(Integer petId) { this.petId = petId; }
    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}
