package spring.tutorial.customer.ejb;

import java.io.Serializable;
import java.util.List;
import java.util.logging.Logger;

import jakarta.annotation.PostConstruct;
import spring.tutorial.customer.data.Customer;

import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.RequestScope;

@Component("customerManager")
@RequestScope
public class CustomerManager implements Serializable {
    private Customer customer;
    private List<Customer> customers;
    private static final Logger logger = Logger.getLogger(CustomerManager.class.getName());
    private final CustomerBean customerBean;
    
    public CustomerManager(CustomerBean customerBean) {
        this.customerBean = customerBean;
    }

    @PostConstruct
    private void init() {
        logger.info("new customer created");
        customer = new Customer();
        setCustomers(customerBean.retrieveAllCustomers());
    }

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

    public List<Customer> getCustomers() {
        return customerBean.retrieveAllCustomers();
    }

    public void setCustomers(List<Customer> customers) {
        this.customers = customers;
    }
}
