package org.springframework.samples.petclinic.api.web;

import org.springframework.http.ResponseEntity;
import org.springframework.samples.petclinic.api.client.CustomersServiceClient;
import org.springframework.samples.petclinic.api.client.VisitsServiceClient;
import org.springframework.samples.petclinic.api.dto.OwnerDetails;
import org.springframework.samples.petclinic.api.dto.PetDetails;
import org.springframework.samples.petclinic.api.dto.Visits;
import org.springframework.samples.petclinic.api.dto.VisitDetails;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/gateway")
public class ApiGatewayController {

    private final CustomersServiceClient customersServiceClient;
    private final VisitsServiceClient visitsServiceClient;

    public ApiGatewayController(CustomersServiceClient customersServiceClient,
                                 VisitsServiceClient visitsServiceClient) {
        this.customersServiceClient = customersServiceClient;
        this.visitsServiceClient = visitsServiceClient;
    }

    @GetMapping("/owners/{ownerId}")
    public ResponseEntity<OwnerDetails> getOwnerDetails(@PathVariable int ownerId) {
        OwnerDetails owner = customersServiceClient.getOwner(ownerId);
        if (owner == null) {
            return ResponseEntity.notFound().build();
        }
        Visits visits;
        try {
            visits = visitsServiceClient.getVisitsForPets(owner.getPetIds());
        } catch (Exception e) {
            visits = new Visits();
        }
        attachVisits(owner, visits);
        return ResponseEntity.ok(owner);
    }

    private void attachVisits(OwnerDetails owner, Visits visits) {
        for (PetDetails pet : owner.pets) {
            List<VisitDetails> forPet = new ArrayList<>();
            for (VisitDetails v : visits.items) {
                if (v.petId != null && pet.id != null && v.petId.equals(pet.id)) {
                    forPet.add(v);
                }
            }
            pet.visits = forPet;
        }
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleAny(Exception e) {
        return ResponseEntity.status(500).body(e.getMessage());
    }
}
