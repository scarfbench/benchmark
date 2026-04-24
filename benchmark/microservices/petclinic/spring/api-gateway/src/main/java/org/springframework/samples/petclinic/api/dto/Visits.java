package org.springframework.samples.petclinic.api.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.ArrayList;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Visits {
    public List<VisitDetails> items = new ArrayList<>();

    public Visits() {}

    public Visits(List<VisitDetails> items) {
        this.items = items != null ? items : new ArrayList<>();
    }

    public List<VisitDetails> getItems() { return items; }
    public void setItems(List<VisitDetails> items) { this.items = items; }
}
