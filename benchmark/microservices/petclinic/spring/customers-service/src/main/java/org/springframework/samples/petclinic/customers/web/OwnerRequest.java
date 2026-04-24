package org.springframework.samples.petclinic.customers.web;

public record OwnerRequest(String firstName,
                           String lastName,
                           String address,
                           String city,
                           String telephone) {
}
