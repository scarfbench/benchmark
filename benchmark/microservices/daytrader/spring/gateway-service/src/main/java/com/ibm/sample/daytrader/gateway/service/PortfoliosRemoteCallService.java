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

// Jackson
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ibm.sample.daytrader.gateway.core.beans.RunStatsDataBean;
import com.ibm.sample.daytrader.gateway.utils.Log;

// Java Runtime
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;

//Daytrader

//Spring
import com.ibm.sample.daytrader.gateway.entities.HoldingDataBean;
import com.ibm.sample.daytrader.gateway.entities.OrderDataBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
* The remote call service to the accounts microservice.
* 
* @author
*
*/

@Service
public class PortfoliosRemoteCallService extends BaseRemoteCallService
{
	protected static ObjectMapper mapper = null;
	
	static 
	{
		mapper = new ObjectMapper(); // create once, reuse
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false); // ignore properties that are not declared
	}

//
//  - Naming convention based service discovery 
	private static String portfoliosServiceRoute;
	@Value("${daytrader.services.portfolios}")
	public void setPortfoliosServiceRoute(String route) { portfoliosServiceRoute = route;}

	
	   /**
		*
		* @see PortfoliosServices#tradeBuildDB(int,int)
		*
		*/
		public boolean tradeBuildDB(int limit, int offset) throws Exception 
		{ 
	    	String url = portfoliosServiceRoute + "/admin/tradeBuildDB?limit="+limit+ "&offset=" + offset;
			Log.debug("PortfoliosRemoteCallService.tradeBuildDB() - " + url);
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
	    	String url = portfoliosServiceRoute + "/admin/resetTrade?deleteAll=" + deleteAll;
			Log.debug("PortfoliosRemoteCallService.resetTrade() - " + url);
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
	    	String url = portfoliosServiceRoute + "/admin/recreateDBTables";
			Log.debug("PortfoliosRemoteCallService.recreateDBTables() - " + url);
	    	String responseEntity = invokeEndpoint(url, "POST", "");
	    	Boolean success = mapper.readValue(responseEntity,Boolean.class);
	    	return success;
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
    	String url = portfoliosServiceRoute + "/portfolios/" + userID + "/orders?mode=" + orderProcessingMode;
		Log.debug("PortfoliosRemoteCallService.buy() - " + url);
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
    	String url = portfoliosServiceRoute + "/portfolios/" + userID + "/orders?mode=" + orderProcessingMode;
		Log.debug("PortfoliosRemoteCallService.sell() - " + url);
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
    	String url = portfoliosServiceRoute + "/portfolios/" + userID + "/orders";
		Log.debug("PortfoliosRemoteCallService.getOrders() - " + url);
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
	 * is defined as an Http PATCH method; instead of an Http GET
	 *  
	 * 	- Endpoint:  PATCH /portfolios/{userId}/orders?status
	 */
	public Collection<OrderDataBean> getClosedOrders(String userID) throws Exception
    {
		// REST call transitions closed orders to completed state and returns them.
    	String url = portfoliosServiceRoute + "/portfolios/" + userID + "/orders?status=closed";
		Log.debug("PortfoliosRemoteCallService.getClosedOrders() - " + url);
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
    	String url = portfoliosServiceRoute + "/portfolios/" + userID + "/holdings";
		Log.debug("PortfoliosRemoteCallService.getHoldings() - " + url);
	   	String responseString = invokeEndpoint(url, "GET", null); // Entity must be null for http method GET.
	   	Collection<HoldingDataBean> holdings = mapper.readValue(responseString,new TypeReference<ArrayList<HoldingDataBean>>(){ });
		return holdings;
    }
	
}