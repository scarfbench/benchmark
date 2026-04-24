/*
Copyright 2017- IBM Corp. All Rights Reserved.

SPDX-License-Identifier: Apache-2.0
*/

package com.acmeair.mongo.services;

import static com.mongodb.client.model.Filters.eq;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.bson.Document;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import com.acmeair.mongo.MongoConstants;
import com.acmeair.service.BookingService;
import com.acmeair.service.KeyGenerator;
import com.mongodb.ConnectionString;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;

@ApplicationScoped
public class BookingServiceImpl implements BookingService, MongoConstants {

  @Inject
  MongoClient mongoClient;

  @ConfigProperty(name = "quarkus.mongodb.connection-string")
  String connectionString;

  private static final  Logger logger = Logger.getLogger(BookingService.class.getName());

  private MongoCollection<Document> booking;

  @Inject
  KeyGenerator keyGenerator;

  @PostConstruct
  public void initialization() {
    ConnectionString conn = new ConnectionString(connectionString);
    String dbname = conn.getDatabase();

    Properties prop = new Properties();
    String acmeairProps = System.getenv("ACMEAIR_PROPERTIES");
    try {
        if (acmeairProps != null) {
            prop.load(new FileInputStream(acmeairProps));
        } else {
            prop.load(BookingServiceImpl.class.getResourceAsStream("/config.properties"));
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
        if (prop.containsKey("hostname") || prop.containsKey("port") ||
            prop.containsKey("username") || prop.containsKey("password") ||
            prop.containsKey("sslEnabled") ||
            prop.containsKey("connectionsPerHost") || prop.containsKey("minConnectionsPerHost") ||
            prop.containsKey("maxWaitTime") || prop.containsKey("connectTimeout") || prop.containsKey("socketTimeout") ||
            prop.containsKey("socketKeepAlive") ||
            prop.containsKey("threadsAllowedToBlockForConnectionMultiplier")) {
            logger.warning("Options specified in config.properties (except \"dbname\") are ignored. " +
                           "Use application.properties or environment varialbes to specify MongoDB connection options.");
        }
    }

    MongoDatabase database = mongoClient.getDatabase(dbname);
    booking = database.getCollection("booking");

    List<ServerAddress> hostAddrs =  mongoClient.getClusterDescription().getClusterSettings().getHosts();
    logger.info("#### Mongo DB [Server:Port] list: " + hostAddrs.toString() + " ####");
    logger.info("#### Mongo DB is created with DB name: " + dbname + " ####");
    logger.info("#### MongoClient Options ####");
    logger.info("maxWaitTime : " + conn.getMaxWaitTime());
    logger.info("connectTimeout : " + conn.getConnectTimeout());
    logger.info("socketTimeout : " + conn.getSocketTimeout());
    logger.info("sslEnabled : " + conn.getSslEnabled());
    logger.info("Complete List : " + connectionString);
  }
  
  /**
   * Book Flight.
   */
  public String bookFlight(String customerId, String flightId) {
    try {

      String bookingId = keyGenerator.generate().toString();

      Document bookingDoc = new Document("_id", bookingId)
          .append("customerId", customerId)
          .append("flightId", flightId)
          .append("dateOfBooking", new Date());

      booking.insertOne(bookingDoc);

      return bookingId;
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public String bookFlight(String customerId, String flightSegmentId, String flightId) {
    if (flightSegmentId == null) {
      return bookFlight(customerId, flightId);
    } else {

      try {

        String bookingId = keyGenerator.generate().toString();

        Document bookingDoc = new Document("_id", bookingId).append("customerId", customerId)
            .append("flightId", flightId).append("dateOfBooking", new Date())
            .append("flightSegmentId", flightSegmentId);

        booking.insertOne(bookingDoc);

        return bookingId;
      } catch (Exception e) {
        throw new RuntimeException(e);
      }

    }
  }

  @Override
  public String getBooking(String user, String bookingId) {
    try {
      return booking.find(eq("_id", bookingId)).first().toJson();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public List<String> getBookingsByUser(String user) {
    List<String> bookings = new ArrayList<>();
    if (logger.isLoggable(Level.FINE)) {
      logger.fine("getBookingsByUser : " + user);
    }
    try (MongoCursor<Document> cursor = booking.find(eq("customerId", user)).iterator()) {

      while (cursor.hasNext()) {
        Document tempBookings = cursor.next();
        Date dateOfBooking = (Date) tempBookings.get("dateOfBooking");
        tempBookings.remove("dateOfBooking");
        tempBookings.append("dateOfBooking", dateOfBooking.toString());

        if (logger.isLoggable(Level.FINE)) {
          logger.fine("getBookingsByUser cursor data : " + tempBookings.toJson());
        }
        bookings.add(tempBookings.toJson());
      }
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
    return bookings;
  }

  @Override
  public void cancelBooking(String user, String bookingId) {
    if (logger.isLoggable(Level.FINE)) {
      logger.fine("cancelBooking _id : " + bookingId);
    }
    try {
      booking.deleteMany(eq("_id", bookingId));
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public Long count() {
      return booking.countDocuments();
  }

  @Override
  public void dropBookings() {
    booking.deleteMany(new Document());
  }

  @Override
  public String getServiceType() {
    return "mongo";
  }
}
