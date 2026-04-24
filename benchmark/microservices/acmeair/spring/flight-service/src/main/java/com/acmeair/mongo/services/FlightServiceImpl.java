/*******************************************************************************
* Copyright (c) 2015 IBM Corp.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*******************************************************************************/

package com.acmeair.mongo.services;

import static com.mongodb.client.model.Filters.eq;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;

import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.acmeair.AirportCodeMapping;
import com.acmeair.mongo.ConnectionManager;
import com.acmeair.mongo.MongoConstants;
import com.acmeair.service.FlightService;
import com.acmeair.service.KeyGenerator;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.BasicDBObject;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.util.JSON;

@Component
public class FlightServiceImpl extends FlightService implements  MongoConstants {

  private static final Logger logger = Logger.getLogger(FlightServiceImpl.class.getName()); 
  
  private MongoCollection<Document> flight;
  private MongoCollection<Document> flightSegment;
  private MongoCollection<Document> airportCodeMapping;
  
  @Autowired
  KeyGenerator keyGenerator;

  /**
   * Init mongo db.
   */
  @PostConstruct
  public void initialization() {
    MongoDatabase database = ConnectionManager.getConnectionManager().getDb();
    flight = database.getCollection("flight");
    flightSegment = database.getCollection("flightSegment");
    airportCodeMapping = database.getCollection("airportCodeMapping");
  }

  @Override
  public Long countFlights() {
    return flight.count();
  }
  
  @Override
  public Long countFlightSegments() {
    return flightSegment.count();
  }
  
  @Override
  public Long countAirports() {
    return airportCodeMapping.count();
  }
  
  protected String getFlight(String flightId, String segmentId) {
    return flight.find(eq("_id", flightId)).first().toJson();
  }

  @Override
  protected  String getFlightSegment(String fromAirport, String toAirport) {
    try {
      return flightSegment.find(new BasicDBObject("originPort", fromAirport)
              .append("destPort", toAirport)).first().toJson();
    } catch (java.lang.NullPointerException e) {
      if (logger.isLoggable(Level.FINE)) {
        logger.fine("getFlghtSegment returned no flightSegment available");
      }
      return "";
    }
  }

  @Override
  protected  Long getRewardMilesFromSegment(String segmentId) {
    try {
      String segment = flightSegment.find(new BasicDBObject("_id", segmentId)).first().toJson();
            
      ObjectMapper mapper = new ObjectMapper();
      JsonNode segmentJson = mapper.readTree(segment);
            
      return Long.parseLong(segmentJson.get("miles").asText());
            
    } catch (java.lang.NullPointerException e) {
      if (logger.isLoggable(Level.FINE)) {
        logger.fine("getFlghtSegment returned no flightSegment available");
      }
            
    } catch (Exception e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    return null;
  }
  
  @Override
  protected  List<String> getFlightBySegment(String segment, Date deptDate) {
    try {
    	 ObjectMapper mapper = new ObjectMapper();
         JsonNode segmentJson = mapper.readTree(segment);
      MongoCursor<Document> cursor;

      if (deptDate != null) {
        if (logger.isLoggable(Level.FINE)) {
          logger.fine("getFlghtBySegment Search String : " 
                  + new BasicDBObject("flightSegmentId", segmentJson.get("_id").asText())
                  .append("scheduledDepartureTime", deptDate).toJson());
        }
        cursor = flight.find(new BasicDBObject("flightSegmentId", segmentJson.get("_id").asText())
                .append("scheduledDepartureTime", deptDate)).iterator();
      } else {
        cursor = flight.find(eq("flightSegmentId", segmentJson.get("_id").asText())).iterator();
      }
      
      List<String> flights =  new ArrayList<String>();
      try {
        while (cursor.hasNext()) {
          Document tempDoc = cursor.next();

          if (logger.isLoggable(Level.FINE)) {
            logger.fine("getFlghtBySegment Before : " + tempDoc.toJson());
          }
          
          Date deptTime = (Date)tempDoc.get("scheduledDepartureTime");
          Date arvTime = (Date)tempDoc.get("scheduledArrivalTime");
          tempDoc.remove("scheduledDepartureTime");
          tempDoc.append("scheduledDepartureTime", deptTime.toString());
          tempDoc.remove("scheduledArrivalTime");
          tempDoc.append("scheduledArrivalTime", arvTime.toString());
          tempDoc.append("flightSegment", JSON.parse(segment));
          
          if (logger.isLoggable(Level.FINE)) {
            logger.fine("getFlghtBySegment after : " + tempDoc.toJson());
          }

          flights.add(tempDoc.toJson());
        }
      } finally {
        cursor.close();
      }
      return flights;
    } catch (Exception e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
      return null;
    }
  }


  @Override
  public void storeAirportMapping(AirportCodeMapping mapping) {
    Document airportDoc = new Document("_id", mapping.getAirportCode())
              .append("airportName", mapping.getAirportName());
    airportCodeMapping.insertOne(airportDoc);
  }

  @Override
  public AirportCodeMapping createAirportCodeMapping(String airportCode, String airportName) {
    return new AirportCodeMapping(airportCode,airportName);
  }

  @Override
  public void createNewFlight(String flightSegmentId,
          Date scheduledDepartureTime, Date scheduledArrivalTime,
          int firstClassBaseCost, int economyClassBaseCost,
          int numFirstClassSeats, int numEconomyClassSeats,
          String airplaneTypeId) {
    String id = keyGenerator.generate().toString();
    Document flightDoc = new Document("_id", id)
            .append("firstClassBaseCost", firstClassBaseCost)
            .append("economyClassBaseCost", economyClassBaseCost)
            .append("numFirstClassSeats", numFirstClassSeats)
            .append("numEconomyClassSeats", numEconomyClassSeats)
            .append("airplaneTypeId", airplaneTypeId)
            .append("flightSegmentId", flightSegmentId)
            .append("scheduledDepartureTime", scheduledDepartureTime)
            .append("scheduledArrivalTime", scheduledArrivalTime);

    flight.insertOne(flightDoc);
  }
  
  @Override 
  public void storeFlightSegment(String flightSeg) {
    try {
   	 ObjectMapper mapper = new ObjectMapper();
     JsonNode flightSegJson = mapper.readTree(flightSeg);
      storeFlightSegment(flightSegJson.get("_id").asText(), 
              flightSegJson.get("originPort").asText(), 
              flightSegJson.get("destPort").asText(), 
              Integer.parseInt(flightSegJson.get("miles").asText()));
    } catch (Exception e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

  @Override 
  public void storeFlightSegment(String flightName, String origPort, String destPort, int miles) {
    Document flightSegmentDoc = new Document("_id", flightName)
            .append("originPort", origPort)
            .append("destPort", destPort)
            .append("miles", miles);

    flightSegment.insertOne(flightSegmentDoc);
  }

  @Override
  public void dropFlights() {
    airportCodeMapping.deleteMany(new Document());
    flightSegment.deleteMany(new Document());
    flight.deleteMany(new Document());
  }

  @Override
  public String getServiceType() {
    return "mongo";
  }
}
