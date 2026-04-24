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

package com.ibm.sample.daytrader.core.direct;

import com.ibm.sample.daytrader.core.api.TradeDBServices;
import com.ibm.sample.daytrader.core.api.TradeServices;
import com.ibm.sample.daytrader.core.beans.MarketSummaryDataBean;
import com.ibm.sample.daytrader.core.beans.RunStatsDataBean;
import com.ibm.sample.daytrader.entities.*;
import com.ibm.sample.daytrader.utils.Log;
import com.ibm.sample.daytrader.utils.TradeConfig;
import com.ibm.sample.daytrader.web.service.GatewayRemoteCallService;

import java.io.PrintWriter;
import java.math.BigDecimal;
import java.rmi.RemoteException;
import java.util.Collection;

import com.ibm.sample.daytrader.entities.*;

/**
 * The TradeJDBCDirect class is a session facade that simplifies the interaction between
 * web tier and the business tier. For instance, a single business operation may involve
 * multiple business objects. The session facade interacts with the business objects and 
 * data access objects to implement the business operations. These business operations 
 * include login, logout, buy, sell, getQuote, etc. 
 */

public class TradeJDBCDirect implements TradeServices, TradeDBServices
{

	private static GatewayRemoteCallService tradesGateway = new GatewayRemoteCallService();
	
	/**
     * Zero arg constructor for TradeJDBCDirect
     */
    public TradeJDBCDirect() 
	{

    }
    
    private static boolean initialized = false;

    public static synchronized void init() 
    {
        if (!initialized) 
        {
        	if (Log.doTrace())
        		Log.trace("TradeJDBCDirect:init -- *** initializing");
        
        	TradeConfig.setPublishQuotePriceChange(false);

        	if (Log.doTrace())
        		Log.trace("TradeJDBCDirect:init -- +++ initialized");

        	initialized = true;
        }
    }

    public static void destroy() 
    {
    	if (initialized)
    	{
    		if (Log.doTrace()) 
    			Log.trace("TradesJDBCDirect:destroy -- *** destroying");
        
    		if (Log.doTrace())
    			Log.trace("TradeJDBCDirect:destroy -- +++ destroyed");
        
    		initialized = false;
    	}
    }
    	
    // added this method so microservices can create sample data
    // See TradeBuildDB#TradeBuildDB(PrintWriter, String)
	public boolean tradeBuildDB(int maxUsers, int maxQuotes) throws Exception
	{
    	return tradesGateway.tradeBuildDB(maxUsers, maxQuotes);
	}
	
    // added this method so microservices can create sample data
    // See TradeBuildDB#TradeBuildDB(PrintWriter, String)
	public boolean quotesBuildDB(int limit, int offset) throws Exception
	{
    	return tradesGateway.quotesBuildDB(limit, offset);
	}
    
	@Override
	public RunStatsDataBean resetTrade(boolean deleteAll) throws Exception, RemoteException {
    	return tradesGateway.resetTrade(deleteAll);
	}
	
	// the following methods were defined in the interface but were not used by the application; 
	// instead of changing the interface this class; which would cause a lot of compilation errors, this
	// class implements them by simply throws throwing an unsupported operation exception. There is no
	// need to redirect these calls to the gateway.
	
	@Override
	public String checkDBProductName() throws Exception {
        throw new UnsupportedOperationException("TradesJDBCDirect: checkDBProductName not supported");
	}

	@Override
	public boolean recreateDBTables(Object[] sqlBuffer, PrintWriter out) throws Exception {
		// can't pass print write across address spaces so no need to include it
    	return tradesGateway.recreateDBTables();
	}

	@Override
	public void queueOrder(Integer orderID, boolean twoPhase) throws Exception, RemoteException 
	{
        throw new UnsupportedOperationException("TradesJDBCDirect: queueOrder not supported");
	}

	@Override
	public OrderDataBean completeOrder(Integer orderID, boolean twoPhase) throws Exception, RemoteException
	{
        throw new UnsupportedOperationException("TradesJDBCDirect: complete not supported");
	}

	@Override
	public void cancelOrder(Integer orderID, boolean twoPhase) throws Exception, RemoteException 
	{
        throw new UnsupportedOperationException("TradesJDBCDirect: cancelOrder not supported");
	}

	@Override
	public void orderCompleted(String userID, Integer orderID) throws Exception, RemoteException 
	{
        throw new UnsupportedOperationException("TradesJDBCDirect: orderCompleted not supported");
	}
	
	

	@Override
	public OrderDataBean buy(String userID, String symbol, double quantity, int orderProcessingMode) throws Exception, RemoteException 
	{
    	return tradesGateway.buy(userID, symbol, quantity, orderProcessingMode);
    }

