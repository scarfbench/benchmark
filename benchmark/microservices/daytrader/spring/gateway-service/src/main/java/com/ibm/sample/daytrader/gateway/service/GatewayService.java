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

import java.io.PrintWriter;
import java.math.BigDecimal;
import java.util.Collection;

import com.ibm.sample.daytrader.gateway.core.beans.MarketSummaryDataBean;
import com.ibm.sample.daytrader.gateway.core.beans.RunStatsDataBean;
import com.ibm.sample.daytrader.gateway.entities.*;
import com.ibm.sample.daytrader.gateway.entities.*;
import com.ibm.sample.daytrader.gateway.utils.Log;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * The remote call service to the trades services.
 * 
 * @author
 *
 */

@Service
public class GatewayService {
	
	// TODO: Inject these services to switch between local and remote call implementations

	private static AccountsRemoteCallService accountsService = new AccountsRemoteCallService();
	private static PortfoliosRemoteCallService portfoliosService = new PortfoliosRemoteCallService();
	private static QuotesRemoteCallService quotesService = new QuotesRemoteCallService();
	
    /**
     * @see TradeBuildDB#TradeBuildDB(PrintWriter, String)
     */	
	public boolean tradeBuildDB(int limit, int offset) throws Exception
    {
		if (accountsService.tradeBuildDB(limit, offset)) 
		{
			return portfoliosService.tradeBuildDB(limit, offset);
		}
		return false;
    }   

	   /**
     * @see TradeBuildDB#TradeBuildDB(PrintWriter, String)
     */	
	public boolean quotesBuildDB(int limit, int offset) throws Exception
    {		
	    return quotesService.tradeBuildDB(limit, offset);
    }   
	
    /**
     * @see TradeServices#resetTrade(boolean)
     */  
    public RunStatsDataBean resetTrade(boolean deleteAll) throws Exception
    {
		// Note elected to put the orchestration across these microservices in the gateway
		// instead of picking one of the microservices to do so. If we were to pick one of
		// the microservicecs it would be Accounts simply because Accounts is dependent on
		// (calls) Portfolios and Portfolios is dependent on (calls) Quotes
		//
		// Note that the only orchestration logic is for the admin services which create
    	// the database tables, generate sample data in the databases, and retrieves run
    	// stats from the databases. Although they are unusual services, they are formal 
    	// business operations in the trade application so we mapped them into the micro
    	// services architecture
	
		// Ask the microservices to reset their trades and return their usage data
		RunStatsDataBean quoteStatsData = quotesService.resetTrade(deleteAll);
		RunStatsDataBean portfolioStatsData = portfoliosService.resetTrade(deleteAll); 
		RunStatsDataBean accountStatsData = accountsService.resetTrade(deleteAll);

		// Aggregate the results form the microservices
		RunStatsDataBean runStatsData = accountStatsData;
		runStatsData.setTradeStockCount(quoteStatsData.getTradeStockCount()); 	        
		runStatsData.setHoldingCount(portfolioStatsData.getHoldingCount());
		runStatsData.setOrderCount(portfolioStatsData.getOrderCount());
		runStatsData.setBuyOrderCount(portfolioStatsData.getBuyOrderCount());
		runStatsData.setSellOrderCount(portfolioStatsData.getSellOrderCount());
		runStatsData.setCancelledOrderCount(portfolioStatsData.getCancelledOrderCount());
		runStatsData.setOpenOrderCount(portfolioStatsData.getOpenOrderCount());
		runStatsData.setDeletedOrderCount(portfolioStatsData.getDeletedOrderCount()); 
		
		return runStatsData;
    }
	
    /**
     * @see TradeDBServices#recreateDBTables(Object[],PrintWriter)
     */	
	public boolean recreateDBTables() throws Exception
    {
		// create the trade db
		if (accountsService.recreateDBTables()) {
			Log.trace("Succeeded in re-creating Accounts databases");
		} else {
			Log.error("Failed to re-create Accounts databases");
			return false;
		}
		if (portfoliosService.recreateDBTables()) {
			Log.trace("Succeeded in re-creating Portfolios databases");
		} else {
			Log.error("Failed to re-create Portfolios databases");
			return false;
		}
		if (quotesService.recreateDBTables()) {
			Log.trace("Succeeded in re-creating Quotes databases");
		} else {
			Log.error("Failed to re-create Quotes databases");
			return false;
		}
		return true;
    }   
	
