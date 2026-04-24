package org.springframework.samples.petclinic.api;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class GatewayService {

    @Inject
    @ConfigProperty(name = "customers.service.url")
    String customersUrl;

    @Inject
    @ConfigProperty(name = "visits.service.url")
    String visitsUrl;

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .build();

    private final ObjectMapper mapper = new ObjectMapper();

    public OwnerDetails getOwnerDetails(int ownerId) {
        OwnerDetails owner = getOwner(ownerId);
        if (owner == null) {
            return null;
        }
        Visits visits = getVisitsForPets(owner.getPetIds());
        attachVisits(owner, visits);
        return owner;
    }

    private OwnerDetails getOwner(int ownerId) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(customersUrl + "/owners/" + ownerId))
                    .timeout(Duration.ofSeconds(10))
                    .GET()
                    .build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() >= 400) {
                return null;
            }
            return mapper.readValue(response.body(), OwnerDetails.class);
        } catch (Exception e) {
            return null;
        }
    }

    private Visits getVisitsForPets(List<Integer> petIds) {
        Visits empty = new Visits();
        if (petIds == null || petIds.isEmpty()) return empty;
        try {
            String ids = petIds.stream().map(String::valueOf).collect(Collectors.joining(","));
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(visitsUrl + "/pets/visits?petId=" + ids))
                    .timeout(Duration.ofSeconds(10))
                    .GET()
                    .build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() >= 400) return empty;
            Visits v = mapper.readValue(response.body(), Visits.class);
            return v != null ? v : empty;
        } catch (Exception e) {
            return empty;
        }
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
}
