package org.springframework.samples.petclinic.visits;

public class Visit {
    public Integer id;
    public Integer petId;
    public String date;
    public String description;

    public Visit() {}

    public Visit(Integer id, Integer petId, String date, String description) {
        this.id = id;
        this.petId = petId;
        this.date = date;
        this.description = description;
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public Integer getPetId() { return petId; }
    public void setPetId(Integer petId) { this.petId = petId; }
    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}
