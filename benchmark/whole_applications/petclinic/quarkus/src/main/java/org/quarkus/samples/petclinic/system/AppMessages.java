package org.quarkus.samples.petclinic.system;

import io.quarkus.qute.i18n.Message;
import io.quarkus.qute.i18n.MessageBundle;

@MessageBundle
public interface AppMessages {
    
    @Message("Find Owner")
    String find_owner();

    @Message("Last Name")
    String last_name();

    @Message("Edit Owner")
    String edit_owner();

    @Message("Add Owner")
    String add_owner();

    @Message("Find Owners")
    String find_owners();

    @Message("Name")
    String name();

    @Message("Address")
    String address();

    @Message("Telephone")
    String telephone();

    @Message("City")
    String city();

    @Message("Pets")
    String pets();

    @Message("Pet")
    String pet();

    @Message("Error")
    String error();

    @Message("None")
    String none();

    @Message("Veterinarians")
    String veterinarians();

    @Message("Specialties")
    String specialties();

    @Message("Welcome")
    String welcome();

    @Message("Birthdate")
    String birthdate();

    @Message("Type")
    String type();

    @Message("Owner")
    String owner();

    @Message("Edit Pet")
    String edit_pet();

    @Message("Add Pet")
    String add_pet();

    @Message("Add Visit")
    String add_visit();

    @Message("Visit Date")
    String visit_date();

    @Message("Description")
    String description();

    @Message("New")
    String new_();

    @Message("Visit")
    String visit();

    @Message("Date")
    String date();

    @Message("Home")
    String home();

    @Message("Pets and Visits")
    String pets_and_visits();

    @Message("Previous Visits")
    String previous_visits();

    @Message("Something happened...")
    String something_wrong();

}
