/*
Copyright 2015- IBM Corp. All Rights Reserved.

SPDX-License-Identifier: Apache-2.0
*/

package com.acmeair.web.dto;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonProperty;

public class CustomerInfo implements Serializable {

  private static final long serialVersionUID = 1L;

  @JsonProperty("_id")
  private String _id;

  @JsonProperty("password")
  private String password;

  @JsonProperty("status")
  private String status;

  @JsonProperty("total_miles")
  private int totalMiles;

  @JsonProperty("miles_ytd")
  private int milesYtd;

  @JsonProperty("address")
  private AddressInfo address;

  @JsonProperty("phoneNumber")
  private String phoneNumber;

  @JsonProperty("phoneNumberType")
  private String phoneNumberType;

  public CustomerInfo() {
  }

  public CustomerInfo(String username, String password, String status, int totalMiles,
      int milesYtd, AddressInfo address, String phoneNumber, String phoneNumberType) {
    this._id = username;
    this.password = password;
    this.status = status;
    this.totalMiles = totalMiles;
    this.milesYtd = milesYtd;
    this.address = address;
    this.phoneNumber = phoneNumber;
    this.phoneNumberType = phoneNumberType;
  }

  public String get_id() { return _id; }
  public void setId(String username) { this._id = username; }
  public String getPassword() { return password; }
  public void setPassword(String password) { this.password = password; }
  public String getStatus() { return status; }
  public void setStatus(String status) { this.status = status; }
  public int getTotalMiles() { return totalMiles; }
  public void setTotalMiles(int totalMiles) { this.totalMiles = totalMiles; }
  public int getMilesYtd() { return milesYtd; }
  public void setMilesYtd(int milesYtd) { this.milesYtd = milesYtd; }
  public String getPhoneNumber() { return phoneNumber; }
  public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
  public String getPhoneNumberType() { return phoneNumberType; }
  public void setPhoneNumberType(String phoneNumberType) { this.phoneNumberType = phoneNumberType; }
  public AddressInfo getAddress() { return address; }
  public void setAddress(AddressInfo address) { this.address = address; }

  @Override
  public String toString() {
    return "Customer [id=" + _id + ", password=" + password + ", status="
        + status + ", total_miles=" + totalMiles + ", miles_ytd="
        + milesYtd + ", address=" + address + ", phoneNumber="
        + phoneNumber + ", phoneNumberType=" + phoneNumberType + "]";
  }
}
