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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.acmeair.securityutils.ForbiddenException;
import com.acmeair.securityutils.SecurityUtils;
import com.acmeair.service.CustomerService;
import com.acmeair.web.dto.AddressInfo;
import com.acmeair.web.dto.CustomerInfo;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@RestController
@RequestMapping("/")
public class CustomerServiceRest {

  @Autowired
  CustomerService customerService;

  @Autowired
  private SecurityUtils secUtils;

  private static final Logger logger = Logger.getLogger(CustomerServiceRest.class.getName());

  /**
   * Get customer info.
   */
  @RequestMapping(value = "/byid/{custid}")
  public String getCustomer(@PathVariable("custid") String customerid,
      @CookieValue(value = "jwt_token", required = false) String jwtToken) {
    if (logger.isLoggable(Level.FINE)) {
      logger.fine("getCustomer : userid " + customerid);
    }

    try {
      // make sure the user isn't trying to update a customer other than the one
      // currently logged in
      if (secUtils.secureUserCalls() && !secUtils.validateJwt(customerid, jwtToken)) {
        throw new ForbiddenException();
      }

      return customerService.getCustomerByUsername(customerid);

    } catch (Exception e) {
      e.printStackTrace();
      return null;
    }
  }

  /**
   * Update customer.
   */
  @RequestMapping(value = "/byid/{custid}", method = RequestMethod.POST)
  public String putCustomer(@RequestBody CustomerInfo customer,
      @CookieValue(value = "jwt_token", required = false) String jwtToken) {

    String username = customer.get_id();

    if (secUtils.secureUserCalls() && !secUtils.validateJwt(username, jwtToken)) {
      throw new ForbiddenException();
    }

    String customerFromDb = customerService.getCustomerByUsernameAndPassword(username, customer.getPassword());

    if (logger.isLoggable(Level.FINE)) {
      logger.fine("putCustomer : " + customerFromDb);
    }

    if (customerFromDb == null) {
      // either the customer doesn't exist or the password is wrong
      throw new ForbiddenException();
    }

    customerService.updateCustomer(username, customer);

    // Retrieve the latest results
    customerFromDb = customerService.getCustomerByUsernameAndPassword(username, customer.getPassword());

    return customerFromDb;
  }

  /**
   * Validate user/password.
   */
  @RequestMapping(value = "/validateid", method = RequestMethod.POST, consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE, produces = "application/json")
  public ValidateCustomerResponse validateCustomer(@RequestHeader(name = "acmeair-id", required = false) String headerId,
      @RequestHeader(name = "acmeair-date", required = false) String headerDate, @RequestHeader(name = "acmeair-sig-body", required = false) String headerSigBody,
      @RequestHeader(name = "acmeair-signature", required = false) String headerSig, @RequestParam String login, @RequestParam String password) {


    if (logger.isLoggable(Level.FINE)) {
      logger.fine("validateid : login " + login + " password " + password);
    }

    // verify header
    if (secUtils.secureServiceCalls()) {
      String body = "login=" + login + "&password=" + password;
      secUtils.verifyBodyHash(body, headerSigBody);
      secUtils.verifyFullSignature("POST", "/validateid", headerId, headerDate, headerSigBody, headerSig);
    }

    Boolean validCustomer = customerService.validateCustomer(login, password);

    ValidateCustomerResponse result = new ValidateCustomerResponse();
    result.validCustomer = validCustomer;

    return result;
  }

  /**
   * Update reward miles.
   */
  @RequestMapping(value = "/updateCustomerTotalMiles", method = RequestMethod.POST, consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE, produces = "application/json")
  public UpdateMilesResult updateCustomerTotalMiles(@RequestHeader(name = "acmeair-id", required = false) String headerId,
      @RequestHeader(name = "acmeair-date", required = false) String headerDate, @RequestHeader(name = "acmeair-sig-body", required = false) String headerSigBody,
      @RequestHeader(name = "acmeair-signature", required = false) String headerSig,
      @RequestParam String customerid,  @RequestParam Long miles) {

    try {
      if (secUtils.secureServiceCalls()) {
        String body = "miles=" + miles + "&customerid=" + customerid;
        secUtils.verifyBodyHash(body, headerSigBody);
        secUtils.verifyFullSignature("POST", "/updateCustomerTotalMiles", headerId, headerDate, headerSigBody,
            headerSig);
      }

      ObjectMapper mapper = new ObjectMapper();
      JsonNode customerJson = mapper.readTree(customerService.getCustomerByUsername(customerid));

      JsonNode addressJson = customerJson.get("address");

      String streetAddress2 = null;

      if (addressJson.get("streetAddress2") != null
          && !addressJson.get("streetAddress2").toString().equals("null")) {
        streetAddress2 = addressJson.get("streetAddress2").asText();
      }

      AddressInfo addressInfo = new AddressInfo(addressJson.get("streetAddress1").asText(), streetAddress2,
          addressJson.get("city").asText(), addressJson.get("stateProvince").asText(),
          addressJson.get("country").asText(), addressJson.get("postalCode").asText());

      Long milesUpdate = Integer.parseInt(customerJson.get("total_miles").asText()) + miles;
      CustomerInfo customerInfo = new CustomerInfo(customerid, null, customerJson.get("status").asText(),
          milesUpdate.intValue(), Integer.parseInt(customerJson.get("miles_ytd").asText()), addressInfo,
          customerJson.get("phoneNumber").asText(), customerJson.get("phoneNumberType").asText());

      customerService.updateCustomer(customerid, customerInfo);

      UpdateMilesResult result = new UpdateMilesResult();
      result.total_miles = milesUpdate;

      return result;

    } catch (Exception e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
      throw new InternalServerErrorException();
    }
  }

  @RequestMapping("/")
  public String checkStatus() {
    return "OK";

  }
}
