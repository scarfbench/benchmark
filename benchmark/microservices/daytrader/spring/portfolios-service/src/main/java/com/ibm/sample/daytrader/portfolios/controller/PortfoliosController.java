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

package com.ibm.sample.daytrader.portfolios.controller;

// DayTrader
import com.ibm.sample.daytrader.portfolios.service.PortfoliosService;
import com.ibm.sample.daytrader.portfolios.utils.Log;

// Java
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

import javax.ws.rs.BadRequestException;
import javax.ws.rs.ClientErrorException;
import javax.ws.rs.NotFoundException;

// Daytrader
import com.ibm.sample.daytrader.portfolios.direct.beans.RunStatsDataBean;
import com.ibm.sample.daytrader.portfolios.entities.AccountDataBean;
import com.ibm.sample.daytrader.portfolios.entities.HoldingDataBean;
import com.ibm.sample.daytrader.portfolios.entities.OrderDataBean;

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
 * @see https://{portfoliosServiceHost}:{portfoliosServicePort}/swagger-ui.html
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
 * - PUT is used for updates (full)
 * 
 * - PATCH is used for updates (partial)
 * 
 * TODO:
 * 1.	Access Control
 *		The controller provides a centralized location for access control. Currently,
 *		the application does not check to see is a user is logged in before invoking
 *		a method. So access control checks should be added in Stage 04: Microservices   
 */

@RestController
public class PortfoliosController 
{
	private static PortfoliosService portfoliosService = new PortfoliosService();

