package org.springframework.samples.petclinic.visits.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;

import java.util.Date;

@Entity
@Table(name = "visits")
public class Visit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "visit_date")
    @Temporal(TemporalType.TIMESTAMP)
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date date = new Date();

    @Column(name = "description")
    private String description;

    @Column(name = "pet_id")
    private int petId;

    public Integer getId() { return id; }
    public Date getDate() { return date; }
    public String getDescription() { return description; }
    public int getPetId() { return petId; }

    public void setId(Integer id) { this.id = id; }
    public void setDate(Date date) { this.date = date; }
    public void setDescription(String description) { this.description = description; }
    public void setPetId(int petId) { this.petId = petId; }
}
