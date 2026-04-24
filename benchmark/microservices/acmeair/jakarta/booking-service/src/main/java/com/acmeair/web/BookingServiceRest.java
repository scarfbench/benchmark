/*
Copyright 2013- IBM Corp. All Rights Reserved.

SPDX-License-Identifier: Apache-2.0
*/

package com.acmeair.web;

import java.util.logging.Level;
import java.util.logging.Logger;

import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.CookieParam;
import jakarta.ws.rs.FormParam;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

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

  @GET
  @Path("/bybookingnumber/{userid}/{number}")
  public String getBookingByNumber(@PathParam("number") String number, @PathParam("userid") String userid,
      @CookieParam("jwt_token") String jwtToken) {
    try {
      if (secUtils.secureUserCalls()  && !secUtils.validateJwt(userid, jwtToken)) {
        throw new ForbiddenException();
      }
      return bs.getBooking(userid, number);
    } catch (Exception e) {
      e.printStackTrace();
      return null;
    }
  }

  @GET
  @Path("/byuser/{user}")
  public String getBookingsByUser(@PathParam("user") String user,
      @CookieParam("jwt_token") String jwtToken) {
    try {
      logger.fine("getBookingsByUser user: " + user + ", jwtToken: " + jwtToken);
      if (secUtils.secureUserCalls()  && !secUtils.validateJwt(user, jwtToken)) {
        throw new ForbiddenException();
      }
      return bs.getBookingsByUser(user).toString();
    } catch (Exception e) {
      e.printStackTrace();
      return null;
    }
  }

  @POST
  @Path("/cancelbooking")
  @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
  @Produces(MediaType.TEXT_PLAIN)
  public Response cancelBookingsByNumber(@FormParam("number") String number, @FormParam("userid") String userid,
      @CookieParam("jwt_token") String jwtToken) {
    try {
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
