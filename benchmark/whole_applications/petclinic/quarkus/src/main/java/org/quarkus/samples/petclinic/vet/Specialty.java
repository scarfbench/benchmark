package org.quarkus.samples.petclinic.vet;


import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "specialties")
public class Specialty extends PanacheEntity {

    @Column(name = "name")
    public String name;

    @Override
    public String toString() {
        return "Specialty [name=" + name + "]";
    }

}
