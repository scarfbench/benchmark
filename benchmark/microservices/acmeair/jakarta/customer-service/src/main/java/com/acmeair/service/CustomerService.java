/*
Copyright 2013- IBM Corp. All Rights Reserved.

SPDX-License-Identifier: Apache-2.0
*/

package com.acmeair.service;

import java.io.IOException;

import jakarta.inject.Inject;

import com.acmeair.web.dto.CustomerInfo;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public abstract class CustomerService {
	protected static final int DAYS_TO_ALLOW_SESSION = 1;

    @Inject
	protected KeyGenerator keyGenerator;

	public abstract void createCustomer(String username, String password, String status, int totalMiles, int milesYtd,
			String phoneNumber, String phoneNumberType, String addressJson);

	public abstract String createAddress(String streetAddress1, String streetAddress2, String city,
			String stateProvince, String country, String postalCode);

	public abstract void updateCustomer(String username, CustomerInfo customerJson);

	protected abstract String getCustomer(String username);

	public abstract String getCustomerByUsername(String username);

	public boolean validateCustomer(String username, String password) {
		boolean validatedCustomer = false;
		String customerToValidate = getCustomer(username);
		if (customerToValidate != null) {
			ObjectMapper mapper = new ObjectMapper();
			JsonNode customerJson;
			try {
				customerJson = mapper.readTree(customerToValidate);
			} catch (JsonProcessingException e) {
				throw new RuntimeException(e);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
			validatedCustomer = password.equals(customerJson.get("password").asText());
		}
		return validatedCustomer;
	}

	public String getCustomerByUsernameAndPassword(String username, String password) {
		String c = getCustomer(username);
		ObjectMapper mapper = new ObjectMapper();
		JsonNode customerJson;
		try {
			customerJson = mapper.readTree(c);
		} catch (JsonProcessingException e) {
			throw new RuntimeException(e);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		if (!customerJson.get("password").asText().equals(password)) {
			return null;
		}
		return c;
	}

	public abstract Long count();

	public abstract void dropCustomers();

	public abstract String getServiceType();

}
