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
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

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

  @GET
  @Path("/byid/{custid}")
  public String getCustomer(@PathParam("custid") String customerid,
      @CookieParam("jwt_token") String jwtToken) {
    if (logger.isLoggable(Level.FINE)) {
      logger.fine("getCustomer : userid " + customerid);
    }
    try {
      if (secUtils.secureUserCalls() && !secUtils.validateJwt(customerid, jwtToken)) {
        throw new ForbiddenException();
      }
      return customerService.getCustomerByUsername(customerid);
    } catch (Exception e) {
      e.printStackTrace();
      return null;
    }
  }

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
            throw new ForbiddenException();
        }
        customerService.updateCustomer(username, customer);
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
