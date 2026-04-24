/*
Copyright 2016- IBM Corp. All Rights Reserved.

SPDX-License-Identifier: Apache-2.0
*/

package com.acmeair.web;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.inject.Inject;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;

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

	/**
	 * Login with username/password.
	 * 
	 */
    @POST
	@Path("/login")
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response login(@FormParam("login") String login, @FormParam("password") String password) {
		// Test: curl -d 'login=user1' -d 'password=letmein' http://localhost:8080/login
		try {
			
			if (logger.isLoggable(Level.FINE)) {
				logger.fine("attempting to login : login " + login + " password " + password);
			}

			if (!validateCustomer(login, password)) {
				throw new ForbiddenException("Invalid username or password for " + login);
			}
			
			// Generate simple JWT with login as the Subject
			String token = "";
			if (secUtils.secureUserCalls()) {
				token = secUtils.generateJwt(login);
			}
			
			// We need to pass the login and token to the CookieInterceptor so that it can
			// set response headers:
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
