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

package com.ibm.sample.daytrader.accounts;

import com.ibm.sample.daytrader.accounts.entities.AccountDataBean;
import com.ibm.sample.daytrader.accounts.entities.AccountProfileDataBean;
import com.ibm.sample.daytrader.accounts.service.AccountsRemoteCallService;
import com.ibm.sample.daytrader.accounts.utils.TradeConfig;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.springframework.boot.test.context.SpringBootTest;

import java.io.PrintWriter;
import java.io.StringWriter;

//Don't do any logging from the integration tests unless you send it to its own logger;
//otherwise the test log messages will be written to the server log and the server log
//messages will not written. For debugging, you will need the server log messages.

@SpringBootTest(classes = AccountsApplication.class)
public class AccountsIntegrationTests {
	
	// The remote call service for testing this microservice
	private static AccountsRemoteCallService accountsService = new AccountsRemoteCallService();
	
	// Random data for testing this microservice
    private static AccountDataBean accountData = AccountDataBean.getRandomInstance();
    private static AccountProfileDataBean profileData = AccountProfileDataBean.getRandomInstance();
    private static int limit = TradeConfig.getMAX_USERS(); // populate users for the test
    private static int offset = 0;
    
    // Create/populate database before test
    @Before
    public void runBeforeTestMethod() 
    {
    	// Init profile to one in db
        profileData.setUserID("uid:0");
        profileData.setPassword("xxx");
        // Init account to one in db
        accountData.setProfileID("uid:0");
        accountData.setAccountID(0);
        
        try // create the database
     	{
        	accountsService.recreateDBTables();
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
        	accountsService.tradeBuildDB(limit,offset);
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
        	accountsService.resetTrade(true);
        }
        catch(Throwable t)
        {
     		StringWriter sw = new StringWriter();
     		t.printStackTrace(new PrintWriter(sw));
     		String exceptionAsString = sw.toString();
     		fail("recreateDBTables() threw exception " + exceptionAsString);
        }
   }

   // Notes: can't register without starting the portfolios microservice.
   // So we'll test the register method as part of the gateway integration 
   // 

   @Test
   public void testTradeBuildDB() throws Exception 
   {   
 	   try 
 	   { 
 		   assertTrue( accountsService.tradeBuildDB( limit, offset ));
 	   }
 	   catch(Throwable t)
 	   {
 		   StringWriter sw = new StringWriter();
 		   t.printStackTrace(new PrintWriter(sw));
 		   String exceptionAsString = sw.toString();
     	   fail("tradeBuildDB(" + limit + "," + offset + ") threw exception " + exceptionAsString);
 	   }
   }
   
   @Test
   public void testResetTrade() throws Exception 
   {   
 	   try 
 	   { 
 		   assertTrue( accountsService.resetTrade(false) != null );
 	   }
 	   catch(Throwable t)
 	   {
 		   StringWriter sw = new StringWriter();
 		   t.printStackTrace(new PrintWriter(sw));
 		   String exceptionAsString = sw.toString();
     	   fail("tradeBuildDB(false) threw exception " + exceptionAsString);
 	   }
   }

   @Test
   public void testGetAccountProfileData() throws Exception 
   {
	   try 
	   { 
		   AccountProfileDataBean actualValue = null;
		   for (int i = 0; i < limit; i++)
		   {
			   profileData.setUserID("uid:" + i);
			   actualValue= accountsService.getAccountProfileData(profileData.getUserID());
			   assertTrue(profileData.equals( actualValue ));
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
		   AccountProfileDataBean actualResult = accountsService.updateAccountProfile( profileData );
		   assertTrue( profileData.getAddress().equals( actualResult.getAddress() ));
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
		   AccountDataBean actualResult = accountsService.login( accountData.getProfileID(), profileData.getPassword() );
		   assertTrue( accountData.equals( actualResult ));
	   }
	   catch(Throwable t)
	   {
		   StringWriter sw = new StringWriter();
		   t.printStackTrace(new PrintWriter(sw));
		   String exceptionAsString = sw.toString();
		   fail("login( " + profileData.getUserID() + " ) threw exception " + exceptionAsString);
	   }
   }
   
   @Test
   public void testLogout()
   {
	   try 
	   {
		   accountsService.logout( profileData.getUserID() );
	   }
	   catch(Throwable t)
	   {
   			StringWriter sw = new StringWriter();
   			t.printStackTrace(new PrintWriter(sw));
   			String exceptionAsString = sw.toString();
   			fail("logout( " + profileData.getUserID() + " ) threw exception " + exceptionAsString);
	   }
    }
}
