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
public class FlightClient {

    private static Client client = ClientBuilder.newClient();
    private static final String GET_REWARD_PATH = "/getrewardmiles";

    @ConfigProperty(name = "flight.service", defaultValue = "localhost:6379/customer")
    protected String FLIGHT_SERVICE_LOC;

    @Inject
    private SecurityUtils secUtils;

	public Long getRewardMiles(String customerId, String flightSegId, boolean add) {
		String flightUrl = "http://" + FLIGHT_SERVICE_LOC + GET_REWARD_PATH;
		String flightParameters = "flightSegment=" + flightSegId;

		MultivaluedMap<String, Object> headers = new MultivaluedHashMap<>();

		if (secUtils.secureServiceCalls()) {
			Date date = new Date();
			String sigBody;
			String signature;
			try {
				sigBody = secUtils.buildHash(flightParameters);
				signature = secUtils.buildHmac("POST", GET_REWARD_PATH, customerId, date.toString(), sigBody);
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
		map.add("flightSegment", flightSegId);

		Entity<Form> request = Entity.form(map);

		FlightServiceGetRewardsResult result = client.target(flightUrl)
		                                             .request()
													 .headers(headers)
													 .post(request, FlightServiceGetRewardsResult.class);

		Long miles = result.miles;
		if (!add) {
			miles = miles * -1;
		}
		return miles;
	}
}
