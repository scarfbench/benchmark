package org.quarkus.samples.petclinic.system;

import io.quarkus.qute.i18n.Localized;
import io.quarkus.qute.i18n.Message;

@Localized("es")
public interface SpanishAppMessages extends AppMessages {
    
    @Override
    @Message("Buscar Propietario")
    String find_owner();

    @Override
    @Message("Editar Propietario")
    String edit_owner();

    @Override
    @Message("Apellido")
    String last_name();

    @Override
    @Message("Añadir Propietario")
    String add_owner();

    @Override
    @Message("Buscar Propietarios")
    String find_owners();

    @Override
    @Message("Nombre")
    String name();

    @Override
    @Message("Error")
    String error();

    @Override
    @Message("Veterinarios")
    String veterinarians();

    @Override
    @Message("ninguna")
    String none();

    @Override
    @Message("Inicio")
    String home();

    @Override
    @Message("Dirección")
    String address();

    @Override
    @Message("Teléfono")
    String telephone();

    @Override
    @Message("Mascotas")
    String pets();

    @Override
    @Message("Especialidades")
    String specialties();

    @Override
    @Message("Bienvenidos")
    String welcome();

    @Override
    @Message("Ciudad")
    String city();

    @Message("Fecha Nacimiento")
    String birthdate();

    @Override
    @Message("Mascotas y Visitas")
    String pets_and_visits();

    @Override
    @Message("Tipo")
    String type();

    @Override
    @Message("Añadir Visita")
    String add_visit();

    @Override
    @Message("Añadir Mascota")
    String add_pet();

    @Override
    @Message("Editar Mascota")
    String edit_pet();

    @Override
    @Message("Fecha Visita")
    String visit_date();

    @Override
    @Message("Mascota")
    String pet();

    @Override
    @Message("Descripción")
    String description();

    @Override
    @Message("Algo ha ocurrido...")
    String something_wrong();

    @Override
    @Message("Propietario")
    String owner();

    @Override
    @Message("Fecha")
    String date();

    @Override
    @Message("Visitas Anteriores")
    String previous_visits();

}
