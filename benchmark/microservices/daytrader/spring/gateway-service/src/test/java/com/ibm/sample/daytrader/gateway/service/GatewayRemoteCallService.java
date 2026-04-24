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

// java
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;

// daytrader
import com.ibm.sample.daytrader.gateway.entities.*;

// spring
import com.ibm.sample.daytrader.gateway.core.beans.MarketSummaryDataBean;
import com.ibm.sample.daytrader.gateway.core.beans.RunStatsDataBean;
import com.ibm.sample.daytrader.gateway.entities.*;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * The remote call service to the trader gateway service.
 * 
 * @author
 * 
 * Implement a remote call service if you want to host the gateway in its own microservice.
 * For now, the gateway is architected to be pulled out into its own microservice, but it
 * did not seem to warrant its own separate process. For that reason, the web tier calls a
 * local gateway. It can easily be hosted in its own microservice later if warranted.
 *
 */

//
//Don't do any logging from the integration tests unless you send it to its own logger;
//otherwise the test log messages will be written to the server log and the server log
//messages will not written. For debugging, you will need the server log messages.

@Service
public class GatewayRemoteCallService extends BaseRemoteCallService
{
    
	protected static ObjectMapper mapper = null;
	
	static 
	{
		mapper = new ObjectMapper(); // create once, reuse
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false); // ignore properties that are not declared
	}

	//  - Naming convention based service discovery
