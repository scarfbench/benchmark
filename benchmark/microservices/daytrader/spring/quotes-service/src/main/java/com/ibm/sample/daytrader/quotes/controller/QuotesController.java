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

package com.ibm.sample.daytrader.quotes.controller;

// DayTrader
import com.ibm.sample.daytrader.quotes.service.QuotesService;
import com.ibm.sample.daytrader.quotes.utils.Log;

// Java
import java.math.BigDecimal;
import java.util.Collection;
import java.util.Map;

import javax.ws.rs.NotFoundException;

// Daytrader
import com.ibm.sample.daytrader.quotes.core.beans.MarketSummaryDataBean;
import com.ibm.sample.daytrader.quotes.core.beans.RunStatsDataBean;
import com.ibm.sample.daytrader.quotes.entities.QuoteDataBean;

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
 * @see https://{quotesServiceHost}:{quotesServicePort}/swagger-ui.html
 * 
 * HTTP Methods. There is no official and enforced standard for designing HTTP & RESTful APIs.
 * There are many ways for designing them. These notes cover the guidelines used for designing
 * the aforementioned HTTP & RESTful API.
 * 
 * - GET is used for reading objects and for partial updates.
 * - POST is used for creating objects or for operations that change the server side state. In the
 *   case where an object is created, the created object is returned; instead of it'd URI. This is 
 *   in keeping with the existing services. A better practice is to return the URI to the created
 *   object, but we elected to keep the REST APIs consistent with the existing services. New APIs 
 *   that return the URI can be added during Stage 04: Microservices if required. 
 * - PUT is used for updates (full and partial)
 * 
 * TODO:  
 * 1.	Access Control
 *		The controller provides a centralized location for access control. Currently,
 *		the application does not check to see is a user is logged in before invoking
 *		a method. So access control checks should be added in Stage 04: Microservices 
 */
@RestController
public class QuotesController
{
	private static QuotesService quotesService = new QuotesService();

	//
	// Quotes Related Endpoints
	//

	@RequestMapping(
			value = "/quotes/test",
			method = RequestMethod.POST,
			consumes = "application/json"
	)
	public ResponseEntity<String> testAPIRequest(
			@RequestBody Map<String,String> valMap ) {
		return new ResponseEntity<String>(valMap.get("key"), HttpStatus.OK);
	}

