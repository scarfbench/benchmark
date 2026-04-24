package org.springframework.samples.petclinic.visits;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@ApplicationScoped
public class VisitsStore {

    private final Map<Integer, Visit> visits = new ConcurrentHashMap<>();
    private final AtomicInteger seq = new AtomicInteger(100);

    @PostConstruct
    public void init() {
        visits.put(1, new Visit(1, 7, "2013-01-01", "rabies shot"));
        visits.put(2, new Visit(2, 8, "2013-01-02", "rabies shot"));
        visits.put(3, new Visit(3, 8, "2013-01-03", "neutered"));
        visits.put(4, new Visit(4, 7, "2013-01-04", "spayed"));
        visits.put(5, new Visit(5, 1, "2013-02-01", "checkup"));
        visits.put(6, new Visit(6, 1, "2013-03-10", "vaccination"));
    }

    public List<Visit> findByPetId(int petId) {
        List<Visit> out = new ArrayList<>();
        for (Visit v : visits.values()) {
            if (v.petId != null && v.petId == petId) {
                out.add(v);
            }
        }
        return out;
    }

    public List<Visit> findByPetIdIn(Collection<Integer> petIds) {
        List<Visit> out = new ArrayList<>();
        for (Visit v : visits.values()) {
            if (v.petId != null && petIds.contains(v.petId)) {
                out.add(v);
            }
        }
        return out;
    }

    public Visit create(int petId, Visit visit) {
        int id = seq.incrementAndGet();
        visit.id = id;
        visit.petId = petId;
        visits.put(id, visit);
        return visit;
    }
}
