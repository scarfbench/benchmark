/*
Copyright 2013- IBM Corp. All Rights Reserved.

SPDX-License-Identifier: Apache-2.0
*/

package com.acmeair.loader;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.logging.Logger;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import com.acmeair.service.CustomerService;

@ApplicationScoped
public class CustomerLoader {

  @Inject
  CustomerService customerService;

  private static Logger logger = Logger.getLogger(CustomerLoader.class.getName());

  public String queryLoader() {
    String message = System.getProperty("loader.numCustomers");
    if (message == null) {
      logger.info("The system property 'loader.numCustomers' has not been set yet. "
          + "Looking up the default properties.");
      lookupDefaults();
      message = System.getProperty("loader.numCustomers");
    }
    return message;
  }

  public String loadCustomerDb(long numCustomers) {
    double length = 0;
    try {
      logger.info("Start loading " +  numCustomers + " customers");
      long start = System.currentTimeMillis();
      customerService.dropCustomers();

      String addressJson =  "{streetAddress1 : \"123 Main St.\", streetAddress2 :null, city: "
          + "\"Anytown\", stateProvince: \"NC\", country: \"USA\", postalCode: \"27617\"}";

      for (long ii = 0; ii < numCustomers; ii++) {
        customerService.createCustomer("uid" + ii + "@email.com", "password", "GOLD", 0, 0,
            "919-123-4567", "BUSINESS", addressJson);
      }

      long stop = System.currentTimeMillis();
      logger.info("Finished loading in " + (stop - start) / 1000.0 + " seconds");
      length = (stop - start) / 1000.0;
    } catch (Exception e) {
      e.printStackTrace();
    }

    return "Loaded "  +  numCustomers + " customers in " + length + " seconds";
  }

  private void lookupDefaults() {
    Properties props = getProperties();
    String numCustomers = props.getProperty("loader.numCustomers","100");
    System.setProperty("loader.numCustomers", numCustomers);
  }

  private Properties getProperties() {
    Properties props = new Properties();
    String propFileName = "/loader.properties";
    try {
      InputStream propFileStream = CustomerLoader.class.getResourceAsStream(propFileName);
      props.load(propFileStream);
    } catch (FileNotFoundException e) {
      logger.info("Property file " + propFileName + " not found.");
    } catch (IOException e) {
      logger.info("IOException - Property file " + propFileName + " not found.");
    }
    return props;
  }

}