    /**
     * @see TradeServices#buy(String,String,double,int)
     */	
	public OrderDataBean buy(String userID, String symbol, double quantity, int orderProcessingMode) throws Exception
    {
		return portfoliosService.buy(userID, symbol, quantity, orderProcessingMode);
    }

    /**
     * @see TradeServices#sell(String,Integer,int)
     */
	public OrderDataBean sell(String userID, Integer holdingID, int orderProcessingMode) throws Exception
	{
		return portfoliosService.sell(userID, holdingID, orderProcessingMode);
    }
    
    /**
     * @see TradeServices#getMarketSummary()
     */
	public MarketSummaryDataBean getMarketSummary() throws Exception {
        return quotesService.getMarketSummary();
    }
    
    /**
     * @see TradeServices#getOrders(Integer)
     */
	public Collection<OrderDataBean> getOrders(String userID) throws Exception
	{
    	return portfoliosService.getOrders(userID);
    }

    /**
     * @see TradeServices#getClosedOrders(String)
     */
	public Collection<OrderDataBean> getClosedOrders(String userID) throws Exception {
    	return portfoliosService.getClosedOrders(userID);
    }

    /**
     * @see TradeServices#createQuote(String, String, BigDecimal)
     */
    public QuoteDataBean createQuote(String symbol, String companyName, BigDecimal price) throws Exception {
    	return quotesService.createQuote(symbol, companyName, price);
    }

    /**
     * @see TradeServices#getQuote(String)
     */
    public QuoteDataBean getQuote(String symbol) throws Exception {
    	return quotesService.getQuote(symbol);
    }

    /**
     * @see TradeServices#getAllQuotes(String)
     */
	public Collection<QuoteDataBean> getAllQuotes(int limit, int offset) throws Exception {
    	return quotesService.getAllQuotes(limit, offset);
    }

    /**
     * @see TradeServices#getHoldings(String)
     */
	public Collection<HoldingDataBean> getHoldings(String userID) throws Exception
	{
    	return portfoliosService.getHoldings(userID);
    }

    /**
     * @see TradeServices#getAccountData(String)
     */
	public AccountDataBean getAccountData(String userID) throws Exception {
    	return accountsService.getAccountData(userID);
    }

    /**
     * @see TradeServices#getProfileData(String)
     */
	public AccountProfileDataBean getAccountProfileData(String userID) throws Exception {
    	return accountsService.getAccountProfileData(userID);
    }

    /**
     * @see TradeServices#updateAccountProfileData(AccountProfileDataBean)
     */
	public AccountProfileDataBean updateAccountProfile(AccountProfileDataBean profileData) throws Exception {
    		return accountsService.updateAccountProfile(profileData);
    }

    /**
     * @see TradeServices#updateQuotePriceVolume(String,BigDecimal,double)
     */
	public QuoteDataBean updateQuotePriceVolume(String symbol, BigDecimal changeFactor, double sharesTraded) throws Exception {
        return quotesService.updateQuotePriceVolume(symbol, changeFactor, sharesTraded);
    }

    /**
     * @see TradeServices#login(String, String)
     */
    public AccountDataBean login(String userID, String password) throws Exception
    {    
        return accountsService.login(userID, password);       
    }

    /**
     * @see TradeServices#logout(String)
     */
	public void logout(String userID) throws Exception 
	{   
        accountsService.logout(userID);	
    }

    /**
     * @see TradeServices#register(String, String, String, String, String, String, BigDecimal, boolean)
     */
    public AccountDataBean register(String userID, String password, String fullname, String address, String email, 
    		String creditCard, BigDecimal openBalance) throws Exception 
	{
        return accountsService.register(userID, password, fullname, address, email, creditCard, openBalance);
    }
	
    /**
     * @see TradeServices#updateQuotePriceVolume(String,BigDecimal,double,boolean)
     */
    public QuoteDataBean updateQuotePriceVolumeInt(String symbol, BigDecimal changeFactor, double sharesTraded,
        boolean publishQuotePriceChange) throws Exception
    {
            return quotesService.updateQuotePriceVolumeInt(symbol, changeFactor, sharesTraded, publishQuotePriceChange);
    }

}

