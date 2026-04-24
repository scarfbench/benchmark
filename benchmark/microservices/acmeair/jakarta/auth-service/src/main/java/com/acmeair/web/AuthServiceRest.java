/*
Copyright 2016- IBM Corp. All Rights Reserved.

SPDX-License-Identifier: Apache-2.0
*/

package com.acmeair.web;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import jakarta.inject.Inject;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.FormParam;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;

import com.acmeair.client.CustomerClient;
import com.acmeair.securityutils.ForbiddenException;
import com.acmeair.securityutils.SecurityUtils;

@Path("/")
public class AuthServiceRest {

	private static final Logger logger = Logger.getLogger(AuthServiceRest.class.getName());

	public static final String JWT_COOKIE_NAME = "jwt_token";
	public static final String USER_COOKIE_NAME = "loggedinuser";

	@Inject
	private CustomerClient customerClient;

	@Inject
	private SecurityUtils secUtils;

    @POST
	@Path("/login")
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response login(@FormParam("login") String login, @FormParam("password") String password) {
		try {
			if (logger.isLoggable(Level.FINE)) {
				logger.fine("attempting to login : login " + login + " password " + password);
			}

			if (!validateCustomer(login, password)) {
				throw new ForbiddenException("Invalid username or password for " + login);
			}

			String token = "";
			if (secUtils.secureUserCalls()) {
				token = secUtils.generateJwt(login);
			}

			Map<String, String> model = new HashMap<>();
            model.put("token", token);
            model.put("login", login);

            return Response.ok(model, MediaType.APPLICATION_OCTET_STREAM).build();

		} catch (Exception e) {
			e.printStackTrace();
			return Response.status(Response.Status.FORBIDDEN)
			               .entity("Error: " + e.getLocalizedMessage())
			               .build();
		}
	}

    @GET
    @Path("/")
	public String checkStatus() {
		return "OK";
	}

	private boolean validateCustomer(String login, String password) {
		return customerClient.validateCustomer(login, password);
	}
}
