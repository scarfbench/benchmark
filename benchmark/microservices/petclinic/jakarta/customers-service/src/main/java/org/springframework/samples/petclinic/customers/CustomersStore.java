package org.springframework.samples.petclinic.customers;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@ApplicationScoped
public class CustomersStore {

    private final Map<Integer, Owner> owners = new ConcurrentHashMap<>();
    private final Map<Integer, Pet> pets = new ConcurrentHashMap<>();
    private final Map<Integer, PetType> types = new ConcurrentHashMap<>();

    private final AtomicInteger ownerSeq = new AtomicInteger(100);
    private final AtomicInteger petSeq = new AtomicInteger(100);

    @PostConstruct
    public void init() {
        // Pet types
        types.put(1, new PetType(1, "cat"));
        types.put(2, new PetType(2, "dog"));
        types.put(3, new PetType(3, "lizard"));
        types.put(4, new PetType(4, "snake"));
        types.put(5, new PetType(5, "bird"));
        types.put(6, new PetType(6, "hamster"));

        // Owners
        owners.put(1, new Owner(1, "George", "Franklin", "110 W. Liberty St.", "Madison", "6085551023"));
        owners.put(2, new Owner(2, "Betty", "Davis", "638 Cardinal Ave.", "Sun Prairie", "6085551749"));
        owners.put(3, new Owner(3, "Eduardo", "Rodriquez", "2693 Commerce St.", "McFarland", "6085558763"));
        owners.put(4, new Owner(4, "Harold", "Davis", "563 Friendly St.", "Windsor", "6085553198"));
        owners.put(5, new Owner(5, "Peter", "McTavish", "2387 S. Fair Way", "Madison", "6085552765"));
        owners.put(6, new Owner(6, "Jean", "Coleman", "105 N. Lake St.", "Monona", "6085552654"));
        owners.put(7, new Owner(7, "Jeff", "Black", "1450 Oak Blvd.", "Monona", "6085555387"));
        owners.put(8, new Owner(8, "Maria", "Escobito", "345 Maple St.", "Madison", "6085557683"));
        owners.put(9, new Owner(9, "David", "Schroeder", "2749 Blackhawk Trail", "Madison", "6085559435"));
        owners.put(10, new Owner(10, "Carlos", "Estaban", "2335 Independence La.", "Waunakee", "6085555487"));

        // Pets
        addPet(1, "Leo", "2010-09-07", 1, 1);
        addPet(2, "Basil", "2012-08-06", 6, 2);
        addPet(3, "Rosy", "2011-04-17", 2, 3);
        addPet(4, "Jewel", "2010-03-07", 2, 3);
        addPet(5, "Iggy", "2010-11-30", 3, 4);
        addPet(6, "George", "2010-01-20", 4, 5);
        addPet(7, "Samantha", "2012-09-04", 1, 6);
        addPet(8, "Max", "2012-09-04", 1, 6);
        addPet(9, "Lucky", "2011-08-06", 5, 7);
        addPet(10, "Mulligan", "2007-02-24", 2, 8);
        addPet(11, "Freddy", "2010-03-09", 5, 9);
        addPet(12, "Lucky", "2010-06-24", 2, 10);
        addPet(13, "Sly", "2012-06-08", 1, 10);
    }

    private void addPet(int id, String name, String birthDate, int typeId, int ownerId) {
        Pet p = new Pet(id, name, birthDate, types.get(typeId), ownerId);
        pets.put(id, p);
        Owner o = owners.get(ownerId);
        if (o != null) {
            o.pets.add(p);
        }
    }

    public List<Owner> findAllOwners() {
        List<Owner> all = new ArrayList<>(owners.values());
        all.sort(Comparator.comparing(o -> o.id));
        return all;
    }

    public Optional<Owner> findOwner(int id) {
        return Optional.ofNullable(owners.get(id));
    }

    public Owner createOwner(Owner o) {
        int id = ownerSeq.incrementAndGet();
        o.id = id;
        if (o.pets == null) o.pets = new ArrayList<>();
        owners.put(id, o);
        return o;
    }

    public List<PetType> findPetTypes() {
        List<PetType> all = new ArrayList<>(types.values());
        all.sort(Comparator.comparing(t -> t.name));
        return all;
    }

    public Optional<PetType> findPetTypeById(int id) {
        return Optional.ofNullable(types.get(id));
    }

    public Optional<Pet> findPet(int id) {
        return Optional.ofNullable(pets.get(id));
    }

    public Pet createPet(int ownerId, Pet newPet) {
        int id = petSeq.incrementAndGet();
        newPet.id = id;
        newPet.ownerId = ownerId;
        pets.put(id, newPet);
        Owner o = owners.get(ownerId);
        if (o != null) {
            o.pets.add(newPet);
        }
        return newPet;
    }
}
