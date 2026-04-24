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

package com.ibm.sample.daytrader.accounts.service;

//Jackson
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.math.BigDecimal;

// Daytrader
import com.ibm.sample.daytrader.accounts.beans.RunStatsDataBean;
import com.ibm.sample.daytrader.accounts.entities.AccountDataBean;
import com.ibm.sample.daytrader.accounts.entities.AccountProfileDataBean;

// Spring
import org.springframework.stereotype.Service;

/**
 * The remote call service to the accounts microservice.
 */

//Don't do any logging from the integration tests unless you send it to its own logger;
//otherwise the test log messages will be written to the server log and the server log
//messages will not written. For debugging, you will need the server log messages.

@Service
public class AccountsRemoteCallService extends BaseRemoteCallService
{
	protected static ObjectMapper mapper = null;
	
	static 
	{
		mapper = new ObjectMapper(); // create once, reuse
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false); // ignore properties that are not declared
	}

//  - Naming convention based service discovery 
//	  private static String accountsServiceRoute = System.getenv("DAYTRADER_ACCOUNTS_SERVICE");
	  private static String accountsServiceRoute = "http://localhost:1443";


	   /**
		*
		* @see AccountsService#tradeBuildDB(int,int)
		*
		*/
		public boolean tradeBuildDB(int limit, int offset) throws Exception 
		{
	  		String url = accountsServiceRoute + "/admin/tradeBuildDB?limit=" + limit + "&offset=" + offset;
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
		String url = accountsServiceRoute + "/admin/resetTrade?deleteAll=" + deleteAll;
  		String responseEntity = invokeEndpoint(url, "GET", null); // Entity must be null for http method GET.
  		RunStatsDataBean runStatsData = mapper.readValue(responseEntity,RunStatsDataBean.class);
  		return runStatsData; 
	}

	public boolean recreateDBTables() throws Exception 
	{
		String url = accountsServiceRoute + "/admin/recreateDBTables";
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
		String url = accountsServiceRoute + "/accounts/" + userID;
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
		String url = accountsServiceRoute + "/accounts/" + userID + "/profiles";
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
		String url = accountsServiceRoute + "/accounts/" + profileData.getUserID() + "/profiles";
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
			
  		// web app only expected null or non-null account
  		String url = accountsServiceRoute + "/login/" + userID;
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
		String url = accountsServiceRoute + "/logout/" + userID;
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
  		String url = accountsServiceRoute + "/accounts";
        
  		// Construct the account data from that given params
  		AccountDataBean accountData = new AccountDataBean();
  		accountData.setProfileID(userID);
  		accountData.setOpenBalance(openBalance);
  		accountData.setBalance(openBalance);
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
    
}