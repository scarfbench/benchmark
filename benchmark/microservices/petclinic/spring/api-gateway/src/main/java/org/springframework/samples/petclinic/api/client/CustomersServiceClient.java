package org.springframework.samples.petclinic.api.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.samples.petclinic.api.dto.OwnerDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class CustomersServiceClient {

    @Value("${customers.service.url:http://customers-service:9080}")
    private String baseUrl;

    private final RestTemplate restTemplate;

    public CustomersServiceClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public OwnerDetails getOwner(int ownerId) {
        String url = baseUrl + "/owners/" + ownerId;
        return restTemplate.getForObject(url, OwnerDetails.class);
    }
}
