/*
Copyright 2013- IBM Corp. All Rights Reserved.

SPDX-License-Identifier: Apache-2.0
*/

package com.acmeair.service;

import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.acmeair.AirportCodeMapping;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public abstract class FlightService {
    
  protected FlightService() {
    if (useFlightDataRelatedCaching == null) {
      Properties properties = new Properties();
      try {
        String acmeairProps = System.getenv("ACMEAIR_PROPERTIES");
        if (acmeairProps != null) {
          System.out.println("Loading properties : " + acmeairProps);
          properties.load(new FileInputStream(acmeairProps));
          useFlightDataRelatedCaching = Boolean.parseBoolean(properties
                  .getProperty("userFlightDataRelatedCaching"));
          System.out.println("useFlightDataRelatedCaching : " + useFlightDataRelatedCaching);
          
        } else if (System.getenv("USE_FLIGHT_DATA_RELATED_CACHING") != null) {
          System.out.println("Found env variable USE_FLIGHT_DATA_RELATED_CACHING");
          useFlightDataRelatedCaching = Boolean.parseBoolean(System
                  .getenv("USE_FLIGHT_DATA_RELATED_CACHING"));
          System.out.println("useFlightDataRelatedCaching : " + useFlightDataRelatedCaching);
          
        } else {
          System.out.println("Neither ACMEAIR_PROPERTIES or USE_FLIGHT_DATA_RELATED_CACHING "
                  + "environment variables are set. Enabling Caching. To disable caching, use "
                  + "Environment variable ACMEAIR_PROPERTIES or USE_FLIGHT_DATA_RELATED_CACHING");
          useFlightDataRelatedCaching = true;
          
        }
      } catch (Exception e) {
        System.out.println("ACMEAIR_PROPERTIES error. Check for below log");
        e.printStackTrace();
      }
    }
  }

  protected static final Logger logger =  Logger.getLogger(FlightService.class.getName());
  
  protected static Boolean useFlightDataRelatedCaching = null;
  protected static String acmeairDir = "";

  // TODO:need to find a way to invalidate these maps
  protected static ConcurrentHashMap<String, String> originAndDestPortToSegmentCache = 
          new ConcurrentHashMap<>();
  protected static ConcurrentHashMap<String, List<String>> flightSegmentAndDataToFlightCache = 
          new ConcurrentHashMap<>();
  protected static ConcurrentHashMap<String, String> flightPKtoFlightCache = 
          new ConcurrentHashMap<>();
  protected static ConcurrentHashMap<String, Long> flightSegmentIdtoRewardsCache = 
          new ConcurrentHashMap<>();
  
  
  /**
   * Get flight.
   */
  public String getFlightByFlightId(String flightId, String flightSegment) {
    try {
      if (logger.isLoggable(Level.FINE)) {
        logger.fine("Book flights with " + flightId + " and " + flightSegment);
      }
      if (useFlightDataRelatedCaching) {
        String flight = flightPKtoFlightCache.get(flightId);
        if (flight == null) {
          flight = getFlight(flightId, flightSegment);
          if (flightId != null && flight != null) {
            flightPKtoFlightCache.putIfAbsent(flightId, flight);
          }
        }
        return flight;
      } else {
        return getFlight(flightId, flightSegment);
      }
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
  
  
  protected abstract String getFlight(String flightId, String flightSegment);
  
  /**
   * Get Flight.
   */
  public List<String> getFlightByAirportsAndDepartureDate(String fromAirport,
          String toAirport, Date deptDate) {
    if (logger.isLoggable(Level.FINE)) {
      logger.fine("Search for flights from " + fromAirport + " to " + toAirport + " on " 
              + deptDate.toString());
    }

    String originPortAndDestPortQueryString = fromAirport + toAirport;
    String segment = null;
    if (useFlightDataRelatedCaching) {
      segment = originAndDestPortToSegmentCache.get(originPortAndDestPortQueryString);

      if (segment == null) {
        segment = getFlightSegment(fromAirport, toAirport);
        originAndDestPortToSegmentCache.putIfAbsent(originPortAndDestPortQueryString, segment);
      }
    } else {
      segment = getFlightSegment(fromAirport, toAirport);
    }
    if (logger.isLoggable(Level.FINE)) {
      logger.fine("Segment " + segment);
    }
    // cache flights that not available (checks against sentinel value above indirectly)
    try {
      if (segment == "") {
        return new ArrayList<>();
      }
      
      ObjectMapper mapper = new ObjectMapper();
      JsonNode segmentJson = mapper.readTree(segment);
      
      if (logger.isLoggable(Level.FINE)) {
        logger.fine("Segment in JSON " + segmentJson);
      }
      
      String segId = segmentJson.get("_id").asText();
      if (segId == null) {
        if (logger.isLoggable(Level.FINE)) {
          logger.fine("Segment is null");
        }
        
        return new ArrayList<>(); 
      }

      String flightSegmentIdAndScheduledDepartureTimeQueryString = segId + deptDate.toString();
      if (logger.isLoggable(Level.FINE)) {
        logger.fine("flightSegmentIdAndScheduledDepartureTimeQueryString " 
                + flightSegmentIdAndScheduledDepartureTimeQueryString);
      }
      if (useFlightDataRelatedCaching) {
        List<String> flights = flightSegmentAndDataToFlightCache
                .get(flightSegmentIdAndScheduledDepartureTimeQueryString);
        if (flights == null) {
          flights = getFlightBySegment(segment, deptDate);
          if (logger.isLoggable(Level.FINE)) {
            logger.fine("flights search results if flights cache is null " + flights.toString());
          }

          flightSegmentAndDataToFlightCache.putIfAbsent(
                  flightSegmentIdAndScheduledDepartureTimeQueryString, flights);
        }
        if (logger.isLoggable(Level.FINEST)) {
          logger.finest("Returning " + flights);
        }
        return flights;
      } else {
        if (logger.isLoggable(Level.FINE)) {
          logger.fine("useFlightDataRelatedCaching is false ");
        }

        List<String> flights = getFlightBySegment(segment, deptDate);
        if (logger.isLoggable(Level.FINEST)) {
          logger.finest("Returning " + flights);
        }
        return flights;
      }
    } catch (Exception e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
      return null;
    }
  }

  /**
   * Get flight by aiports. Not cached
   */
  public List<String> getFlightByAirports(String fromAirport, String toAirport) {
    String segment = getFlightSegment(fromAirport, toAirport);
    if (segment == null) {
      return new ArrayList<>(); 
    }
    return getFlightBySegment(segment, null);
  }

  protected abstract String getFlightSegment(String fromAirport, String toAirport);
  
  protected abstract Long getRewardMilesFromSegment(String segmentId);
    
  protected abstract List<String> getFlightBySegment(String segment, Date deptDate);  
  
  public abstract void storeAirportMapping(AirportCodeMapping mapping);

  public abstract AirportCodeMapping createAirportCodeMapping(String airportCode, 
          String airportName);
  
  public abstract void createNewFlight(String flightSegmentId,
          Date scheduledDepartureTime, Date scheduledArrivalTime,
          int firstClassBaseCost, int economyClassBaseCost,
          int numFirstClassSeats, int numEconomyClassSeats,
          String airplaneTypeId);

  public abstract void storeFlightSegment(String flightSeg);
  
  public abstract void storeFlightSegment(String flightName, String origPort, String destPort, 
          int miles);
  
  public abstract Long countFlightSegments();
  
  public abstract Long countFlights();
  
  public abstract Long countAirports();

  public abstract void dropFlights();

  public abstract String getServiceType();
    
  /**
   * Get reward miles.
   */
  public Long getRewardMiles(String segmentId) {
        
    if (useFlightDataRelatedCaching) {
            
      Long miles = flightSegmentIdtoRewardsCache.get(segmentId);
            
      if (miles == null) {               
        miles = getRewardMilesFromSegment(segmentId);
        if (miles != null && miles != null) {
          flightSegmentIdtoRewardsCache.putIfAbsent(segmentId,miles);
        }
      }
      return miles;
    } else {
      return getRewardMilesFromSegment(segmentId);
    }
        
  }
}
