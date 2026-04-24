package org.springframework.samples.petclinic.vets;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@ApplicationScoped
public class VetsStore {

    private final Map<Integer, Vet> vets = new ConcurrentHashMap<>();
    private final Map<Integer, Specialty> specialties = new ConcurrentHashMap<>();

    @PostConstruct
    public void init() {
        specialties.put(1, new Specialty(1, "radiology"));
        specialties.put(2, new Specialty(2, "surgery"));
        specialties.put(3, new Specialty(3, "dentistry"));

        Vet v1 = new Vet(1, "James", "Carter");
        Vet v2 = new Vet(2, "Helen", "Leary");
        v2.specialties.add(specialties.get(1));
        Vet v3 = new Vet(3, "Linda", "Douglas");
        v3.specialties.add(specialties.get(2));
        v3.specialties.add(specialties.get(3));
        Vet v4 = new Vet(4, "Rafael", "Ortega");
        v4.specialties.add(specialties.get(2));
        Vet v5 = new Vet(5, "Henry", "Stevens");
        v5.specialties.add(specialties.get(1));
        Vet v6 = new Vet(6, "Sharon", "Jenkins");

        vets.put(1, v1);
        vets.put(2, v2);
        vets.put(3, v3);
        vets.put(4, v4);
        vets.put(5, v5);
        vets.put(6, v6);
    }

    public List<Vet> findAll() {
        List<Vet> all = new ArrayList<>(vets.values());
        all.sort(Comparator.comparing(v -> v.id));
        return all;
    }
}
