package spring.tutorial.customer.ejb;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import spring.tutorial.customer.data.Customer;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

@Service("customerBean")
@Transactional
public class CustomerBean {

    protected RestClient client;
    private static final Logger logger
            = Logger.getLogger(CustomerBean.class.getName());

    @PostConstruct
    private void init() {
        client = RestClient.builder()
                .baseUrl("http://localhost:8080/webapi/Customer")
                .build();
    }

    @PreDestroy
    private void clean() {
        // no-op
    }

    public String createCustomer(Customer customer) {
        if (customer == null) {
            logger.log(Level.WARNING, "customer is null.");
            return "customerError";
        }
        String navigation;
        ResponseEntity<Void> response =
                client.post()
                      .contentType(MediaType.APPLICATION_JSON)
                      .body(customer)
                      .retrieve()
                      .toBodilessEntity();
        if (response.getStatusCode().is2xxSuccessful() && response.getStatusCode().value() == 201) {
            navigation = "customerCreated";
        } else {
            logger.log(Level.WARNING,
                    "couldn''t create customer with id {0}. Status returned was {1}",
                    new Object[]{customer.getId(), response.getStatusCode().value()});
            FacesContext context = FacesContext.getCurrentInstance();
            context.addMessage(null,
                    new FacesMessage("Could not create customer."));
            navigation = "customerError";
        }
        return navigation;
    }

    public String retrieveCustomer(String id) {
        String navigation;
        Customer customer =
                client.get()
                      .uri("/{id}", id)
                      .accept(MediaType.APPLICATION_JSON)
                      .retrieve()
                      .toEntity(Customer.class)
                      .getBody();
        if (customer == null) {
            navigation = "customerError";
        } else {
            navigation = "customerRetrieved";
        }
        return navigation;
    }

    public List<Customer> retrieveAllCustomers() {
        return client.get()
                .uri("/all")
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .body(List.class);
    }
}
