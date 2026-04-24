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

package com.ibm.sample.daytrader.gateway;

import com.ibm.sample.daytrader.gateway.core.beans.MarketSummaryDataBean;
import com.ibm.sample.daytrader.gateway.service.GatewayRemoteCallService;
import com.ibm.sample.daytrader.gateway.utils.TradeConfig;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.ibm.sample.daytrader.gateway.entities.AccountDataBean;
import com.ibm.sample.daytrader.gateway.entities.AccountProfileDataBean;
import com.ibm.sample.daytrader.gateway.entities.HoldingDataBean;
import com.ibm.sample.daytrader.gateway.entities.OrderDataBean;
import com.ibm.sample.daytrader.gateway.entities.QuoteDataBean;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.Iterator;

//
//Don't do any logging from the integration tests unless you send it to its own logger;
//otherwise the test log messages will be written to the server log and the server log
//messages will not written. For debugging, you will need the server log messages.

@SpringBootTest(classes = GatewayApplication.class)
public class GatewayIntegrationTests {
	
	// The remote call services for testing this microservice
	private static GatewayRemoteCallService gatewayService = new GatewayRemoteCallService();
	
	// Random data for testing this microservice
    private static AccountDataBean accountData = AccountDataBean.getRandomInstance();
    private static AccountProfileDataBean profileData = AccountProfileDataBean.getRandomInstance();
    private static QuoteDataBean quoteData = QuoteDataBean.getRandomInstance();
    private static HoldingDataBean holdingData = HoldingDataBean.getRandomInstance();
    private static OrderDataBean orderData =  OrderDataBean.getRandomInstance();
    
    private static int limitUsers = TradeConfig.getMAX_USERS(); // Trade users (uid:0, uid:1, ..) for the test
    private static int limitQuotes = TradeConfig.getMAX_QUOTES(); // Trade quotes (s:0, s:1, ..) for the test
    private static int offset = 0;   
    
    // Create/populate database before test
    @Before
    public void runBeforeTestMethod() 
    {
    	// Set primary keys for comparison of these expected results with the actual results
        accountData.setProfileID("uid:" + (limitUsers-1)); 
        accountData.setAccountID(limitUsers-1);
        profileData.setUserID("uid:" + (limitUsers-1));
        profileData.setPassword("xxx"); // all sample profiles have the same password (xxx) in the DB
    	quoteData.setSymbol("s:" + (limitQuotes-1));
    	
        try // create the database
     	{
        	gatewayService.recreateDBTables();
     	}
     	catch(Throwable t)
     	{
     		StringWriter sw = new StringWriter();
     		t.printStackTrace(new PrintWriter(sw));
     		String exceptionAsString = sw.toString();
     		fail("recreateDBTables() threw exception " + exceptionAsString);
     		
     	}

        try // populate the trade users
     	{
        	gatewayService.tradeBuildDB(limitUsers,offset);
     	}
     	catch(Throwable t)
     	{
     		StringWriter sw = new StringWriter();
     		t.printStackTrace(new PrintWriter(sw));
     		String exceptionAsString = sw.toString();
     		fail("tradeBuildDB() threw exception " + exceptionAsString);
     	}
        
        try // populate the trade quotes
     	{
        	gatewayService.quotesBuildDB(limitQuotes,offset);
     	}
     	catch(Throwable t)
     	{
     		StringWriter sw = new StringWriter();
     		t.printStackTrace(new PrintWriter(sw));
     		String exceptionAsString = sw.toString();
     		fail("quotesBuildDB() threw exception " + exceptionAsString);
     	}
    }

    // Reset database after test method
    @After
    public void runAfterTestMethod() {
        try 
        {
        	gatewayService.resetTrade(true);
        }
        catch(Throwable t)
        {
     		StringWriter sw = new StringWriter();
     		t.printStackTrace(new PrintWriter(sw));
     		String exceptionAsString = sw.toString();
     		fail("recreateDBTables() threw exception " + exceptionAsString);
        }
   }

    //
    // accounts related tests
    //
   @Test
   public void testGetAccountProfileData() throws Exception 
   {
	   try 
	   { 
		   AccountProfileDataBean actualValue = null;
		   for (int i = 0; i < limitUsers; i++)
		   {
			   profileData.setUserID("uid:" + i);
			   actualValue= gatewayService.getAccountProfileData(profileData.getUserID());
			   assertTrue(actualValue.equals( profileData ));
		   }
	   }
	   catch(Throwable t)
	   {
		   StringWriter sw = new StringWriter();
		   t.printStackTrace(new PrintWriter(sw));
		   String exceptionAsString = sw.toString();
		   fail("getAccountProfileData( " + profileData.getUserID() + " ) threw exception " + exceptionAsString);
	   }
   }
   
