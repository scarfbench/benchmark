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
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.acmeair.securityutils.ForbiddenException;
import com.acmeair.securityutils.SecurityUtils;
import com.acmeair.service.CustomerService;
import com.acmeair.web.dto.AddressInfo;
import com.acmeair.web.dto.CustomerInfo;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Path("/")
public class CustomerServiceRest {

  @Inject
  CustomerService customerService;

  @Inject
  private SecurityUtils secUtils;

  private static final Logger logger = Logger.getLogger(CustomerServiceRest.class.getName());

  /**
   * Get customer info.
   */
  @GET
  @Path("/byid/{custid}")
  public String getCustomer(@PathParam("custid") String customerid,
      @CookieParam("jwt_token") String jwtToken) {
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
  @POST
  @Path("/byid/{custid}")
  @Produces(MediaType.TEXT_PLAIN)
  public Response putCustomer(CustomerInfo customer,
      @CookieParam("jwt_token") String jwtToken) {

    try {
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

        return Response.ok(customerFromDb).build();

    } catch (ForbiddenException e) {
        e.printStackTrace();
        return Response.status(Response.Status.FORBIDDEN)
                       .entity("Error: " + e.getLocalizedMessage())
                       .build();
    } catch (Exception e) {
        e.printStackTrace();
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                       .entity("Error: " + e.getLocalizedMessage())
                       .build();
    }
}

  /**
   * Validate user/password.
   */
  @POST
  @Path("/validateid")
  @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
  @Produces(MediaType.APPLICATION_JSON)
  public Response validateCustomer(@HeaderParam("acmeair-id") String headerId,
      @HeaderParam("acmeair-date") String headerDate, @HeaderParam("acmeair-sig-body") String headerSigBody,
      @HeaderParam("acmeair-signature") String headerSig, @FormParam("login") String login, @FormParam("password") String password) {

    try {
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

        return Response.ok(result).build();

    } catch (ForbiddenException e) {
        e.printStackTrace();
        return Response.status(Response.Status.FORBIDDEN)
                       .entity("Error: " + e.getLocalizedMessage())
                       .build();
    } catch (Exception e) {
        e.printStackTrace();
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                       .entity("Error: " + e.getLocalizedMessage())
                       .build();
    }
  }

  /**
   * Update reward miles.
   */
  @POST
  @Path(value = "/updateCustomerTotalMiles")
  @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
  @Produces(MediaType.APPLICATION_JSON)
  public Response updateCustomerTotalMiles(@HeaderParam("acmeair-id") String headerId,
      @HeaderParam("acmeair-date") String headerDate, @HeaderParam("acmeair-sig-body") String headerSigBody,
      @HeaderParam("acmeair-signature") String headerSig,
      @FormParam("customerid") String customerid,  @FormParam("miles") Long miles) {

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

      return Response.ok(result).build();

    } catch (Exception e) {
      // TODO Auto-generated catch block
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
