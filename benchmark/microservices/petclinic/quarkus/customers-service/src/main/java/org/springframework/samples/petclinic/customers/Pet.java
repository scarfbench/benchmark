package org.springframework.samples.petclinic.customers;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;

import java.util.Date;

@Entity
@Table(name = "pets")
public class Pet extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Integer id;

    @Column(name = "name")
    public String name;

    @Column(name = "birth_date")
    @Temporal(TemporalType.DATE)
    public Date birthDate;

    @ManyToOne
    @JoinColumn(name = "type_id")
    public PetType type;

    @ManyToOne
    @JoinColumn(name = "owner_id")
    @JsonIgnore
    public Owner owner;
}
