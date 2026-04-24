package org.springframework.samples.petclinic.api;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class VisitDetails {
    public Integer id;
    public Integer petId;
    public String date;
    public String description;
}
