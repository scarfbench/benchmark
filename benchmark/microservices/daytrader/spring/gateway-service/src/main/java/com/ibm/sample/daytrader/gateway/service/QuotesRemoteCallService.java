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

package com.ibm.sample.daytrader.gateway.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
//Jackson
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ibm.sample.daytrader.gateway.core.beans.MarketSummaryDataBean;
import com.ibm.sample.daytrader.gateway.core.beans.RunStatsDataBean;
import com.ibm.sample.daytrader.gateway.utils.Log;

import java.math.BigDecimal;
import java.util.Collection;

// Daytrader

// Spring
import com.ibm.sample.daytrader.gateway.entities.QuoteDataBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * The remote call service to the accounts microservice.
 * 
 * @author
 *
 */

@Service
public class QuotesRemoteCallService extends BaseRemoteCallService
{
	protected static ObjectMapper mapper = null;
	
	static 
	{
		mapper = new ObjectMapper(); // create once, reuse
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false); // ignore properties that are not declared
	}

	// Service discovery from settings in application.properties file
	private static String quotesServiceRoute;
	@Value("${daytrader.services.quotes}")
	public void setQuotesServiceRoute(String route) {quotesServiceRoute = route;}


	/**
	*
	* @see QuotesServices#tradeBuildDB(int,int)
	*
	*/
	public boolean tradeBuildDB(int limit, int offset) throws Exception
	{
		String url = quotesServiceRoute + "/admin/tradeBuildDB?limit="+limit+ "&offset=" + offset;
		Log.debug("QuotesRemoteCallService.quotesBuildDB() - " + url);
		String responseEntity = invokeEndpoint(url, "POST", "");
		Boolean success = mapper.readValue(responseEntity,Boolean.class);
		return success;
	}

   /**
	*
	* @see TradeServices#resetTrade(boolean)
	*
	*/
	public RunStatsDataBean resetTrade(boolean deleteAll) throws Exception
	{
		String url = quotesServiceRoute + "/admin/resetTrade?deleteAll=" + deleteAll;
		Log.debug("QuotesRemoteCallService.resetTrade() - " + url);
		String responseEntity = invokeEndpoint(url, "GET", null); // Entity must be null for http method GET.
		RunStatsDataBean runStatsData = mapper.readValue(responseEntity,RunStatsDataBean.class);
		return runStatsData;
	}

   /**
	*
	* @see TradeDBServices#recreateDBTables(Object[],PrintWriter)
	*
	*/
	public boolean recreateDBTables() throws Exception
	{
		String url = quotesServiceRoute + "/admin/recreateDBTables";
		Log.debug("QuotesRemoteCallService.recreateDBTables() - " + url);
		String responseEntity = invokeEndpoint(url, "POST", "");
		Boolean success = mapper.readValue(responseEntity,Boolean.class);
		return success;
	}
    
	/**
	 * 
	 * @see TradeServices#getMarketSummary()
	 *
	 */
    public MarketSummaryDataBean getMarketSummary() throws Exception
    {
    	// Added exchange on 7-10-2018 (03); it is ignored for now.
    	// Later you can select the exchange, e.g. NYSE, NASDAQ, AMEX to get
    	// the top gainers and losers in that exchange
    	String exchange = "TSIA"; /* Trade Stock Index Average */
    	String url = quotesServiceRoute + "/markets/" + exchange;   
		Log.debug("QuotesRemoteCallService.getMarketSummary() - " + url);
	   	String responseString = invokeEndpoint(url, "GET", null); // Entity must be null for http method GET.
        MarketSummaryDataBean marketSummaryData = mapper.readValue(responseString,MarketSummaryDataBean.class);
        return marketSummaryData;
    }
    
	/**
	 * 
  	 * @see TradeServices#createQuote(String, String, BigDecimal)
	 *
	 */
    public QuoteDataBean createQuote(String symbol, String companyName, BigDecimal price) throws Exception
    {
    	String url = quotesServiceRoute + "/quotes";
		Log.debug("QuotesRemoteCallService.createQuote() - " + url);
		
    	// Consruct the quote data from the given params
    	QuoteDataBean quoteData = new QuoteDataBean();
		quoteData.setSymbol(symbol);
		quoteData.setCompanyName(companyName);
		quoteData.setPrice(price);
	
    	String quoteDataInString = mapper.writeValueAsString(quoteData);
    	String responseEntity = invokeEndpoint(url, "POST", quoteDataInString);
    	quoteData = mapper.readValue(responseEntity,QuoteDataBean.class);
    	return quoteData;
    }

	/**
	 * 
	 * @see TradeServices#getQuote(String)
	 *
	 */
    public QuoteDataBean getQuote(String symbol) throws Exception 
    {
    	String url = quotesServiceRoute + "/quotes/" + symbol;  
		Log.debug("QuotesRemoteCallService.getQuote() - " + url);
	   	String responseString = invokeEndpoint(url, "GET", null); // Entity must be null for http method GET.
        QuoteDataBean quoteData = mapper.readValue(responseString,QuoteDataBean.class);
        return quoteData;
    }

	/**
	 * 
	 * @see TradeServices#getAllQuotes()
	 *
	 */
    public Collection<QuoteDataBean> getAllQuotes(int limit, int offset) throws Exception 
    {
    	//
    	// 	- TODO: Its never a good idea to return all resources. We arbitrarily limited this call
    	//    to return the first 100. The web application should be modified so use the pagination
    	//    capabilities (limit and offset parameters) of this URI
    	String url = quotesServiceRoute + "/quotes?limit=" + limit + "&offset=" + offset;  
		Log.debug("QuotesRemoteCallService.getAllQuotes() - " + url);
	   	String responseString = invokeEndpoint(url, "GET", null); // Entity must be null for http method GET.
        Collection<QuoteDataBean> quoteCollection = mapper.readValue(responseString,new TypeReference<Collection<QuoteDataBean>>(){ });
        return quoteCollection;
    }

	/**
	 * 
	 * @see TradeServices#updateQuotePriceVolume(String,BigDecimal,double)
	 *
	 */
    public QuoteDataBean updateQuotePriceVolume(String symbol, BigDecimal price, double volume) throws Exception 
    {
    	String url = quotesServiceRoute + "/quotes/" + symbol + "?price=" + price + "&volume=" + volume;
		Log.debug("QuotesRemoteCallService.updateQuotePriceVolume() - " + url);
    	String responseEntity = invokeEndpoint(url, "PATCH", "");
    	QuoteDataBean quoteData = mapper.readValue(responseEntity,QuoteDataBean.class);
    	return quoteData;
    }

	/**
	 * 
	 * @see TradeServices#updateQuotePriceVolumeInt(String,BigDecimal,double,boolean)
	 *
	 */	
    public QuoteDataBean updateQuotePriceVolumeInt(
    		String symbol, BigDecimal changeFactor, double sharesTraded, boolean publishQuotePriceChange) throws Exception
    {
    	// The parameter publishQuotePriceChange isn't required; it was always false
        return updateQuotePriceVolume(symbol, changeFactor, sharesTraded);
    }
        
}