	@RequestMapping(
			value = "/portfolios/test",
			method = RequestMethod.POST,
			consumes = "application/json"
	)
	public ResponseEntity<String> testAPIRequest(
			@RequestBody Map<String,String> valMap ) {
		return new ResponseEntity<String>(valMap.get("key"), HttpStatus.OK);
	}
	/**
	 * REST call to create the user's portfolio.
	 * 
	 */
	@RequestMapping(value = "/portfolios", method = RequestMethod.POST)
	public ResponseEntity<AccountDataBean> register (
			@RequestBody AccountDataBean accountData) 
	{
		Log.traceEnter("PortfolioController.register()");		
		
		try
		{
			// Create the user's portfolio
			accountData = portfoliosService.register(accountData);
			Log.traceExit("PortfoliosController.register()");
			return new ResponseEntity<AccountDataBean>(accountData, getNoCacheHeaders(), HttpStatus.CREATED);
		}
		catch (Throwable t)
     	{
     		Log.error("PortfoliosController.register()", t);
			return new ResponseEntity<AccountDataBean>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
	
	/**
	 * REST call to get the user's portfolio.
	 * 
	 */
	@RequestMapping(value = "/portfolios/{userId}", method = RequestMethod.GET)
	public ResponseEntity<AccountDataBean> getAccountData (@PathVariable("userId") String userId) 
	{
		Log.traceEnter("PortfolioController.getAccountData()");		
		
		AccountDataBean accountData = null;
	
		try
		{
			// Get the account
			accountData = portfoliosService.getAccountData(userId);
			if (accountData != null)
			{
				Log.traceExit("PortfoliosController.getAccountData()");
				return new ResponseEntity<AccountDataBean>(accountData, getNoCacheHeaders(), HttpStatus.OK);
			}
			else
			{
				Log.traceExit("PortfoliosController.getAccountData()");
				return new ResponseEntity<AccountDataBean>(accountData, getNoCacheHeaders(), HttpStatus.NO_CONTENT);
			}
		}
		catch (Throwable t)
     	{
     		Log.error("PortfoliosController.getAccountData()", t);
			return new ResponseEntity<AccountDataBean>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
		
	/**
	 * REST call to get the portfolio's holdings.
	 * 
	 */
	@RequestMapping(value = "/portfolios/{userId}/holdings", method = RequestMethod.GET)
	public ResponseEntity<Collection<HoldingDataBean>> getHoldings(@PathVariable("userId") String userId)
	{
		Log.traceEnter("PortfoliosController.getHoldings()");
		
		Collection<HoldingDataBean> holdings = null;		
		
		try
		{
			holdings = portfoliosService.getHoldings(userId);
			if ( holdings != null)
			{
				Log.traceExit("PortfoliosController.getHoldings()");
				return new ResponseEntity<Collection<HoldingDataBean>>(holdings, getNoCacheHeaders(), HttpStatus.OK);
			}
			else
			{
				Log.traceExit("PortfoliosController.getHoldings()");
				return new ResponseEntity<Collection<HoldingDataBean>>(holdings, getNoCacheHeaders(), HttpStatus.NO_CONTENT);
			}
		}
		catch (Throwable t)
     	{
     		Log.error("PortfoliosController.getHoldings()", t);
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
		Log.traceEnter("PortfoliosController.getOrders()");
		
		Collection<OrderDataBean> orders = null;		
		
		try
		{
			orders = portfoliosService.getOrders(userId);
			if ( orders != null)
			{
				Log.traceExit("PortfoliosController.getOrders()");
				return new ResponseEntity<Collection<OrderDataBean>>(orders, getNoCacheHeaders(), HttpStatus.OK);
			}
			else
			{
				Log.traceExit("PortfoliosController.getOrders()");
				return new ResponseEntity<Collection<OrderDataBean>>(orders, getNoCacheHeaders(), HttpStatus.NO_CONTENT);
			}
		}
		catch (Throwable t)
     	{
     		Log.error("PortfoliosController.getOrders()", t);
			return new ResponseEntity<Collection<OrderDataBean>>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}	
	
	/**
	 * REST call to get orders by their status. 
	 * 
	 * At present, the only status that is recognizes is closed. It transitions the
	 * closed orders to the completed state and returns them. If it doesn't receive 
	 * the closed status then it returns HttpStatus.NO_CONTENT
	 * 
	 */
	@RequestMapping(value = "/portfolios/{userId}/orders", method = RequestMethod.PATCH)
	public ResponseEntity<Collection<OrderDataBean>>getOrdersByStatus(
			@PathVariable("userId") String userId, @RequestParam(value = "status") String status)
	{
		Log.traceEnter("PortfoliosController.getOrdersByStatus()");
		
		Collection<OrderDataBean> orders = new ArrayList<OrderDataBean>();

		try
		{
			if (status.equals("closed")) 
			{
				orders = portfoliosService.getClosedOrders(userId);
				if (orders != null)
				{
					Log.traceExit("PortfoliosController.getOrdersByStatus()");
					return new ResponseEntity<Collection<OrderDataBean>>(orders, getNoCacheHeaders(), HttpStatus.OK);
				}
				else
				{
					Log.traceExit("PortfoliosController.getOrdersByStatus()");
					return new ResponseEntity<Collection<OrderDataBean>>(orders, getNoCacheHeaders(), HttpStatus.NO_CONTENT);
				}
			} 
			else 
			{
				Log.error("PortfoliosController.getOrdersByStatus: invalid status=" + status + " valid statuses are 'closed'");
				return new ResponseEntity<Collection<OrderDataBean>>(orders, getNoCacheHeaders(), HttpStatus.BAD_REQUEST);
			}
		}
		catch (Throwable t)
     	{
     		Log.error("PortfoliosController.getOrdersByStatus()", t);
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
		Log.traceEnter("PortfoliosController.processOrder()");

		try // buy
		{			
			if (orderData.getOrderType().equals("buy")) 
			{
				// Buy the specified quantity of stock and add a holding to the portfolio
				orderData = portfoliosService.buy(userId,orderData.getSymbol(),orderData.getQuantity(),mode);
				Log.traceExit("PortfoliosController.processOrder()");
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
     		Log.error("PortfoliosController.processOrder()", t);
			return new ResponseEntity<OrderDataBean>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
		
		try // sell
		{
			if (orderData.getOrderType().equals("sell"))
			{
				// Sell the specified holding and remove it from the portfolio
				orderData = portfoliosService.sell(userId,orderData.getHoldingID(),mode);	
				Log.traceExit("PortfoliosController.processOrder()");
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
     		Log.error("PortfoliosController.processOrder()", t);
			return new ResponseEntity<OrderDataBean>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
		
		// other
		Throwable t = new BadRequestException("Invalid order type=" + orderData.getOrderType() + " valid types are 'buy' and 'sell'");
		Log.error("PortfoliosController.processOrder()", t);
	    return new ResponseEntity<OrderDataBean>(HttpStatus.BAD_REQUEST);
	}
			
	//
	// Admin Related Endpoints
	//
	
	/**
	 * REST call to register the specified number of users in the query param.
	 *        
	 */
	@RequestMapping(value = "/admin/tradeBuildDB", method = RequestMethod.POST)
	public ResponseEntity<Boolean> tradeBuildDB( 
			@RequestParam(value= "limit") int limit,
			@RequestParam(value= "offset") int offset )
	{
		Log.traceEnter("PortfolioController.tradeBuildDB()");
			
		Boolean result = false;
	
		try
		{
			// Register max users
			result = portfoliosService.tradeBuildDB(limit, offset);
			Log.traceExit("PortfoliosController.tradeBuildDB()");
			return new ResponseEntity<Boolean>(result, getNoCacheHeaders(), HttpStatus.CREATED);
		}
     	catch(Throwable t)
     	{
     		Log.error("PortfoliosController.tradeBuildDB()", t);
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
		Log.traceEnter("PortfoliosController.recreateDBTables()");
		
		Boolean result = false;
		
		try
		{
			result = portfoliosService.recreateDBTables();
			Log.traceExit("PortfoliosController.recreateDBTables()");
			return new ResponseEntity<Boolean>(result, getNoCacheHeaders(), HttpStatus.CREATED);
		}
		catch (Throwable t)
     	{
     		Log.error("PortfoliosController.recreateDBTables()", t);
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
		Log.traceEnter("PortfoliosController.resetTrade()");			
		
		RunStatsDataBean runStatsData = null;
		
		try
		{
			runStatsData = portfoliosService.resetTrade(deleteAll);
			if (runStatsData != null)
			{			
				Log.traceExit("PortfoliosController.resetTrade()");
				return new ResponseEntity<RunStatsDataBean>(runStatsData, getNoCacheHeaders(), HttpStatus.OK);
			}
			else
			{
				Log.traceExit("PortfoliosController.resetTrade()");
				return new ResponseEntity<RunStatsDataBean>(runStatsData, getNoCacheHeaders(), HttpStatus.NO_CONTENT);		
			}
		}
		catch (Throwable t)
     	{
     		Log.error("PortfoliosController.resetTrade()", t);
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