/*
Copyright 2017- IBM Corp. All Rights Reserved.

SPDX-License-Identifier: Apache-2.0
*/

package com.acmeair.mongo.services;

import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Updates.combine;
import static com.mongodb.client.model.Updates.set;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;
import java.util.Properties;
import java.util.logging.Logger;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import org.bson.Document;

import com.acmeair.mongo.MongoClientProducer;
import com.acmeair.mongo.MongoConstants;
import com.acmeair.service.CustomerService;
import com.acmeair.web.dto.CustomerInfo;
import com.mongodb.ConnectionString;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

@ApplicationScoped
public class CustomerServiceImpl extends CustomerService implements MongoConstants {

  @Inject
  MongoClient mongoClient;

  @Inject
  MongoClientProducer mongoClientProducer;

  private MongoCollection<Document> customer;

  private static final Logger logger = Logger.getLogger(CustomerServiceImpl.class.getName());

  @PostConstruct
  public void initialization() {
      String connectionString = mongoClientProducer.getConnectionString();
      ConnectionString conn = new ConnectionString(connectionString);
      String dbname = conn.getDatabase();

      Properties prop = new Properties();
      String acmeairProps = System.getenv("ACMEAIR_PROPERTIES");
      try {
          if (acmeairProps != null) {
              prop.load(new FileInputStream(acmeairProps));
          } else {
              prop.load(CustomerServiceImpl.class.getResourceAsStream("/config.properties"));
              acmeairProps = "OK";
          }
      } catch (IOException ex) {
          logger.info("Properties file does not exist" + ex.getMessage());
          acmeairProps = null;
      }
      if (acmeairProps != null) {
          logger.info("Reading mongo.properties file");
          if(dbname == null) {
              if (System.getenv("MONGO_DBNAME") != null) {
                  dbname = System.getenv("MONGO_DBNAME");
              } else if (prop.containsKey("dbname")) {
                  dbname = prop.getProperty("dbname");
              }
              if(dbname == null) {
                  dbname = "acmeair";
              }
          }
      }

      MongoDatabase database = mongoClient.getDatabase(dbname);
      customer = database.getCollection("customer");

      logger.info("#### Mongo DB is created with DB name: " + dbname + " ####");
      logger.info("Complete List : " + connectionString);
  }

  @Override
  public Long count() {
    return customer.countDocuments();
  }

  @Override
  public void createCustomer(String username, String password, String status,
      int totalMiles, int milesYtd,
      String phoneNumber, String phoneNumberType, String addressJson) {

    new Document();
    Document customerDoc = new Document("_id", username)
        .append("password", password)
        .append("status", status)
        .append("total_miles", totalMiles).append("miles_ytd", milesYtd)
        .append("address", Document.parse(addressJson)).append("phoneNumber", phoneNumber)
        .append("phoneNumberType", phoneNumberType);

    customer.insertOne(customerDoc);
  }

  @Override
  public String createAddress(String streetAddress1, String streetAddress2, String city,
      String stateProvince, String country, String postalCode) {
    Document addressDoc = new Document("streetAddress1", streetAddress1)
        .append("city", city)
        .append("stateProvince", stateProvince)
        .append("country", country)
        .append("postalCode", postalCode);

    if (streetAddress2 != null) {
      addressDoc.append("streetAddress2", streetAddress2);
    }

    return addressDoc.toJson();
  }

  @Override
  public void updateCustomer(String username, CustomerInfo customerInfo) {
    Document address = new Document("streetAddress1", customerInfo.getAddress().getStreetAddress1())
        .append("city", customerInfo.getAddress().getCity())
        .append("stateProvince", customerInfo.getAddress().getStateProvince())
        .append("country", customerInfo.getAddress().getCountry())
        .append("postalCode", customerInfo.getAddress().getPostalCode());

    if (customerInfo.getAddress().getStreetAddress2() != null) {
      address.append("streetAddress2", customerInfo.getAddress().getStreetAddress2());
    }
    customer.updateOne(eq("_id", customerInfo.get_id()),
        combine(set("status", customerInfo.getStatus()),
            set("total_miles", customerInfo.getTotalMiles()),
            set("miles_ytd", customerInfo.getMilesYtd()),
            set("address", address),
            set("phoneNumber", customerInfo.getPhoneNumber()),
            set("phoneNumberType", customerInfo.getPhoneNumberType())));
  }

  @Override
  protected String getCustomer(String username) {
    return customer.find(eq("_id", username)).first().toJson();
  }

  @Override
  public String getCustomerByUsername(String username) {
    Document customerDoc = customer.find(eq("_id", username)).first();
    if (customerDoc != null) {
      customerDoc.remove("password");
      customerDoc.append("password", null);
    }
    return customerDoc.toJson();
  }

  @Override
  public void dropCustomers() {
    customer.deleteMany(new Document());
  }

  @Override
  public String getServiceType() {
    return "mongo";
  }

}
