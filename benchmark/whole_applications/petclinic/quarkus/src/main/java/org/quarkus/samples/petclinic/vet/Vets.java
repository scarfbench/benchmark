package org.quarkus.samples.petclinic.vet;

import java.util.ArrayList;
import java.util.List;

public class Vets {
    
    private List<Vet> vets;

	public List<Vet> getVetList() {
		if (vets == null) {
			vets = new ArrayList<>();
		}
		return vets;
	}

}
