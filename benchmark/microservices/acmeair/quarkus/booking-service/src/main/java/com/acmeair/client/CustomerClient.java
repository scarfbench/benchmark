/*
Copyright 2017- IBM Corp. All Rights Reserved.

SPDX-License-Identifier: Apache-2.0
*/

package com.acmeair.client;

import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Date;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Form;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.resteasy.client.jaxrs.engines.URLConnectionEngine;
import org.jboss.resteasy.client.jaxrs.internal.ResteasyClientBuilderImpl;

import com.acmeair.securityutils.SecurityUtils;

@ApplicationScoped
public class CustomerClient {
    
    private static Client client = new ResteasyClientBuilderImpl()
//                                        .httpEngine(new VertxClientHttpEngine)
                                        .httpEngine(new URLConnectionEngine())
                                        .build();
    private static final String UPDATE_REWARD_PATH = "/updateCustomerTotalMiles";
    
    /**
     * Accepts environment variable override CUSTOMER_SERVICE
     */
    @ConfigProperty(name = "customer.service", defaultValue = "localhost:6379/customer")
    protected String CUSTOMER_SERVICE_LOC;
  
    @Inject
    private SecurityUtils secUtils;   

	/**
	 * call customer to update reward miles.
	 */
	public Long updateTotalMiles(String customerId, String miles) {
		String customerUrl = "http://" + CUSTOMER_SERVICE_LOC + UPDATE_REWARD_PATH;
		String customerParameters = "miles=" + miles + "&customerid=" + customerId;
		
		MultivaluedMap<String, Object> headers = new MultivaluedHashMap<>();

		if (secUtils.secureServiceCalls()) {

			Date date = new Date();
			String sigBody;
			String signature;
			try {
				sigBody = secUtils.buildHash(customerParameters);
				signature = secUtils.buildHmac("POST", UPDATE_REWARD_PATH, customerId, date.toString(), sigBody);
			} catch (NoSuchAlgorithmException e) {
				throw new RuntimeException(e);
			} catch (UnsupportedEncodingException e) {
				throw new RuntimeException(e);
			} catch (InvalidKeyException e) {
				throw new RuntimeException(e);
			}

			headers.putSingle("acmeair-id", customerId);
			headers.putSingle("acmeair-date", date.toString());
			headers.putSingle("acmeair-sig-body", sigBody);
			headers.putSingle("acmeair-signature", signature);
		}

		MultivaluedMap<String, String> map = new MultivaluedHashMap<>();
		map.add("miles", miles);
		map.add("customerid", customerId);

		Entity<Form> request = Entity.form(map);

		UpdateTotalMilesResult result = client.target(customerUrl)
		                                      .request()
											  .headers(headers)
											  .post(request, UpdateTotalMilesResult.class);
		return result.total_miles;
	}
}
