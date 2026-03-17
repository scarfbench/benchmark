/*
 * Copyright (c), Eclipse Foundation, Inc. and its licensors.
 *
 * All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v1.0, which is available at
 * https://www.eclipse.org/org/documents/edl-v10.php
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */
package quarkus.tutorial.customer.ejb;

import java.io.Serializable;
import java.util.List;
import java.util.logging.Logger;
import jakarta.inject.Named;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import quarkus.tutorial.customer.data.Customer;
import quarkus.tutorial.customer.data.Address;

/**
 *
 * @author ievans
 */
@Named
@RequestScoped
public class CustomerManager implements Serializable {
    private static final Logger logger = Logger.getLogger(CustomerManager.class.getName());

    private Customer customer = new Customer();                 // pre-init
    private List<Customer> customers = java.util.Collections.emptyList();

    @Inject
    private CustomerBean customerBean;

    @PostConstruct
    private void init() {
        logger.info("CustomerManager init");
        if (customer.getAddress() == null) {
            customer.setAddress(new Address());
        }
        try {
            customers = customerBean.retrieveAllCustomers();
        } catch (Exception e) {
            logger.warning("retrieveAllCustomers failed: " + e.getMessage());
            customers = java.util.Collections.emptyList();
        }
    }

    public Customer getCustomer() {
        return customer;                                      
    }
    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

    public List<Customer> getCustomers() {
        try {
            return customerBean.retrieveAllCustomers(); 
        } catch (Exception e) {
            return java.util.Collections.emptyList();
        }
    }
    public void setCustomers(List<Customer> customers) {
        this.customers = customers;
    }
}