   @Test
   public void testUpdateAccountProfile() throws Exception 
   {
	   try
	   {  
		   profileData.setAddress( TradeConfig.rndAddress() );
		   AccountProfileDataBean actualProfile = gatewayService.updateAccountProfile( profileData );
		   assertTrue( profileData.getAddress().equals( actualProfile.getAddress() ));
	   }
 	   catch(Throwable t)
 	   {
 		   StringWriter sw = new StringWriter();
 		   t.printStackTrace(new PrintWriter(sw));
 		   String exceptionAsString = sw.toString();
		   fail("updateAccountProfile( " + profileData.getUserID() + " ) threw exception " + exceptionAsString);
	   }
   }

   @Test
   public void testLogin()
   {
	   try
	   {		   
		   AccountDataBean actualResult = gatewayService.login( accountData.getProfileID(), profileData.getPassword() );
		   assertTrue( accountData.equals( actualResult ));
	   }
 	   catch(Throwable t)
 	   {
 		   StringWriter sw = new StringWriter();
 		   t.printStackTrace(new PrintWriter(sw));
 		   String exceptionAsString = sw.toString();
		   fail("GatewayIntegrationTestslogin( " + profileData.getUserID() + " ) threw exception " + exceptionAsString);
	   }
   }
   
   @Test
   public void testLogout()
   {
	   try 
	   {
		   gatewayService.logout( profileData.getUserID() );
	   }
 	   catch(Throwable t)
 	   {
 		   StringWriter sw = new StringWriter();
 		   t.printStackTrace(new PrintWriter(sw));
 		   String exceptionAsString = sw.toString();
 		   fail("logout( " + profileData.getUserID() + " ) threw exception " + exceptionAsString);
	   }
    }
   
 
  // portfolios related tests
    
  @Test
  public void testGetOrders() throws Exception 
  {
	   try 
	   { 
		   Collection<OrderDataBean> orders = null;
		   for (int i = 0; i < limitUsers; i++)
		   {
			   accountData.setProfileID("uid:" + i);
			   orders = gatewayService.getOrders(accountData.getProfileID());
			   if (orders.size() > 0) break;
		   }
		   assertTrue(orders.size()>0);
	   }
 	   catch(Throwable t)
 	   {
 		   StringWriter sw = new StringWriter();
 		   t.printStackTrace(new PrintWriter(sw));
 		   String exceptionAsString = sw.toString();
		   fail("getOrders() threw exception " + exceptionAsString);
	   }
  }
  
  @Test
  public void testGetHoldings() throws Exception 
  {
	   try 
	   { 
		   Collection<HoldingDataBean> holdings = null;
		   for (int i = 0; i < limitUsers; i++)
		   {
			   accountData.setProfileID("uid:" + i);
			   holdings = gatewayService.getHoldings(accountData.getProfileID());
			   if (holdings.size() > 0) break;
		   }
		   assertTrue(holdings.size()>0);
	   }
 	   catch(Throwable t)
 	   {
 		   StringWriter sw = new StringWriter();
 		   t.printStackTrace(new PrintWriter(sw));
 		   String exceptionAsString = sw.toString();
		   fail("getHoldings() threw exception " + exceptionAsString);
	   }
  }
  
