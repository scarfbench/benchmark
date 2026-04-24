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

package com.ibm.sample.daytrader.portfolios;

import com.ibm.sample.daytrader.portfolios.service.PortfoliosRemoteCallService;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.ibm.sample.daytrader.portfolios.entities.AccountDataBean;
import com.ibm.sample.daytrader.portfolios.entities.HoldingDataBean;
import com.ibm.sample.daytrader.portfolios.entities.OrderDataBean;
import com.ibm.sample.daytrader.portfolios.utils.TradeConfig;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Collection;

//
//Don't do any logging from the integration tests unless you send it to its own logger;
//otherwise the test log messages will be written to the server log and the server log
//messages will not written. For debugging, you will need the server log messages.

public class PortfoliosIntegrationTests {
	
	// The remote call service for testing this microservice
	private static PortfoliosRemoteCallService portfoliosService = new PortfoliosRemoteCallService();
	
	// Random data for testing this microservice
    private static AccountDataBean accountData = AccountDataBean.getRandomInstance();
    private static int limit = TradeConfig.getMAX_USERS(); // populate users for the test
    private static int offset = 0;
        
    // Create/populate database before test
    @Before
    public void runBeforeTestMethod() 
    {
        // Init account to one in db
        accountData.setAccountID(limit-1);
        accountData.setProfileID("uid:" + accountData.getAccountID());
        
        try // create the database
     	{
        	portfoliosService.recreateDBTables();
     	}
     	catch(Throwable t)
     	{
     		StringWriter sw = new StringWriter();
     		t.printStackTrace(new PrintWriter(sw));
     		String exceptionAsString = sw.toString();
     		fail("recreateDBTables() threw exception " + exceptionAsString);
     	}

        try // populate the database
     	{
        	portfoliosService.tradeBuildDB(limit,offset);
     	}
     	catch(Throwable t)
     	{
     		StringWriter sw = new StringWriter();
     		t.printStackTrace(new PrintWriter(sw));
     		String exceptionAsString = sw.toString();
     		fail("tradeBuildDB() threw exception " + exceptionAsString);
     	}
    }

    // Reset database after test method
    @After
    public void runAfterTestMethod() {
        try 
        {
        	portfoliosService.resetTrade(true);
        }
        catch(Throwable t)
        {
     		StringWriter sw = new StringWriter();
     		t.printStackTrace(new PrintWriter(sw));
     		String exceptionAsString = sw.toString();
     		fail("recreateDBTables() threw exception " + exceptionAsString);
        }
   }

    
  @Test
  public void testGetAccountData() throws Exception 
  {
	   try 
	   { 
		   AccountDataBean actualValue = null;
		   for (int i = 0; i < limit; i++)
		   {
			   accountData.setProfileID("uid:" + i);
			   actualValue= portfoliosService.getAccountData(accountData.getProfileID());
			   assertTrue(actualValue.equals( accountData ));
		   }
	   }
 	   catch(Throwable t)
 	   {
 		   StringWriter sw = new StringWriter();
 		   t.printStackTrace(new PrintWriter(sw));
 		   String exceptionAsString = sw.toString();
		   fail("getAccountData( " + accountData.getProfileID() + " ) threw exception " + exceptionAsString);
	   }
  }
    
  @Test
  public void testGetOrders() throws Exception 
  {
	   try 
	   { 
		   Collection<OrderDataBean> orders = null;
		   for (int i = 0; i < limit; i++)
		   {
			   accountData.setProfileID("uid:" + i);
			   orders = portfoliosService.getOrders(accountData.getProfileID());
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
		   for (int i = 0; i < limit; i++)
		   {
			   accountData.setProfileID("uid:" + i);
			   holdings = portfoliosService.getHoldings(accountData.getProfileID());
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
		   for (int i = 0; i < limit; i++)
		   {
			   accountData.setProfileID("uid:" + i);
			   orders = portfoliosService.getClosedOrders(accountData.getProfileID());
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

}
