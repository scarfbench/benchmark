/**
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.ibm.sample.daytrader.accounts.controller;

// DayTrader
import com.ibm.sample.daytrader.accounts.beans.RunStatsDataBean;
import com.ibm.sample.daytrader.accounts.service.AccountsService;
import com.ibm.sample.daytrader.accounts.utils.Log;

// Java
import java.math.BigDecimal;
import java.util.Map;

import javax.ws.rs.NotAuthorizedException;

// Daytrader
import com.ibm.sample.daytrader.accounts.entities.AccountDataBean;
import com.ibm.sample.daytrader.accounts.entities.AccountProfileDataBean;

// Spring
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * API endpoints are documented using Swagger UI. 
 *
 * @see <a href="https://">{accountsHost}:{accountsPort}/swagger-ui.html</a>
 *
 * HTTP Methods. There is no official and enforced standard for designing HTTP & RESTful APIs.
 * There are many ways for designing them. These notes cover the guidelines used for designing
 * the aforementioned HTTP & RESTful API.
 *
 * - GET is used for reading objects; no cache headers is used if the object should not be cached
 *
 * - POST is used for creating objects or for operations that change the server side state. In the
 *   case where an object is created, the created object is returned; instead of it'd URI. This is 
 *   in keeping with the existing services. A better practice is to return the URI to the created
 *   object, but we elected to keep the REST APIs consistent with the existing services. New APIs 
 *   that return the URI can be added during Stage 04: Microservices if required. 
 *
 * - PUT is used for full updates
 *
 * - PATCH is used for partial updates
 *
 * TODO:
 * 1.	Access Control
 *		The controller provides a centralized location for access control. Currently,
 *		the application does not check to see is a user is logged in before invoking
 *		a method. So access control checks should be added in Stage 04: Microservices 
 */

@RestController
public class AccountsController
{
	private static AccountsService accountsService = new AccountsService();