  @Test
  public void testGetClosedOrders() throws Exception 
  {
	   try 
	   { 
		   Collection<OrderDataBean> orders = null;
		   for (int i = 0; i < limitUsers; i++)
		   {
			   accountData.setProfileID("uid:" + i);
			   orders = gatewayService.getClosedOrders(accountData.getProfileID());
			   if (orders.size() > 0) break;
		   }
		   assertTrue(orders.size()>0);
	   }
 	   catch(Throwable t)
 	   {
 		   StringWriter sw = new StringWriter();
 		   t.printStackTrace(new PrintWriter(sw));
 		   String exceptionAsString = sw.toString();
		   fail("getClosedOrders() threw exception " + exceptionAsString);
	   }
  }

   
@Test
public void testSell() throws Exception 
{ 
   // Sell all the holdings for a given account
   try 
   { 
	   Collection<HoldingDataBean> holdings = null;
	   for (int i = 0; i < limitUsers; i++)
	   {
		   accountData.setAccountID(i);
		   accountData.setProfileID("uid:" + i);
		   holdings = gatewayService.getHoldings(accountData.getProfileID());
		   if (holdings.size() > 0) break;
	   }
	   assertTrue(holdings.size()>0);

       Iterator<HoldingDataBean> it = holdings.iterator();
       while ( it.hasNext() )
       {
           holdingData = it.next();
    	   orderData = gatewayService.sell(accountData.getProfileID(), holdingData.getHoldingID(), TradeConfig.SYNCH);
    	   assertTrue(orderData.getAccountID().equals(accountData.getAccountID()));
       }
   }
   catch(Throwable t)
   {
	   StringWriter sw = new StringWriter();
	   t.printStackTrace(new PrintWriter(sw));
	   String exceptionAsString = sw.toString();
	   fail("sell( " + accountData.getProfileID() + " ) threw exception " + exceptionAsString);
   }
}

@Test
public void testBuy() throws Exception 
{ 
   // Buy a random number of shares for a given user
   try 
   { 
   	   orderData = gatewayService.buy(accountData.getProfileID(), quoteData.getSymbol(), TradeConfig.rndQuantity(), TradeConfig.SYNCH);	   
	   assertTrue(orderData.getAccountID().equals(accountData.getAccountID()));
   }
   catch(Throwable t)
   {
	   StringWriter sw = new StringWriter();
	   t.printStackTrace(new PrintWriter(sw));
	   String exceptionAsString = sw.toString();
	   fail("buy( " + accountData.getProfileID() + " ) threw exception " + exceptionAsString);
   }
}
   
  
  // Quotes related tests
  
  
 @Test
 public void testGetMarketSummary() throws Exception 
 {
	   try 
	   { 
		   MarketSummaryDataBean marketData = gatewayService.getMarketSummary();
		   // Market summary only considers stocks with a symbol like 's:1__')
		   if (limitQuotes > 100 )
		   {
			   assertTrue( marketData.getTopGainers().size()>0 );
			   assertTrue( marketData.getTopLosers().size()>0 );
		   }
		   else
		   {
			   assertTrue( marketData.getTopGainers().size()==0 );
			   assertTrue( marketData.getTopLosers().size()==0 );			   
		   }
	   }
	   catch(Throwable t)
	   {
		   StringWriter sw = new StringWriter();
		   t.printStackTrace(new PrintWriter(sw));
		   String exceptionAsString = sw.toString();
		   fail("getMarketSummary() threw exception " + exceptionAsString);
	   }
 }
 
 @Test
 public void testGetQuote() throws Exception 
 {
	   try 
	   { 
		   QuoteDataBean actualValue = null;
		   for (int i = 0; i < limitQuotes; i++)
		   {
			   quoteData.setSymbol("s:" + i);
			   actualValue= gatewayService.getQuote(quoteData.getSymbol());
			   assertTrue(actualValue.equals( quoteData ));
		   }
	   }
	   catch(Throwable t)
	   {
		   StringWriter sw = new StringWriter();
		   t.printStackTrace(new PrintWriter(sw));
		   String exceptionAsString = sw.toString();
		   fail("getQuote() threw exception " + exceptionAsString);
	   }
 }
 
 @Test
 public void testGetAllQuotes() throws Exception 
 {
	   try 
	   { 
		   Collection<QuoteDataBean> actualQuotes = gatewayService.getAllQuotes(limitQuotes, offset);
		   assertTrue( actualQuotes.size() == limitQuotes );
	   }
	   catch(Throwable t)
	   {
		   StringWriter sw = new StringWriter();
		   t.printStackTrace(new PrintWriter(sw));
		   String exceptionAsString = sw.toString();
		   fail("getAllQuotes() threw exception " + exceptionAsString);
	   }
 }
 
 @Test
 public void testUpdateQuotePriceVolume() throws Exception 
 {
	   try
	   {  
		   double volume = TradeConfig.rndFloat(100000);
		   BigDecimal price = TradeConfig.getRandomPriceChangeFactor();
		   QuoteDataBean actualQuote = gatewayService.updateQuotePriceVolume( quoteData.getSymbol(), price, volume );
		   assertTrue( actualQuote.equals(quoteData) );
	   }
	   catch(Throwable t)
	   {
		   StringWriter sw = new StringWriter();
		   t.printStackTrace(new PrintWriter(sw));
		   String exceptionAsString = sw.toString();
		   fail("updateUpdateQuotePriceVolume( " + quoteData.getSymbol() + " ) threw exception " + exceptionAsString);
	   }
 }
 
}