//	private static String gatewayServiceRoute = System.getenv("DAYTRADER_GATEWAY_SERVICE");
	//  - Hardcoded service discovery
	private static String gatewayServiceRoute = "http://localhost:2443";

    
   /**
	*
	* @see AccountsService#tradeBuildDB(int,int)
	*
	*/
	public boolean tradeBuildDB(int limit, int offset) throws Exception 
	{ 
    	String url = gatewayServiceRoute + "/admin/tradeBuildDB?limit=" + limit + "&offset=" + offset;
    	String responseEntity = invokeEndpoint(url, "POST", "");
    	Boolean success = mapper.readValue(responseEntity,Boolean.class);
    	return success;
	}
	   
	   /**
		*
		* @see AccountsService#quotesBuildDB(int,int)
		*
		*/
		public boolean quotesBuildDB(int limit, int offset) throws Exception 
		{ 
	    	String url = gatewayServiceRoute + "/admin/quotesBuildDB?limit=" + limit + "&offset=" + offset;
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
	    	String url = gatewayServiceRoute + "/admin/resetTrade?deleteAll=" + deleteAll;
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
	    	String url = gatewayServiceRoute + "/admin/recreateDBTables";
	    	String responseEntity = invokeEndpoint(url, "POST", "");
	    	Boolean success = mapper.readValue(responseEntity,Boolean.class);
	    	return success;
		}

		   /**
		*
		* @see TradeServices#getAccountData(String)
		*
		*/
	    public AccountDataBean getAccountData(String userID) throws Exception
	    {
	    	String url = gatewayServiceRoute + "/accounts/" + userID;
	    	String responseEntity = invokeEndpoint(url, "GET", null); // Entity must be null for http method GET.
	    	AccountDataBean accountData = mapper.readValue(responseEntity,AccountDataBean.class);
	    	return accountData;
	    }

	   /**
		*
		* @see TradeServices#getAccountProfileData(String)
		*
		*/
		public AccountProfileDataBean getAccountProfileData(String userID) throws Exception
		{
	    	String url = gatewayServiceRoute + "/accounts/" + userID + "/profiles";
	    	String responseEntity = invokeEndpoint(url, "GET", null); // Entity must be null for http method GET.
	    	AccountProfileDataBean profileData = mapper.readValue(responseEntity,AccountProfileDataBean.class);
	    	return profileData;     	
		}

	   /**
		*
		* @see TradeServices#updateAccountProfile(AccountProfileDataBean)
		*
		*/
		public AccountProfileDataBean updateAccountProfile(AccountProfileDataBean profileData) throws Exception
	    {
			String url = gatewayServiceRoute + "/accounts/" + profileData.getUserID() + "/profiles";
			String profileDataInString = mapper.writeValueAsString(profileData);
		   	String responseEntity = invokeEndpoint(url, "PUT", profileDataInString);
	        profileData = mapper.readValue(responseEntity, AccountProfileDataBean.class);
	        return profileData;
	    }

		   /**
		*
		* @see TradeServices#login(String,String)	
		*
		*/
	    public AccountDataBean login(String userID, String password) throws Exception 
	    {    
	    	String url = gatewayServiceRoute + "/login/" + userID;
	    	String responseEntity = invokeEndpoint(url, "PATCH", password);
	    	AccountDataBean accountData = mapper.readValue(responseEntity,AccountDataBean.class);
	    	return accountData;
	    }

	   /**
		*
		* @see TradeServices#logout(String)
		*
		*/
	    public void logout(String userID) throws Exception 
	    {
	    	String url = gatewayServiceRoute + "/logout/" + userID;
	    	String responseEntity = invokeEndpoint(url, "PATCH", "");
	    	mapper.readValue(responseEntity,Boolean.class);
	    }
		
	   /**
		*
		* @see TradeServices#register(String,String,String,String,String,String,BigDecimal)
		*
		*/
	    public AccountDataBean register(String userID, String password, String fullname, 
	    		String address, String email, String creditCard, BigDecimal openBalance) throws Exception 
	    {
	    	String url = gatewayServiceRoute + "/accounts";

	    	// Consruct the account data from that given params
	    	AccountDataBean accountData = new AccountDataBean();
			accountData.setProfileID(userID);
			accountData.setOpenBalance(openBalance);
			accountData.setProfile(new AccountProfileDataBean());
			accountData.getProfile().setUserID(userID);
			accountData.getProfile().setPassword(password);
			accountData.getProfile().setFullName(fullname);
			accountData.getProfile().setAddress(address);
			accountData.getProfile().setEmail(email);
			accountData.getProfile().setCreditCard(creditCard);
			
	    	String accountDataInString = mapper.writeValueAsString(accountData);
	    	String responseEntity = invokeEndpoint(url, "POST", accountDataInString);
	    	accountData = mapper.readValue(responseEntity,AccountDataBean.class);
	    	return accountData;
	    }
	
		/**
		 * 
		 * @see TradeServices#buy(String, String, double, int)
		 *
		 * 	- Endpoint: POST /portfolios/{userId}/orders?mode
		 */
		public OrderDataBean buy(String userID, String symbol, double quantity, int orderProcessingMode) throws Exception
		{
	    	// Construct the order data from the given params
			OrderDataBean orderData = new OrderDataBean();
			orderData.setSymbol(symbol);
			orderData.setQuantity(quantity);
			orderData.setOrderType("buy");
			orderData.setOrderStatus("open");
	    	String orderDataInString = mapper.writeValueAsString(orderData);
	    	String url = gatewayServiceRoute + "/portfolios/" + userID + "/orders?mode=" + orderProcessingMode;
	    	String responseEntity = invokeEndpoint(url, "POST", orderDataInString);
	    	orderData = mapper.readValue(responseEntity,OrderDataBean.class);
	    	return orderData;
	    }

		/**
		 * 
		 * @see TradeServices#sell(String, Integer)
		 *
		 * 	- Endpoint: POST /portfolios/{userId}/orders?mode
		 */
		public OrderDataBean sell(String userID, Integer holdingID, int orderProcessingMode) throws Exception 
		{
	    	// Construct the order data from the given params
			OrderDataBean orderData = new OrderDataBean();
			orderData.setHoldingID(holdingID);
			orderData.setOrderType("sell");
			orderData.setOrderStatus("open");
	    	String orderDataInString = mapper.writeValueAsString(orderData);
	    	String url = gatewayServiceRoute + "/portfolios/" + userID + "/orders?mode=" + orderProcessingMode;
	    	String responseEntity = invokeEndpoint(url, "POST", orderDataInString);
	    	orderData = mapper.readValue(responseEntity,OrderDataBean.class);
	    	return orderData;
	    }
	      
		/**
		 * 
		 * @see TradeServices#getOrders(String)
		 *
		 */
		public Collection<OrderDataBean> getOrders(String userID) throws Exception 
	    {
			// Returns the orders for the given user
	    	String url = gatewayServiceRoute + "/portfolios/" + userID + "/orders";
		   	String responseString = invokeEndpoint(url, "GET", null); // Entity must be null for http method GET.
		   	Collection<OrderDataBean> orderCollection = mapper.readValue(responseString,new TypeReference<ArrayList<OrderDataBean>>(){ });
	        return orderCollection;
	    }

		/**
		 * 
		 * @see TradeServices#getClosedOrders(String)
		 *
		 * REST call to change resources to the given state and return those resources.
		 * 
		 * Note: the original name getClosedOrders() is a bit misleading, it actually changes 
		 * the user's closed orders to complete and then returns them to the caller. Thus, it 
		 * is defined as an Http PATCH method; not an Http GET
		 *  
		 * 	- Endpoint:  PATCH /portfolios/{userId}/orders?status
		 */
		public Collection<OrderDataBean> getClosedOrders(String userID) throws Exception
	    {
			// REST call transitions closed orders to completed state and returns them.
	    	String url = gatewayServiceRoute + "/portfolios/" + userID + "/orders?status=closed";
		   	String responseString = invokeEndpoint(url, "PATCH", ""); // must pass a request body to the patch method 
		   	Collection<OrderDataBean> orderCollection = mapper.readValue(responseString,new TypeReference<ArrayList<OrderDataBean>>(){ });
	        return orderCollection;
	    }

		/**
		 * 
		 * @see TradeServices#getHoldings(String)
		 *
		 */
		public Collection<HoldingDataBean> getHoldings(String userID) throws Exception
		{		
			// Returns the holdings for the give user
	    	String url = gatewayServiceRoute + "/portfolios/" + userID + "/holdings";
		   	String responseString = invokeEndpoint(url, "GET", null); // Entity must be null for http method GET.
		   	Collection<HoldingDataBean> holdings = mapper.readValue(responseString,new TypeReference<ArrayList<HoldingDataBean>>(){ });
			return holdings;
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
	    	String url = gatewayServiceRoute + "/markets/" + exchange;   
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
	    	String url = gatewayServiceRoute + "/quotes";

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
	    	String url = gatewayServiceRoute + "/quotes/" + symbol;  
		   	String responseString = invokeEndpoint(url, "GET", null); // Entity must be null for http method GET.
	        QuoteDataBean quoteData = mapper.readValue(responseString,QuoteDataBean.class);
	        return quoteData;
	    }

		/**
		 * 
		 * @see TradeServices#getAllQuotes(String)
		 *
		 */
	    public Collection<QuoteDataBean> getAllQuotes(int limit, int offset) throws Exception 
	    {
	    	//
	    	// 	- TODO: Its never a good idea to return all resources. We arbitrarily limited this call
	    	//    to return the first 100. The web application should be modified so use the pagination
	    	//    capabilities (limit and offset parameters) of this URI
	    	String url = gatewayServiceRoute + "/quotes?limit=" + limit + "&offset=" + offset;  
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
	    	String url = gatewayServiceRoute + "/quotes/" + symbol + "?price=" + price + "&volume=" + volume;
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

