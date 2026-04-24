/*
Copyright 2013- IBM Corp. All Rights Reserved.

SPDX-License-Identifier: Apache-2.0
*/

package com.acmeair.web;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.CookieParam;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.acmeair.securityutils.ForbiddenException;
import com.acmeair.securityutils.SecurityUtils;
import com.acmeair.service.BookingService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Path("/")
public class BookingServiceRest {

  @Inject
  BookingService bs;

  @Inject
  private SecurityUtils secUtils;

  @Inject
  private RewardTracker rewardTracker;

  private static final Logger logger = Logger.getLogger(BookingServiceRest.class.getName());


  /**
   * Book flights.
   */
  @POST
  @Path("/bookflights")
  @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
  @Produces(MediaType.TEXT_PLAIN)
  public Response bookFlights(@FormParam("userid") String userid,
      @FormParam("toFlightId") String toFlightId,
      @FormParam("toFlightSegId") String toFlightSegId,
      @FormParam("retFlightId") String retFlightId,
      @FormParam("retFlightSegId") String retFlightSegId,
      @FormParam("oneWayFlight") boolean oneWayFlight,
      @CookieParam("jwt_token") String jwtToken) {
    try {

      // make sure the user isn't trying to bookflights for someone else
      if (secUtils.secureUserCalls() && !secUtils.validateJwt(userid, jwtToken)) {
        throw new ForbiddenException();
      }

      String bookingIdTo = bs.bookFlight(userid, toFlightSegId, toFlightId);
      if (rewardTracker.trackRewardMiles()) {
        rewardTracker.updateRewardMiles(userid, toFlightSegId, true);
      }

      String bookingInfo = "";

      String bookingIdReturn = null;
      if (!oneWayFlight) {
        bookingIdReturn = bs.bookFlight(userid, retFlightSegId, retFlightId);
        if (rewardTracker.trackRewardMiles()) {
          rewardTracker.updateRewardMiles(userid, retFlightSegId, true);
        }
        bookingInfo = "{\"oneWay\":false,\"returnBookingId\":\"" + bookingIdReturn + "\",\"departBookingId\":\""
            + bookingIdTo + "\"}";
      } else {
        bookingInfo = "{\"oneWay\":true,\"departBookingId\":\"" + bookingIdTo + "\"}";
      }
      return Response.ok(bookingInfo).build();
    } catch (Exception e) {
      e.printStackTrace();
      return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                     .entity("Error: " + e.getLocalizedMessage())
                     .build();
    }
  }

  /**
   * Get Booking by Number.
   */
  @GET
  @Path("/bybookingnumber/{userid}/{number}")
  public String getBookingByNumber(@PathParam("number") String number, @PathParam("userid") String userid,
      @CookieParam("jwt_token") String jwtToken) {
    try {
      // make sure the user isn't trying to bookflights for someone else
      if (secUtils.secureUserCalls()  && !secUtils.validateJwt(userid, jwtToken)) {
        throw new ForbiddenException();
      }
      return bs.getBooking(userid, number);
    } catch (Exception e) {
      e.printStackTrace();
      return null;
    }
  }

  /**
   * Get bookins for a customer.
   */
  @GET
  @Path("/byuser/{user}")
  public String getBookingsByUser(@PathParam("user") String user,
      @CookieParam("jwt_token") String jwtToken) {

    try {

      logger.fine("getBookingsByUser user: " + user + ", jwtToken: " + jwtToken);

      // make sure the user isn't trying to bookflights for someone else
      if (secUtils.secureUserCalls()  && !secUtils.validateJwt(user, jwtToken)) {
        throw new ForbiddenException();
      }
      return bs.getBookingsByUser(user).toString();
    } catch (Exception e) {
      e.printStackTrace();
      return null;
    }
  }

  /**
   * Cancel bookings.
   */
  @POST
  @Path("/cancelbooking")
  @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
  @Produces(MediaType.TEXT_PLAIN)
  public Response cancelBookingsByNumber(@FormParam("number") String number, @FormParam("userid") String userid,
      @CookieParam("jwt_token") String jwtToken) {
    try {
     
      // make sure the user isn't trying to bookflights for someone else
      if (secUtils.secureUserCalls()  && !secUtils.validateJwt(userid, jwtToken)) {
        throw new ForbiddenException();
      }

      if (rewardTracker.trackRewardMiles()) {
        try {
          ObjectMapper mapper = new ObjectMapper();
          JsonNode booking = mapper.readTree(bs.getBooking(userid, number));
          
          bs.cancelBooking(userid, number);
          rewardTracker.updateRewardMiles(userid, booking.get("flightSegmentId").asText(), false);
        } catch (RuntimeException re) {
          // booking does not exist
          if (logger.isLoggable(Level.FINE)) {
            logger.fine("booking : This booking does not exist: " + number);
          }
        }
      } else {
        bs.cancelBooking(userid, number);
      }

      return Response.ok("booking " + number + " deleted.").build();

    } catch (Exception e) {
      e.printStackTrace();
      return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                     .entity("Error: " + e.getLocalizedMessage())
                     .build();
    }
  }

  @GET
  @Path("/")
  @Produces(MediaType.TEXT_PLAIN)
  public String checkStatus() {
    return "OK";
  }

}