	//
	// Account Related Endpoints
	//
	@RequestMapping(
			value = "/accounts/test",
			method = RequestMethod.POST,
			consumes = "application/json"
	)
	public ResponseEntity<String> testAPIRequest(
			@RequestBody Map<String,String> valMap ) {
		return new ResponseEntity<String>(valMap.get("key"), HttpStatus.OK);
	}
	/**
	 * REST call to register the user provided in the request body.
	 * 
	 */
	@RequestMapping(value = "/accounts", method = RequestMethod.POST)
	public ResponseEntity<AccountDataBean> register (
			@RequestBody AccountDataBean accountData) 
	{
		Log.traceEnter("AccountsController.register(" + accountData.getProfileID() + ")");
		
		// Get the registration data
		String userID = accountData.getProfileID();
		String password = accountData.getProfile().getPassword();
		String fullname = accountData.getProfile().getFullName();
		String address = accountData.getProfile().getAddress();
		String email = accountData.getProfile().getEmail();
		String creditCard = accountData.getProfile().getCreditCard();
		BigDecimal openBalance = accountData.getOpenBalance();
	
		try
		{
			// Registers the user and publishes the registered user event
			accountData = accountsService.register(userID, password, fullname, address, email, creditCard, openBalance);
            Log.traceExit("AccountsController.register()");
			return new ResponseEntity<AccountDataBean>(accountData, getNoCacheHeaders(), HttpStatus.CREATED);
		}
     	catch(Throwable t)
     	{
     		Log.error("AccountsController.register()",t);
			return new ResponseEntity<AccountDataBean>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}	
	
	/**
	 * REST call to update the account profile for given the user id.
	 * 
	 */
	@RequestMapping(value = "/accounts/{userId}/profiles", method = RequestMethod.PUT)
	public ResponseEntity<AccountProfileDataBean> updateAccountProfile(
			@PathVariable("userId") String userId, 
			@RequestBody AccountProfileDataBean profileData)
	{
		Log.traceEnter("AccountsController.updateAccountProfile()");
		try
		{
			profileData = accountsService.updateAccountProfile(profileData);
			if (profileData != null) 
			{
	            Log.traceExit("AccountsController.updateAccountProfile()");
				return new ResponseEntity<AccountProfileDataBean>(profileData, getNoCacheHeaders(), HttpStatus.OK);
			}
			else
			{
	            Log.traceExit("AccountsController.updateAccountProfile()");
				return new ResponseEntity<AccountProfileDataBean>(profileData, getNoCacheHeaders(), HttpStatus.NO_CONTENT);
			}
		}
     	catch(Throwable t)
     	{
     		Log.error("AccountsController.updateAccountProfile()",t);
			return new ResponseEntity<AccountProfileDataBean>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	/**
	 * REST call to get the user's profile
	 * 
	 */
	@RequestMapping(value = "/accounts/{userId}/profiles", method = RequestMethod.GET)
	public ResponseEntity<AccountProfileDataBean> getAccountProfileData(@PathVariable("userId") String userId) 
	{
		Log.traceEnter("AccountsController.getAccountProfileData()");	
		AccountProfileDataBean profileData = null;
		try
		{
			profileData = accountsService.getAccountProfileData(userId);
			if (profileData != null)
			{
	            Log.traceExit("AccountsController.getAccountProfileData()");
				return new ResponseEntity<AccountProfileDataBean>(profileData, getNoCacheHeaders(), HttpStatus.OK);
			}
			else
			{
				Log.traceExit("AccountsController.getAccountProfileData()");
				return new ResponseEntity<AccountProfileDataBean>(profileData, getNoCacheHeaders(), HttpStatus.NO_CONTENT);
			}
		}
     	catch(Throwable t)
     	{
     		Log.error("AccountsController.getAccountProfileData()",t);
			return new ResponseEntity<AccountProfileDataBean>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
	
	/**
	 * REST call to get the user's account
	 * 
	 */
	@RequestMapping(value = "/accounts/{userId}", method = RequestMethod.GET)
	public ResponseEntity<AccountDataBean> getAccountData(@PathVariable("userId") String userId) 
	{
		Log.traceEnter("AccountsController.getAccountData()");
		AccountDataBean accountData = null;
		try
		{
			accountData = accountsService.getAccountData(userId);
			if (accountData != null) 
			{
	            Log.traceExit("AccountsController.getAccountData()");
				return new ResponseEntity<AccountDataBean>(accountData, getNoCacheHeaders(), HttpStatus.OK);
			}
			else
			{
	            Log.traceExit("AccountsController.getAccountData()");
				return new ResponseEntity<AccountDataBean>(accountData, getNoCacheHeaders(), HttpStatus.NO_CONTENT);
			}
		}
     	catch(Throwable t)
     	{
     		Log.error("AccountsController.getAccountData()",t);
			return new ResponseEntity<AccountDataBean>(HttpStatus.INTERNAL_SERVER_ERROR);
		}

	}

	//
	// Authentication Related Endpoints
	//

	/**
	 * REST call to login the user from the authentication data passed in the body.
	 * 
	 * @return The new version of the account
	 */
	
	@RequestMapping(value = "/login/{userId}", method = RequestMethod.PATCH)
	public ResponseEntity<AccountDataBean> login (@PathVariable("userId") String userId, @RequestBody String password)
	{
		Log.traceEnter("AccountsController.login()");
		AccountDataBean accountData = null;
		try
		{
			accountData = accountsService.login(userId, password); 
            Log.traceExit("AccountsController.login()");
			return new ResponseEntity<AccountDataBean>(accountData, getNoCacheHeaders(), HttpStatus.OK);
		}
		catch(NotAuthorizedException nae)
		{
     		Log.error("AccountsController.login()",nae);
			return new ResponseEntity<AccountDataBean>(HttpStatus.UNAUTHORIZED);			
		}
     	catch(Throwable t)
	    {
     		Log.error("AccountsController.login()",t);
			return new ResponseEntity<AccountDataBean>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	/**
	 * REST call to logout the user with the given userid.
	 * 
	 */
	@RequestMapping(value = "/logout/{userId}", method = RequestMethod.PATCH)
	public ResponseEntity<Boolean> logout(@PathVariable("userId") String userId)
	{
		Log.traceEnter("AccountsController.logout()");
		try
		{
			accountsService.logout(userId);
            Log.traceExit("AccountsController.logout()");
			return new ResponseEntity<Boolean>(true,HttpStatus.OK);
		}
     	catch(Throwable t)
     	{
     		Log.error("AccountsController.logout()",t);
			return new ResponseEntity<Boolean>(HttpStatus.INTERNAL_SERVER_ERROR);
		}	
	}
	
	//
	// Admin Related Endpoints
	//
	
	/**
	 * REST call to register the specified number of users.
	 * 
	 */
	@RequestMapping(value = "/admin/tradeBuildDB", method = RequestMethod.POST)
	public ResponseEntity<Boolean> tradeBuildDB( 
			@RequestParam(value = "limit") Integer limit,
			@RequestParam(value = "offset") Integer offset) 
	{
		Log.traceEnter("AccountsController.tradeBuildDB()");
		Boolean result = false;
		try
		{
			// Register the sample users
			result = accountsService.tradeBuildDB(limit.intValue(), offset.intValue());
            Log.traceExit("AccountsController.tradeBuildDB()");
			return new ResponseEntity<Boolean>(result, getNoCacheHeaders(), HttpStatus.CREATED);
		}
     	catch(Throwable t)
     	{
     		Log.error("AccountsController.tradeBuildDB()",t);
			return new ResponseEntity<Boolean>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
	
	/**
	 * REST call to recreate dbtables
	 * 
	 */
	@RequestMapping(value = "/admin/recreateDBTables", method = RequestMethod.POST)
	public ResponseEntity<Boolean> recreateDBTables() 
	{	
		Log.traceEnter("AccountsController.recreateDBTables()");	
		Boolean result = false;		
		try
		{
			result = accountsService.recreateDBTables();
			Log.traceExit("AccountsController.recreateDBTables()");
			return new ResponseEntity<Boolean>(result, getNoCacheHeaders(), HttpStatus.CREATED);
		}
     	catch(Throwable t)
     	{
     		Log.error("AccountsController.recreateDBTables()",t);
			return new ResponseEntity<Boolean>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
		
	/**
	 * REST call to reset trades and return the usage statistics
	 * 
	 */
	@RequestMapping(value = "/admin/resetTrade", method = RequestMethod.GET)
	public ResponseEntity<RunStatsDataBean> resetTrade(@RequestParam(value="deleteAll") Boolean deleteAll)
	{
		Log.traceEnter("AccountsController.resetTrade()");			
		RunStatsDataBean runStatsData = null;
		try
		{
			runStatsData = accountsService.resetTrade(deleteAll);
			if (runStatsData != null)
			{
	            Log.traceExit("AccountsController.resetTrade()");
				return new ResponseEntity<RunStatsDataBean>(runStatsData, getNoCacheHeaders(), HttpStatus.OK);
			}
			else
			{
	            Log.traceExit("AccountsController.resetTrade()");
				return new ResponseEntity<RunStatsDataBean>(runStatsData, getNoCacheHeaders(), HttpStatus.NO_CONTENT);				
			}
		}
     	catch(Throwable t)
     	{
     		Log.error("AccountsController.resetTrade()",t);
			return new ResponseEntity<RunStatsDataBean>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
		
	//
	// Private helper functions
	//
	
	private HttpHeaders getNoCacheHeaders() 
	{
		HttpHeaders responseHeaders = new HttpHeaders();
		responseHeaders.set("Cache-Control", "no-cache");
		return responseHeaders;
	}
	
}

