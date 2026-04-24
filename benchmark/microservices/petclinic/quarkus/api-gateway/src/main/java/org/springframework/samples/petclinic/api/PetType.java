package org.springframework.samples.petclinic.api;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class PetType {
    public Integer id;
    public String name;
}
