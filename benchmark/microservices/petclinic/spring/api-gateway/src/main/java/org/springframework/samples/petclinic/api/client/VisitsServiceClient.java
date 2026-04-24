package org.springframework.samples.petclinic.api.client;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.samples.petclinic.api.dto.Visits;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class VisitsServiceClient {

    @Value("${visits.service.url:http://visits-service:9080}")
    private String baseUrl;

    private final RestTemplate restTemplate;

    public VisitsServiceClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public Visits getVisitsForPets(List<Integer> petIds) {
        if (petIds == null || petIds.isEmpty()) {
            return new Visits();
        }
        String ids = petIds.stream().map(String::valueOf).collect(Collectors.joining(","));
        String url = baseUrl + "/pets/visits?petId=" + ids;
        Visits result = restTemplate.getForObject(url, Visits.class);
        return result != null ? result : new Visits();
    }
}
