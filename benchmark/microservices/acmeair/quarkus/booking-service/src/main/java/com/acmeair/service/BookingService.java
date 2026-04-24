/*
Copyright 2013- IBM Corp. All Rights Reserved.

SPDX-License-Identifier: Apache-2.0
*/

package com.acmeair.service;

import java.util.List;

public interface BookingService {

  // String bookFlight(String customerId, FlightPK flightId);
  // String bookFlight(String customerId, String flightId);

  String bookFlight(String customerId, String flightSegmentId, String flightId);

  String getBooking(String user, String id);

  List<String> getBookingsByUser(String user);

  void cancelBooking(String user, String id);

  Long count();

  void dropBookings();

  String getServiceType();
}
