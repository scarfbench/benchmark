/*******************************************************************************
* Copyright (c) 2013-2015 IBM Corp.
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

package com.acmeair.service;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;

import com.acmeair.web.dto.CustomerInfo;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public abstract class CustomerService {
	protected static final int DAYS_TO_ALLOW_SESSION = 1;

	@Autowired
	protected KeyGenerator keyGenerator;

	public abstract void createCustomer(String username, String password, String status, int totalMiles, int milesYtd,
			String phoneNumber, String phoneNumberType, String addressJson);

	public abstract String createAddress(String streetAddress1, String streetAddress2, String city,
			String stateProvince, String country, String postalCode);

	public abstract void updateCustomer(String username, CustomerInfo customerJson);

	protected abstract String getCustomer(String username);

	public abstract String getCustomerByUsername(String username);

	/**
	 * Validate password for customer.
	 */
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

	/**
	 * Get customer info.
	 */
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

		// Should we also set the password to null?
		return c;
	}

	public abstract Long count();

	public abstract void dropCustomers();

	public abstract String getServiceType();

}
