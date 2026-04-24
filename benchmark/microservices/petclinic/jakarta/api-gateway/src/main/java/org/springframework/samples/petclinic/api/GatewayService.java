package org.springframework.samples.petclinic.api;

import java.io.StringReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonObject;
import jakarta.json.JsonReader;
import jakarta.json.JsonValue;

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

    public JsonObject getOwnerDetails(int ownerId) {
        JsonObject owner = getOwner(ownerId);
        if (owner == null) {
            return null;
        }
        List<Integer> petIds = new ArrayList<>();
        JsonArray petsArr = owner.getJsonArray("pets");
        if (petsArr != null) {
            for (JsonValue pv : petsArr) {
                if (pv.getValueType() == JsonValue.ValueType.OBJECT) {
                    JsonObject pj = (JsonObject) pv;
                    if (pj.containsKey("id") && !pj.isNull("id")) {
                        petIds.add(pj.getInt("id"));
                    }
                }
            }
        }

        JsonArray visits = getVisitsForPets(petIds);

        return mergeVisits(owner, visits);
    }

    private JsonObject getOwner(int ownerId) {
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
            try (JsonReader reader = Json.createReader(new StringReader(response.body()))) {
                return reader.readObject();
            }
        } catch (Exception e) {
            return null;
        }
    }

    private JsonArray getVisitsForPets(List<Integer> petIds) {
        if (petIds.isEmpty()) {
            return Json.createArrayBuilder().build();
        }
        try {
            String ids = petIds.stream().map(String::valueOf).collect(Collectors.joining(","));
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(visitsUrl + "/pets/visits?petId=" + ids))
                    .timeout(Duration.ofSeconds(10))
                    .GET()
                    .build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() >= 400) {
                return Json.createArrayBuilder().build();
            }
            try (JsonReader reader = Json.createReader(new StringReader(response.body()))) {
                JsonObject obj = reader.readObject();
                if (obj.containsKey("items") && !obj.isNull("items")) {
                    return obj.getJsonArray("items");
                }
                return Json.createArrayBuilder().build();
            }
        } catch (Exception e) {
            return Json.createArrayBuilder().build();
        }
    }

    private JsonObject mergeVisits(JsonObject owner, JsonArray visits) {
        jakarta.json.JsonObjectBuilder ownerBuilder = Json.createObjectBuilder();
        for (String key : owner.keySet()) {
            if (!"pets".equals(key)) {
                ownerBuilder.add(key, owner.get(key));
            }
        }

        jakarta.json.JsonArrayBuilder newPets = Json.createArrayBuilder();
        JsonArray petsArr = owner.getJsonArray("pets");
        if (petsArr != null) {
            for (JsonValue pv : petsArr) {
                if (pv.getValueType() != JsonValue.ValueType.OBJECT) {
                    continue;
                }
                JsonObject pj = (JsonObject) pv;
                int petId = (pj.containsKey("id") && !pj.isNull("id")) ? pj.getInt("id") : -1;

                jakarta.json.JsonObjectBuilder pb = Json.createObjectBuilder();
                for (String k : pj.keySet()) {
                    pb.add(k, pj.get(k));
                }

                jakarta.json.JsonArrayBuilder petVisits = Json.createArrayBuilder();
                for (JsonValue vv : visits) {
                    if (vv.getValueType() != JsonValue.ValueType.OBJECT) continue;
                    JsonObject vj = (JsonObject) vv;
                    if (vj.containsKey("petId") && !vj.isNull("petId") && vj.getInt("petId") == petId) {
                        petVisits.add(vj);
                    }
                }
                pb.add("visits", petVisits.build());
                newPets.add(pb.build());
            }
        }
        ownerBuilder.add("pets", newPets.build());
        return ownerBuilder.build();
    }
}