	@Override
	public OrderDataBean sell(String userID, Integer holdingID, int orderProcessingMode) throws Exception, RemoteException 
	{
    	return tradesGateway.sell(userID, holdingID, orderProcessingMode);
    }
    
    /**
     * @see TradeServices#getMarketSummary()
     */
    public MarketSummaryDataBean getMarketSummary() throws Exception
    {
        return tradesGateway.getMarketSummary();
    }
    
    /**
     * @see TradeServices#getOrders(String)
     */
	@Override
	public Collection getOrders(String userID) throws Exception, RemoteException 
    {
    	return tradesGateway.getOrders(userID);
    }

    /**
     * @see TradeServices#getClosedOrders(String)
     */
	@Override
	public Collection getClosedOrders(String userID) throws Exception, RemoteException
    {
    	return tradesGateway.getClosedOrders(userID);
    }

    /**
     * @see TradeServices#createQuote(String, String, BigDecimal)
     */
    public QuoteDataBean createQuote(String symbol, String companyName, BigDecimal price) throws Exception
    {
    	return tradesGateway.createQuote(symbol, companyName, price);
    }

    /**
     * @see TradeServices#getQuote(String)
     */

    public QuoteDataBean getQuote(String symbol) throws Exception 
    {
    	return tradesGateway.getQuote(symbol);
    }

    /**
     * @see TradeServices#getAllQuotes(String)
     */
    public Collection getAllQuotes() throws Exception 
    {
    	return tradesGateway.getAllQuotes();
    }

    /**
     * @see TradeServices#getHoldings(String)
     */
	@Override
	public Collection getHoldings(String userID) throws Exception, RemoteException 
	{
    	return tradesGateway.getHoldings(userID);
    }
    
	
	public HoldingDataBean getHolding(Integer holdingID) throws Exception
	{
        throw new UnsupportedOperationException("TradesJDBCDirect: getHolding not supported");
	}

    /**
     * @see TradeServices#getAccountData(String)
     */
    public AccountDataBean getAccountData(String userID) throws Exception
    {
    	return tradesGateway.getAccountData(userID);
    }

    /**
     * @see TradeServices#getProfileData(String)
     */
	@Override
    public AccountProfileDataBean getAccountProfileData(String userID) throws Exception
    {
    	return tradesGateway.getAccountProfileData(userID);
    }
    
    /**
     * @see TradeServices#updateProfile(AccountProfileDataBean)
     */
	@Override
	public AccountProfileDataBean updateAccountProfile(AccountProfileDataBean profileData) throws Exception, RemoteException 
    {
   		return tradesGateway.updateAccountProfile(profileData);
    }

    /**
     * Update a quote's price and volume
     * 
     * @param symbol
     *            The PK of the quote
     * @param changeFactor
     *            the percent to change the old price by (between 50% and 150%)
     * @param sharedTraded
     *            the ammount to add to the current volume
     */
	@Override
    public QuoteDataBean updateQuotePriceVolume(String symbol, BigDecimal changeFactor, double sharesTraded) 
    		throws Exception 
    {
        return tradesGateway.updateQuotePriceVolume(symbol, changeFactor, sharesTraded);
    }
	
 	// Public helper functions; over and above those defined in the subertypes
 	
    /**
     * Update a quote's price and volume
     * 
     * @param symbol
     *            The PK of the quote
     * @param changeFactor
     *            the percent to change the old price by (between 50% and 150%)
     * @param sharedTraded
     *            the ammount to add to the current volume
     * @param publishQuotePriceChange
     *            used by the PingJDBCWrite Primitive to ensure no JMS is used, should be true for all normal calls to
     *            this API
     */
    public QuoteDataBean updateQuotePriceVolumeInt(String symbol, BigDecimal changeFactor, double sharesTraded,
        boolean publishQuotePriceChange) throws Exception
    {
            return tradesGateway.updateQuotePriceVolumeInt(symbol, changeFactor, sharesTraded, publishQuotePriceChange);
    }

    /**
     * @see TradeServices#login(String, String)
     */

    public AccountDataBean login(String userID, String password) throws Exception 
    {    
    	return tradesGateway.login(userID, password);
    }

    /**
     * @see TradeServices#logout(String)
     */
    public void logout(String userID) throws Exception 
    {
        tradesGateway.logout(userID);	
    }

    /**
     * @see TradeServices#register(String, String, String, String, String, String, BigDecimal, boolean)
     */
    public AccountDataBean register(String userID, String password, String fullname, String address, String email, 
    		String creditCard, BigDecimal openBalance) throws Exception 
    {
    	return tradesGateway.register(userID, password, fullname, address, email, creditCard, openBalance);
    }

}

