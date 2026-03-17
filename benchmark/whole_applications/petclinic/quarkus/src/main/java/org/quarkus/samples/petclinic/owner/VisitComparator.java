package org.quarkus.samples.petclinic.owner;

import java.util.Comparator;

import org.quarkus.samples.petclinic.visit.Visit;

public class VisitComparator implements Comparator<Visit> {

    @Override
    public int compare(Visit o1, Visit o2) {
        return o1.date.compareTo(o2.date);
    }
    
}
