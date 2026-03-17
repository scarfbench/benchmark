package org.quarkus.samples.petclinic.visit;

import java.time.LocalDate;
import java.util.Collection;



import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotEmpty;
import jakarta.ws.rs.FormParam;

@Entity
@Table(name = "visits")
public class Visit extends PanacheEntity {
    
	@Column(name = "visit_date")
	@FormParam("date")
	public LocalDate date;

	@NotEmpty
	@Column(name = "description")
	@FormParam("description")
	public String description;

	@Column(name = "pet_id")
	public Long petId;

	public static Collection<Visit> findByPetId(Long petId) {
        return Visit.list("petId", petId);
    }
}
