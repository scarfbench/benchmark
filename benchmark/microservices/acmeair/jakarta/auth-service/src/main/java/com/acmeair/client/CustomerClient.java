/*
Copyright 2017- IBM Corp. All Rights Reserved.

SPDX-License-Identifier: Apache-2.0
*/

package com.acmeair.client;

import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Date;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.Form;
import jakarta.ws.rs.core.MultivaluedHashMap;
import jakarta.ws.rs.core.MultivaluedMap;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import com.acmeair.securityutils.SecurityUtils;

@ApplicationScoped
public class CustomerClient {

    @ConfigProperty(name = "customer.service", defaultValue = "localhost:6379/customer")
	String CUSTOMER_SERVICE_LOC;

	private static final String VALIDATE_PATH = "/validateid";

	@Inject
	private SecurityUtils secUtils;

    private static Client client = ClientBuilder.newClient();

	public boolean validateCustomer(String login, String password) {
		String url = "http://" + CUSTOMER_SERVICE_LOC + VALIDATE_PATH;
		String urlParameters = "login=" + login + "&password=" + password;

        MultivaluedMap<String, Object> headers = new MultivaluedHashMap<String, Object>();

		if (secUtils.secureServiceCalls()) {
			Date date = new Date();
			String sigBody;
			String signature;
			try {
				sigBody = secUtils.buildHash(urlParameters);
				signature = secUtils.buildHmac("POST", VALIDATE_PATH, login, date.toString(), sigBody);
			} catch (NoSuchAlgorithmException e) {
				throw new RuntimeException(e);
			} catch (UnsupportedEncodingException e) {
				throw new RuntimeException(e);
			} catch (InvalidKeyException e) {
				throw new RuntimeException(e);
			}

            headers.putSingle("acmeair-id", login);
            headers.putSingle("acmeair-date", date.toString());
            headers.putSingle("acmeair-sig-body", sigBody);
            headers.putSingle("acmeair-signature", signature);
		}

        MultivaluedMap<String, String> map = new MultivaluedHashMap<String, String>();
		map.add("login", login);
		map.add("password", password);

        Entity<Form> request = Entity.form(map);

		CustomerResult result = client.target(url).request()
                                                  .headers(headers)
                                                  .post(request, CustomerResult.class);

		return result.validCustomer;
	}
}
