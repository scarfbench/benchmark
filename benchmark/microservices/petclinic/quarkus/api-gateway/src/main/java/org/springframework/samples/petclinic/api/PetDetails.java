package org.springframework.samples.petclinic.api;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.ArrayList;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class PetDetails {
    public Integer id;
    public String name;
    public String birthDate;
    public PetType type;
    public List<VisitDetails> visits = new ArrayList<>();
}
