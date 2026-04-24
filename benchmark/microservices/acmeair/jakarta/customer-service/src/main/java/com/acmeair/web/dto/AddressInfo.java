/*
Copyright 2015- IBM Corp. All Rights Reserved.

SPDX-License-Identifier: Apache-2.0
*/

package com.acmeair.web.dto;

import java.io.Serializable;

public class AddressInfo implements Serializable {

  private static final long serialVersionUID = 1L;
  private String streetAddress1;
  private String streetAddress2;
  private String city;
  private String stateProvince;
  private String country;
  private String postalCode;

  public AddressInfo() {
  }

  public AddressInfo(String streetAddress1, String streetAddress2,
          String city, String stateProvince, String country, String postalCode) {
    super();
    this.streetAddress1 = streetAddress1;
    this.streetAddress2 = streetAddress2;
    this.city = city;
    this.stateProvince = stateProvince;
    this.country = country;
    this.postalCode = postalCode;
  }

  public String getStreetAddress1() { return streetAddress1; }
  public void setStreetAddress1(String streetAddress1) { this.streetAddress1 = streetAddress1; }
  public String getStreetAddress2() { return streetAddress2; }
  public void setStreetAddress2(String streetAddress2) { this.streetAddress2 = streetAddress2; }
  public String getCity() { return city; }
  public void setCity(String city) { this.city = city; }
  public String getStateProvince() { return stateProvince; }
  public void setStateProvince(String stateProvince) { this.stateProvince = stateProvince; }
  public String getCountry() { return country; }
  public void setCountry(String country) { this.country = country; }
  public String getPostalCode() { return postalCode; }
  public void setPostalCode(String postalCode) { this.postalCode = postalCode; }

  @Override
  public String toString() {
    return "CustomerAddress [streetAddress1=" + streetAddress1
            + ", streetAddress2=" + streetAddress2 + ", city=" + city
            + ", stateProvince=" + stateProvince + ", country=" + country
            + ", postalCode=" + postalCode + "]";
  }
}
