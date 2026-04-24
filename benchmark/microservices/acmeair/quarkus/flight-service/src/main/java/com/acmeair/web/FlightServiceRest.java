/*
Copyright 2013- IBM Corp. All Rights Reserved.

SPDX-License-Identifier: Apache-2.0
*/

package com.acmeair.web;

import java.util.Date;
import java.util.List;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.acmeair.securityutils.SecurityUtils;
import com.acmeair.service.FlightService;

@Path("/")
public class FlightServiceRest {


  @Inject
  private FlightService flightService;

  @Inject
  private SecurityUtils secUtils;

  /**
   * Get flights.
   */
  
  @POST
  @Path("/queryflights")
  @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
  public String getTripFlights(@FormParam("fromAirport") String fromAirport, @FormParam("toAirport") String toAirport, 
      @FormParam("fromDate") Date fromDate, @FormParam("returnDate") Date returnDate, @FormParam("oneWay") Boolean oneWay) {


    String options = "";

    List<String> toFlights = flightService.getFlightByAirportsAndDepartureDate(fromAirport, 
        toAirport, fromDate);

    if (!oneWay) {
      

      List<String> retFlights = flightService.getFlightByAirportsAndDepartureDate(toAirport, 
          fromAirport, returnDate);

      // TODO: Why are we doing it like this?
      options = "{\"tripFlights\":"  
          + "[{\"numPages\":1,\"flightsOptions\": " 
          + toFlights 
          + ",\"currentPage\":0,\"hasMoreOptions\":false,\"pageSize\":10}, " 
          + "{\"numPages\":1,\"flightsOptions\": " 
          + retFlights 
          + ",\"currentPage\":0,\"hasMoreOptions\":false,\"pageSize\":10}], " 
          + "\"tripLegs\":2}";
    } else {
      options = "{\"tripFlights\":" 
          + "[{\"numPages\":1,\"flightsOptions\": " 
          + toFlights 
          + ",\"currentPage\":0,\"hasMoreOptions\":false,\"pageSize\":10}], " 
          + "\"tripLegs\":1}";
    }
    
    return options;
  }

  /**
   * Get reward miles for flight segment.
   */
  @POST
  @Path("/getrewardmiles")
  @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
  @Produces(MediaType.APPLICATION_JSON)
  public RewardMilesResponse getRewardMiles(
      @HeaderParam("acmeair-id") String headerId,
      @HeaderParam("acmeair-date") String headerDate,
      @HeaderParam("acmeair-sig-body") String headerSigBody, 
      @HeaderParam("acmeair-signature") String headerSig,
      @FormParam("flightSegment") String flightSegment

      ) {
    
    if (secUtils.secureServiceCalls()) { 
      String body = "flightSegment=" + flightSegment;
      secUtils.verifyBodyHash(body, headerSigBody);
      secUtils.verifyFullSignature("POST", "/getrewardmiles",headerId,headerDate,
          headerSigBody,headerSig);
    }

    Long miles = flightService.getRewardMiles(flightSegment); 
    RewardMilesResponse result = new RewardMilesResponse();
    result.miles = miles;
    return result;
  }

  @GET
  @Path("/")
  @Produces(MediaType.TEXT_PLAIN)
  public String checkStatus() {
    return "OK";
  }
}
