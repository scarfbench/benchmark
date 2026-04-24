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

// DayTrader
import com.ibm.sample.daytrader.accounts.entities.AccountDataBean;
import com.ibm.sample.daytrader.accounts.utils.Log;

//Jackson Object Mapping
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

// Daytrader

// Spring Framework
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;



/**
 * The remote call service to the trader gateway service.
 * 
 * Implement a remote call service if you want to host the gateway in its own microservice.
 * For now, the gateway is architected to be pulled out into its own microservice, but it
 * did not seem to warrant its own separate process. For that reason, the web tier calls a
 * local gateway. It can easily be hosted in its own microservice later if warranted.
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

//  - Naming convention based service discovery 
	private static String portfoliosServiceRoute;
	@Value("${daytrader.services.portfolios}")
	public void setPortfoliosServiceRoute(String route) { portfoliosServiceRoute = route;}

   /**
	*
	* @see PortfoliosService#getAccountData(String)
	*
	*/
    public AccountDataBean getAccountData(String userID) throws Exception
    {
   		String url = portfoliosServiceRoute + "/portfolios/" + userID;
   		Log.debug("PortfoliosRemoteCallService.getAccountData() - " + url);
   		String responseEntity = invokeEndpoint(url, "GET", null); // Entity must be null for http method GET.
   		AccountDataBean accountData = mapper.readValue(responseEntity,AccountDataBean.class);
   		return accountData;
    }
	
   /**
	*
	* @see PortofliosService#register(AccountDataBean)
	*
	*/
    public AccountDataBean register(AccountDataBean accountData) throws Exception 
    {
    	// Construct the portfolio data from that given params
  	  	String url = portfoliosServiceRoute + "/portfolios";
    	Log.debug("PortfoliosRemoteCallService.getAccountData() - " + url);
  		String accountDataInString = mapper.writeValueAsString(accountData);
  		String responseEntity = invokeEndpoint(url, "POST", accountDataInString);
  		accountData = mapper.readValue(responseEntity,AccountDataBean.class);
  		return accountData;
    }    	
}

