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

package com.ibm.sample.daytrader.gateway.controller;

// DayTrader
import com.ibm.sample.daytrader.gateway.core.beans.MarketSummaryDataBean;
import com.ibm.sample.daytrader.gateway.core.beans.RunStatsDataBean;
import com.ibm.sample.daytrader.gateway.service.GatewayService;
import com.ibm.sample.daytrader.gateway.utils.Log;

// Java
import java.util.ArrayList;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.Map;

// Javax
import javax.ws.rs.BadRequestException;
import javax.ws.rs.ClientErrorException;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.NotAuthorizedException;

// Daytrader
import com.ibm.sample.daytrader.gateway.entities.AccountDataBean;
import com.ibm.sample.daytrader.gateway.entities.AccountProfileDataBean;
import com.ibm.sample.daytrader.gateway.entities.HoldingDataBean;
import com.ibm.sample.daytrader.gateway.entities.OrderDataBean;
import com.ibm.sample.daytrader.gateway.entities.QuoteDataBean;

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
 * @see https://{serviceHost}:{servicePort}/swagger-ui.html
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
public class GatewayController
{
	private static GatewayService gatewayService = new GatewayService();

	//
	// Account Related Endpoints
	//
	
	/**
	 * REST call to register an user provided in the request body.
	 * 
	 */
	@RequestMapping(value = "/accounts", method = RequestMethod.POST)
	public ResponseEntity<AccountDataBean> register (
			@RequestBody AccountDataBean accountData) 
	{
		Log.traceEnter("GatewayController.register()");
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
			// Register the user
			accountData = gatewayService.register(userID, password, fullname, address, email, creditCard, openBalance);
			Log.traceExit("GatewayController.register()");
			return new ResponseEntity<AccountDataBean>(accountData, getNoCacheHeaders(), HttpStatus.CREATED);
		}
     	catch(Throwable t)
     	{
     		Log.error("GatewayController.register()", t);
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
		Log.traceEnter("GatewayController.updateAccountProfile()");
		
		try
		{
			profileData = gatewayService.updateAccountProfile(profileData);
			if (profileData != null) 
			{
				Log.traceExit("GatewayController.updateAccountProfile()");
				return new ResponseEntity<AccountProfileDataBean>(profileData, getNoCacheHeaders(), HttpStatus.OK);
			}
			else
			{
				Log.traceExit("GatewayController.updateAccountProfile()");
				return new ResponseEntity<AccountProfileDataBean>(profileData, getNoCacheHeaders(), HttpStatus.NO_CONTENT);
			}
		}
     	catch(Throwable t)
     	{
     		Log.error("GatewayController.updateAccountProfile()", t);
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
		Log.traceEnter("GatewayController.getAccountProfileData()");
		AccountProfileDataBean profileData = null;
		try
		{
			profileData = gatewayService.getAccountProfileData(userId);
			if (profileData != null)
			{
				Log.traceExit("GatewayController.getAccountProfileData()");
				return new ResponseEntity<AccountProfileDataBean>(profileData, getNoCacheHeaders(), HttpStatus.OK);
			}
			else
			{
				Log.traceExit("GatewayController.getAccountProfileData()");
				return new ResponseEntity<AccountProfileDataBean>(profileData, getNoCacheHeaders(), HttpStatus.NO_CONTENT);
			}
		}
     	catch(Throwable t)
     	{
     		Log.error("GatewayController.getAccountProfileData()", t);
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
		Log.traceEnter("GatewayController.getAccountData()");
		AccountDataBean accountData = null;
		try
		{
			accountData = gatewayService.getAccountData(userId);
			if (accountData != null) 
			{
				Log.traceExit("GatewayController.getAccountData()");
				return new ResponseEntity<AccountDataBean>(accountData, getNoCacheHeaders(), HttpStatus.OK);
			}
			else
			{
				Log.traceExit("GatewayController.getAccountData()");
				return new ResponseEntity<AccountDataBean>(accountData, getNoCacheHeaders(), HttpStatus.NO_CONTENT);
			}
		}
     	catch(Throwable t)
     	{
     		Log.error("GatewayController.getAccountData()", t);
			return new ResponseEntity<AccountDataBean>(HttpStatus.INTERNAL_SERVER_ERROR);
		}

	}

	//
	// Authentication Related Endpoints
	//

	/**
	 * REST call to login the user from the authentication data passed in the body.
	 * 
	 */
	@RequestMapping(value = "/login/{userId}", method = RequestMethod.PATCH)
	public ResponseEntity<AccountDataBean> login (@PathVariable("userId") String userId, @RequestBody String password)
	{
		Log.traceEnter("GatewayController.login()");			
		AccountDataBean accountData = null;
		try
		{
			accountData = gatewayService.login(userId, password); 
			Log.traceExit("GatewayController.login()");
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
		Log.traceEnter("GatewayController.logout()");	
		try
		{
			gatewayService.logout(userId);
			Log.traceExit("GatewayController.logout()");
			return new ResponseEntity<Boolean>(true,HttpStatus.OK);
		}
     	catch(Throwable t)
     	{
     		Log.error("GatewayController.logout()", t);
			return new ResponseEntity<Boolean>(HttpStatus.INTERNAL_SERVER_ERROR);
		}	
	}
	
	//
	// Admin Related Endpoints
	//
	
	/**
	 * REST call to populate the database with the specified users and quotes
	 * 
	 */

	/**
	 * This is a dummy call to test if we can pick up a POST payload.
	 *
	 * @param valMap
	 * @return
	 */
	@RequestMapping(
			value = "/admin/test",
			method = RequestMethod.POST,
			consumes = "application/json"
	)
	public ResponseEntity<String> testAPIRequest(
			@RequestBody Map<String,String> valMap ) {
		return new ResponseEntity<String>(valMap.get("key"), HttpStatus.OK);
	}

	@RequestMapping(value = "/admin/tradeBuildDB", method = RequestMethod.POST)
	public ResponseEntity<Boolean> tradeBuildDB( 
			@RequestParam(value = "limit") int limit,
			@RequestParam(value= "offset") int offset )
	{
		Log.traceEnter("GatewayController.tradeBuildDB()");
			
		Boolean success = false;
	
		try
		{
			// Register the user
			success = gatewayService.tradeBuildDB(limit, offset);
			Log.traceExit("GatewayController.tradeBuildDB()");
			return new ResponseEntity<Boolean>(success, getNoCacheHeaders(), HttpStatus.CREATED);
		}
     	catch(Throwable t)
     	{
     		Log.error("GatewayController.tradeBuildDB()", t);
			return new ResponseEntity<Boolean>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
	
	/**
	 * REST call to populate the database with the specified users and quotes
	 * 
	 */
	@RequestMapping(value = "/admin/quotesBuildDB", method = RequestMethod.POST)
	public ResponseEntity<Boolean> quotesBuildDB( 
			@RequestParam(value = "limit") int limit,
			@RequestParam(value= "offset") int offset )
	{
		Log.traceEnter("GatewayController.quotesBuildDB()");
			
		Boolean success = false;
	
		try
		{
			// Create the quotes
			success = gatewayService.quotesBuildDB(limit, offset);
			Log.traceExit("GatewayController.quotesBuildDB()");
			return new ResponseEntity<Boolean>(success, getNoCacheHeaders(), HttpStatus.CREATED);
		}
     	catch(Throwable t)
     	{
     		Log.error("GatewayController.quotesBuildDB()", t);
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
		Log.traceEnter("GatewayController.recreateDBTables()");
		
		boolean result = false;
		
		try
		{
			result = gatewayService.recreateDBTables();
			Log.traceExit("GatewayController.recreateDBTables()");
			return new ResponseEntity<Boolean>(result, getNoCacheHeaders(), HttpStatus.CREATED);
		}
     	catch(Throwable t)
		{
     		Log.error("GatewayContoller.recreateDBTables()", t);
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
		Log.traceEnter("GatewayController.resetData()");			
		
		RunStatsDataBean runStatsData = null;
		
		try
		{
			runStatsData = gatewayService.resetTrade(deleteAll);
			if (runStatsData != null)
			{
				Log.traceExit("GatewayController.resetTrade()");
				return new ResponseEntity<RunStatsDataBean>(runStatsData, getNoCacheHeaders(), HttpStatus.OK);
			}
			else
			{
				Log.traceExit("GatewayController.resetTrade()");
				return new ResponseEntity<RunStatsDataBean>(runStatsData, getNoCacheHeaders(), HttpStatus.NO_CONTENT);				
			}
		}
     	catch(Throwable t)
     	{
     		Log.error("GatewayController.resetTrade()", t);
			return new ResponseEntity<RunStatsDataBean>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
	
	//
	// Portfolio related endpoints
	//

	/**
	 * REST call to get the portfolio's holdings.
	 * 
	 */
	@RequestMapping(value = "/portfolios/{userId}/holdings", method = RequestMethod.GET)
	public ResponseEntity<Collection<HoldingDataBean>> getHoldings(@PathVariable("userId") String userId)
	{
		Log.traceEnter("GatewayController.getHoldings()");
		
		Collection<HoldingDataBean> holdings = null;		
		
		try
		{
			holdings = gatewayService.getHoldings(userId);
			if ( holdings != null)
			{
				Log.traceExit("GatewayController.getHoldings()");
				return new ResponseEntity<Collection<HoldingDataBean>>(holdings, getNoCacheHeaders(), HttpStatus.OK);
			}
			else
			{
				Log.traceExit("GatewayController.getHoldings()");
				return new ResponseEntity<Collection<HoldingDataBean>>(holdings, getNoCacheHeaders(), HttpStatus.NO_CONTENT);
			}
		}
     	catch(Throwable t)
     	{
     		Log.error("GatewayController.getHoldings()", t);
			return new ResponseEntity<Collection<HoldingDataBean>>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
	
	/**
	 * REST call to get the portfolio's orders.
	 * 
	 */
	@RequestMapping(value = "/portfolios/{userId}/orders", method = RequestMethod.GET)
	public ResponseEntity<Collection<OrderDataBean>> getOrders(@PathVariable("userId") String userId)
	{
		Log.traceEnter("GatewayController.getOrders()");
		
		Collection<OrderDataBean> orders = null;		
		
		try
		{
			orders = gatewayService.getOrders(userId);
			if ( orders != null)
			{
				Log.traceExit("GatewayController.getOrders()");
				return new ResponseEntity<Collection<OrderDataBean>>(orders, getNoCacheHeaders(), HttpStatus.OK);
			}
			else
			{
				Log.traceExit("GatewayController.getOrders()");
				return new ResponseEntity<Collection<OrderDataBean>>(orders, getNoCacheHeaders(), HttpStatus.NO_CONTENT);
			}
		}
     	catch(Throwable t)
     	{
     		Log.error("GatewayController.getOrders()", t);
			return new ResponseEntity<Collection<OrderDataBean>>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}	
		
	/**
	 * REST call to get orders by their status. 
	 * 
	 * At present, the only status that is recognizes is closed. It transitions the
	 * closed orders to the completed state and returns them. If it doesn't receive 
	 * the closed status then it returns HttpStatus.BAD_REQUEST
	 * 
	 */
	@RequestMapping(value = "/portfolios/{userId}/orders", method = RequestMethod.PATCH)
	public ResponseEntity<Collection<OrderDataBean>>getOrdersByStatus(
			@PathVariable("userId") String userId, @RequestParam(value = "status") String status)
	{
		Log.traceEnter("GatewayController.getOrdersByStatus()");
		
		Collection<OrderDataBean> orders = new ArrayList<OrderDataBean>();

		try
		{
			if (status.equals("closed")) 
			{
				orders = gatewayService.getClosedOrders(userId);
				if (orders != null)
				{
					Log.traceExit("GatewayController.getOrdersByStatus()");
					return new ResponseEntity<Collection<OrderDataBean>>(orders, getNoCacheHeaders(), HttpStatus.OK);
				}
				else
				{			
					Log.traceExit("GatewayController.getOrdersByStatus()");
					return new ResponseEntity<Collection<OrderDataBean>>(orders, getNoCacheHeaders(), HttpStatus.NO_CONTENT);
				}
			} 
			else 
			{
				Throwable t = new Throwable("GatewayController.getOrdersByStatus: invalid status=" + status + " valid statuses are 'closed'");
				Log.error("GatewayController.getOrdersByStatus()", t);
				return new ResponseEntity<Collection<OrderDataBean>>(orders, getNoCacheHeaders(), HttpStatus.BAD_REQUEST);
			}
		}
     	catch(Throwable t)
     	{
     		Log.error("GatewayController.getOrdersByStatus()", t);
			return new ResponseEntity<Collection<OrderDataBean>>(orders, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}	
	
	/**
	 * REST call to buy/sell a stock and add it to the user's portfolio.
	 * 
	 * TODO: 
	 * - Improved choreography and error handling. The group of actions to purchase a stock
	 *   must be considered as a single entity even though it crosses multiple microservices.
	 */
	@RequestMapping(value = "/portfolios/{userId}/orders", method = RequestMethod.POST)
	public ResponseEntity<OrderDataBean> processOrder(
			@PathVariable("userId") String userId, 
			@RequestBody OrderDataBean orderData,
			@RequestParam(value= "mode") Integer mode)
	{
		Log.traceEnter("GatewayController.processOrder()");
		try //buy()
		{			
			if (orderData.getOrderType().equals("buy")) 
			{
				// Buy the specified quantity of stock and add a holding to the portfolio
				orderData = gatewayService.buy(userId,orderData.getSymbol(),orderData.getQuantity(),mode);
				Log.traceExit("GatewayController.processOrder()");
				return new ResponseEntity<OrderDataBean>(orderData, getNoCacheHeaders(), HttpStatus.CREATED);
			}
		}
		catch (NotFoundException nfe)
		{
     		Log.error("PortfoliosController.processOrder()", nfe);
			return new ResponseEntity<OrderDataBean>(HttpStatus.NOT_FOUND);			
		}
		catch (ClientErrorException cee)
		{
     		Log.error("PortfoliosController.processOrder()", cee);
			return new ResponseEntity<OrderDataBean>(HttpStatus.CONFLICT);			
		}
		catch (Throwable t)
     	{
     		Log.error("GatewayController.processOrder()", t);
			return new ResponseEntity<OrderDataBean>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
		
		try // sell()
		{	
			if (orderData.getOrderType().equals("sell"))
			{
				// Sell the specified holding and remove it from the portfolio
				orderData = gatewayService.sell(userId,orderData.getHoldingID(),mode);	
				Log.traceExit("GatewayController.processOrder()");
				return new ResponseEntity<OrderDataBean>(orderData, getNoCacheHeaders(), HttpStatus.CREATED);
			}
		}
		catch (NotFoundException nfe)
		{
     		Log.error("PortfoliosController.processOrder()", nfe);
			return new ResponseEntity<OrderDataBean>(HttpStatus.NOT_FOUND);			
		}
		catch (ClientErrorException cee)
		{
     		Log.error("PortfoliosController.processOrder()", cee);
			return new ResponseEntity<OrderDataBean>(HttpStatus.CONFLICT);			
		}
		catch (Throwable t)
     	{
     		Log.error("GatewayController.processOrder()", t);
			return new ResponseEntity<OrderDataBean>(HttpStatus.INTERNAL_SERVER_ERROR);
		}

		// other
		Throwable t = new BadRequestException("Invalid order type=" + orderData.getOrderType() + " valid types are 'buy' and 'sell'");
		Log.error("GatewayController.processOrder()", t);
	    return new ResponseEntity<OrderDataBean>(orderData, getNoCacheHeaders(), HttpStatus.BAD_REQUEST);
	}
			
	/**
	 * REST call to get the quote given the symbol.
	 * 
	 */
	@RequestMapping(value = "/quotes/{symbol}", method = RequestMethod.GET)
	public ResponseEntity<QuoteDataBean> getQuote(@PathVariable("symbol") String symbol)
	{
		Log.traceEnter("GatewayController.getQuote()");
				
		QuoteDataBean quoteData = null;
		
		try 
		{
			quoteData = gatewayService.getQuote(symbol);
			if (quoteData != null)
			{
				Log.traceExit("GatewayController.getQuote()");
				return new ResponseEntity<QuoteDataBean>(quoteData,getNoCacheHeaders(),HttpStatus.OK);
			}
			else
			{
				Log.traceExit("GatewayController.getQuote()");
				return new ResponseEntity<QuoteDataBean>(quoteData, getNoCacheHeaders(),HttpStatus.NO_CONTENT);
			}
		}
		catch (Throwable t)
     	{
     		Log.error("GatewayController.getQuote()", t);
			return new ResponseEntity<QuoteDataBean>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
	
	/**
	 * REST call to get all quotes.
	 * 
	 */
	@RequestMapping(value = "/quotes", method = RequestMethod.GET)
	public ResponseEntity<Collection<QuoteDataBean>> getAllQuotes(
			@RequestParam(value = "limit") Integer limit,
			@RequestParam(value = "offset") Integer offset) 
	{
		Log.traceEnter("GatewayController.getAllQuotes()");
			
		Collection<QuoteDataBean> quotes = null;
		
		try 
		{
			//
			// 	- TODO: Currently get all quotes returns all resources. It is never a good idea to
			//    return all resources. So, refactor this method to take in the parameters limit &
			//    offset change the SQL query to use those values, e.g.
			// 
			//    SELECT * FROM quoteejb 
			//    ORDER BY symbol
			//    LIMIT limit OFFSET offset;
			//
			//    Note this offset pagination mechanism uses well known keywords from SQL databases
			quotes = gatewayService.getAllQuotes(limit,offset);
			if (quotes != null)
			{
				Log.traceExit("GatewayController.getAllQuotes()");
				return new ResponseEntity<Collection<QuoteDataBean>>(quotes, getNoCacheHeaders(), HttpStatus.OK);
			}
			else
			{
				Log.traceExit("GatewayController.getAllQuotes()");
				return new ResponseEntity<Collection<QuoteDataBean>>(quotes, getNoCacheHeaders(), HttpStatus.NO_CONTENT);
			}
		}
		catch (Throwable t)
     	{
     		Log.error("GatewayController.getAllQuotes()", t);
			return new ResponseEntity<Collection<QuoteDataBean>>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
	
	/**
	 * REST call to create a quote provided in the request body.
	 * 
	 */
	@RequestMapping(value = "/quotes", method = RequestMethod.POST)
	public ResponseEntity<QuoteDataBean> createQuote (@RequestBody QuoteDataBean quoteData) 
	{
		Log.traceEnter("GatewayController.createQuote()");
		
		// Get the quote data
		String symbol = quoteData.getSymbol();
		String companyName = quoteData.getCompanyName();
		BigDecimal price = quoteData.getPrice();
					
		try
		{
			// Create the quote
			quoteData = gatewayService.createQuote(symbol,companyName,price);
			Log.traceExit("GatewayController.createQuote()");
			return new ResponseEntity<QuoteDataBean>(quoteData, getNoCacheHeaders(), HttpStatus.CREATED);
		}
		catch (Throwable t)
     	{
     		Log.error("GatewayController.createQuote()", t);
			return new ResponseEntity<QuoteDataBean>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
	
	/**
	 * REST call to update the quote price and volume using data provided in the request body.
	 * 
	 */
	@RequestMapping(value = "/quotes/{symbol}", method = RequestMethod.PATCH)
	public ResponseEntity<QuoteDataBean> updateQuotePriceVolume(
			@PathVariable("symbol") String symbol, 
			@RequestParam(value = "price") BigDecimal price, 
			@RequestParam(value = "volume") double volume) 
	{
		Log.trace("GatewayController.updateQuotePriceVolume()");
			
		QuoteDataBean quoteData = null;
		
		try 
		{
			// Update the quote price volume
			quoteData = gatewayService.updateQuotePriceVolume(symbol, price, volume);
			if (quoteData != null)
			{
				Log.traceExit("GatewayController.updateQuotePriceVolume()");
				return new ResponseEntity<QuoteDataBean>(quoteData, getNoCacheHeaders(), HttpStatus.OK);
			}
			else
			{
				Log.traceExit("GatewayController.updateQuotePriceVolume()");
				return new ResponseEntity<QuoteDataBean>(quoteData, getNoCacheHeaders(), HttpStatus.NO_CONTENT);
			}
		}
		catch (Throwable t)
     	{
     		Log.error("GatewayController.updateQuotePriceVolume()", t);
			return new ResponseEntity<QuoteDataBean>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
		
	//
	// Markets Related Endpoints
	//

	@RequestMapping(value = "/markets/{exchange}", method = RequestMethod.GET)
	public ResponseEntity<MarketSummaryDataBean> getMarketSummary(@PathVariable("exchange") String exchange)
	{	
		Log.traceEnter("GatewayController.getMarketSummary()");
		
		MarketSummaryDataBean marketSummary = new MarketSummaryDataBean();
		
		try
		{
			marketSummary = gatewayService.getMarketSummary();
			Log.traceExit("GatewayController.getMarketSummary()");
			return new ResponseEntity<MarketSummaryDataBean>(marketSummary, getNoCacheHeaders(), HttpStatus.OK);
		}
		catch (NotFoundException nfe)
		{
     		Log.error("QuotesController.getMarketSummary()", nfe);
			return new ResponseEntity<MarketSummaryDataBean>(HttpStatus.NOT_FOUND);			
		}
		catch (Throwable t)
     	{
     		Log.error("GatewayController.getMarketSummary()", t);
			return new ResponseEntity<MarketSummaryDataBean>(HttpStatus.INTERNAL_SERVER_ERROR);
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

