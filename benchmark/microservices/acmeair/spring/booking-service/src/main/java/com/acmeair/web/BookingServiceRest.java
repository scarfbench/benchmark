/*******************************************************************************
 * Copyright (c) 2013 IBM Corp.
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

package com.acmeair.web;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.acmeair.securityutils.ForbiddenException;
import com.acmeair.securityutils.SecurityUtils;
import com.acmeair.service.BookingService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@RestController
@RequestMapping("/")
public class BookingServiceRest {

  @Autowired
  BookingService bs;

  @Autowired
  private SecurityUtils secUtils;

  @Autowired
  private RewardTracker rewardTracker;

  private static final Logger logger = Logger.getLogger(BookingServiceRest.class.getName());


  /**
   * Book flights.
   */
  @RequestMapping(value = "/bookflights", method = RequestMethod.POST, consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
  public String bookFlights(@RequestParam String userid,
      @RequestParam String toFlightId,
      @RequestParam String toFlightSegId,
      @RequestParam String retFlightId,
      @RequestParam String retFlightSegId,
      @RequestParam boolean oneWayFlight,
      @CookieValue(value = "jwt_token", required = false) String jwtToken) {
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
      return bookingInfo;
    } catch (Exception e) {
      e.printStackTrace();
      throw new InternalServerErrorException();
    }
  }

  /**
   * Get Booking by Number.
   */
  @RequestMapping("/bybookingnumber/{userid}/{number}")
  public String getBookingByNumber(@PathVariable("number") String number, @PathVariable("userid") String userid,
      @CookieValue(value = "jwt_token", required = false) String jwtToken) {
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
  @RequestMapping("/byuser/{user}")
  public String getBookingsByUser(@PathVariable("user") String user,
      @CookieValue(value = "jwt_token", required = false) String jwtToken) {

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
  @RequestMapping(value = "/cancelbooking", method = RequestMethod.POST, consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
  public String cancelBookingsByNumber(@RequestParam String number, @RequestParam String userid,
      @CookieValue(value = "jwt_token", required = false) String jwtToken) {
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

      return "booking " + number + " deleted.";

    } catch (Exception e) {
      e.printStackTrace();
      throw new InternalServerErrorException();
    }
  }

  @RequestMapping("/")
  public String checkStatus() {
    return "OK";
  }

}
