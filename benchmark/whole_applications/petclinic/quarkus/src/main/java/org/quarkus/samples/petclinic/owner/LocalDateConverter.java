package org.quarkus.samples.petclinic.owner;

import jakarta.ws.rs.ext.ParamConverter;

import java.time.LocalDate;

public class LocalDateConverter implements ParamConverter<LocalDate> {

    @Override
    public LocalDate fromString(String value) {
        if ("".equals(value.trim()))
            return null;
        return LocalDate.parse(value);
    }

    @Override
    public String toString(LocalDate value) {
        if (value == null)
            return null;
        return value.toString();
    }

}
