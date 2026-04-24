/*
Copyright 2013- IBM Corp. All Rights Reserved.

SPDX-License-Identifier: Apache-2.0
*/

package com.acmeair;

// Not sure this is the right place for this class, or if we really need it.
public class AirportCodeMapping {

  private String id;
  private String airportName;

  public AirportCodeMapping() {
  }

  public AirportCodeMapping(String airportCode, String airportName) {
    this.id = airportCode;
    this.airportName = airportName;
  }

  public String getAirportCode() {
    return id;
  }

  public void setAirportCode(String airportCode) {
    this.id = airportCode;
  }

  public String getAirportName() {
    return airportName;
  }

  public void setAirportName(String airportName) {
    this.airportName = airportName;
  }

  public String toJson() {
    return "{ _id : \"" + id + "\", airportName : \"" + airportName + "\"}";
  }
}