	/**
	 * REST call to get the quote given the symbol.
	 * 
	 */
	@RequestMapping(value = "/quotes/{symbol}", method = RequestMethod.GET)
	public ResponseEntity<QuoteDataBean> getQuote(@PathVariable("symbol") String symbol) 
	{
		Log.traceEnter("QuotesController.getQuote()");
				
		QuoteDataBean quoteData = null;
		
		try 
		{
			quoteData = quotesService.getQuote(symbol);
			if (quoteData != null)
			{
				Log.traceExit("QuotesController.getQuote()");
				return new ResponseEntity<QuoteDataBean>(quoteData,getNoCacheHeaders(),HttpStatus.OK);
			}
			else
			{
				Log.traceExit("QuotesController.getQuote()");
				return new ResponseEntity<QuoteDataBean>(quoteData, getNoCacheHeaders(),HttpStatus.NO_CONTENT);
			}
		}
		catch (Throwable t)
     	{
     		Log.error("QuotesController.getQuote()", t);
			return new ResponseEntity<QuoteDataBean>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
	
	/**
	 * REST call to get all quotes.
	 * 
	 * Notes: added params attribute to request mapping to resolve ambigous mapping
	 * 
	 */
	@RequestMapping(value = "/quotes", method = RequestMethod.GET)
	public ResponseEntity<Collection<QuoteDataBean>> getAllQuotes(
			@RequestParam(value = "limit") Integer limit,
			@RequestParam(value = "offset", required = false) Integer offset) 
	{
		Log.traceEnter("QuotesController.getAllQuotes()");
			
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
			quotes = quotesService.getAllQuotes();
			if (quotes != null)
			{
				Log.traceExit("QuotesController.getAllQuotes()");
				return new ResponseEntity<Collection<QuoteDataBean>>(quotes, getNoCacheHeaders(), HttpStatus.OK);
			}
			else
			{
				Log.traceExit("QuotesController.getAllQuotes()");
				return new ResponseEntity<Collection<QuoteDataBean>>(quotes, getNoCacheHeaders(), HttpStatus.NO_CONTENT);
			}
		}
		catch (Throwable t)
     	{
			Log.error("QuotesController.getAllQuotes()", t);
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
		Log.traceEnter("QuotesController.createQuote()");
		
		// Get the quote data
		String symbol = quoteData.getSymbol();
		String companyName = quoteData.getCompanyName();
		BigDecimal price = quoteData.getPrice();
				
		try
		{
			// Create the quote
			quoteData = quotesService.createQuote(symbol,companyName,price);
			Log.traceExit("QuotesController.createQuote()");
			return new ResponseEntity<QuoteDataBean>(quoteData, getNoCacheHeaders(), HttpStatus.CREATED);
		}
		catch (Throwable t)
     	{
     		Log.error("QuotesController.createQuote()", t);
			return new ResponseEntity<QuoteDataBean>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
	
	/**
	 * REST call to update the quote price volume using data provided in the request body.
	 * 
	 */
	@RequestMapping(value = "/quotes/{symbol}", method = RequestMethod.PATCH)
	public ResponseEntity<QuoteDataBean> updateQuotePriceVolume(
			@PathVariable("symbol") String symbol, 
			@RequestParam(value = "price") BigDecimal price, 
			@RequestParam(value = "volume") double volume) 
	{
		Log.traceEnter("QuotesController.updateQuotePriceVolume()");
			
		QuoteDataBean quoteData = null;
		
		try 
		{
			// Update the quote price volume
			quoteData = quotesService.updateQuotePriceVolumeInt(symbol, price, volume);
			if (quoteData != null)
			{
				Log.traceExit("QuotesController.updateQuotePriceVolume()");
				return new ResponseEntity<QuoteDataBean>(quoteData, getNoCacheHeaders(), HttpStatus.OK);
			}
			else
			{
				Log.traceExit("QuotesController.updateQuotePriceVolume()");
				return new ResponseEntity<QuoteDataBean>(quoteData, getNoCacheHeaders(), HttpStatus.NO_CONTENT);
			}
		}
		catch (Throwable t)
     	{
     		Log.error("QuotesController.updateQuotePriceVolume()", t);
			return new ResponseEntity<QuoteDataBean>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
		
	//
	// Markets Related Endpoints
	//

	@RequestMapping(value = "/markets/{exchange}", method = RequestMethod.GET)
	public ResponseEntity<MarketSummaryDataBean> getMarketSummary(@PathVariable("exchange") String exchange) 
	{	
		Log.traceEnter("QuotesController.getMarketSummary()");
		
		MarketSummaryDataBean marketSummary = new MarketSummaryDataBean();
		
		try
		{
			marketSummary = quotesService.getMarketSummary(exchange);
			Log.traceExit("QuotesController.getMarketSummary()");
			return new ResponseEntity<MarketSummaryDataBean>(marketSummary, getNoCacheHeaders(), HttpStatus.OK);
		}
		catch (NotFoundException nfe)
		{
     		Log.error("QuotesController.getMarketSummary()", nfe);
			return new ResponseEntity<MarketSummaryDataBean>(HttpStatus.NOT_FOUND);			
		}
		catch (Throwable t)
     	{
     		Log.error("QuotesController.getMarketSummary()", t);
			return new ResponseEntity<MarketSummaryDataBean>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
	
	//
	// Admin Related Endpoints
	//
	
	/**
	 * REST call to create the specified number of quotes
	 * 
	 */
	@RequestMapping(value = "/admin/tradeBuildDB", method = RequestMethod.POST)
	public ResponseEntity<Boolean> tradeBuildDB( 
			@RequestParam(value = "limit") Integer limit,
			@RequestParam(value = "offset") Integer offset) 
	{
		Log.traceEnter("QuotesController.tradeBuildDB()");
			
		Boolean success = false;
	
		try
		{
			// Create the sample quotes
			success = quotesService.tradeBuildDB(limit,offset);
			Log.traceExit("QuotesController.tradeBuildDB()");
			return new ResponseEntity<Boolean>(success, getNoCacheHeaders(), HttpStatus.CREATED);
		}
     	catch(Throwable t)
     	{
     		Log.error("QuotesController.tradeBuildDB()", t);
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
		Log.traceEnter("QuotesController.recreateDBTables()");
		
		Boolean result = false;
		
		try
		{
			result = quotesService.recreateDBTables();
			Log.traceExit("QuotesController.recreateDBTables()");
			return new ResponseEntity<Boolean>(result, getNoCacheHeaders(), HttpStatus.CREATED);
		}
		catch (Throwable t)
     	{
     		Log.error("QuotesController.recreateDBTables()", t);
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
		Log.traceEnter("QuotesController.resetTrade()");			
		
		RunStatsDataBean runStatsData = new RunStatsDataBean();
		try
		{
			runStatsData = quotesService.resetTrade(deleteAll);
			if (runStatsData != null)
			{
				Log.traceExit("QuotesController.resetTrade()");
				return new ResponseEntity<RunStatsDataBean>(runStatsData, getNoCacheHeaders(), HttpStatus.OK);
			}
			else
			{
				Log.traceExit("QuotesController.resetTrade()");
				return new ResponseEntity<RunStatsDataBean>(runStatsData, getNoCacheHeaders(), HttpStatus.NO_CONTENT);
			}
		}
		catch (Throwable t)
     	{
     		Log.error("QuotesController.resetTrade()", t);
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
