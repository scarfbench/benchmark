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

package com.ibm.sample.daytrader.portfolios.service;

import com.fasterxml.jackson.databind.DeserializationFeature;
//Jackson
import com.fasterxml.jackson.databind.ObjectMapper;

// Daytrader

// Spring
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.context.annotation.PropertySource;
import com.ibm.sample.daytrader.portfolios.entities.QuoteDataBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * The remote call service to the quotes microservice.
 * 
 * @author
 *
 */

@Service
//@Configuration
//@PropertySource("classpath:application.properties")
public class QuotesRemoteCallService extends BaseRemoteCallService
{
	protected static ObjectMapper mapper = null;
	
	static 
	{
		mapper = new ObjectMapper(); // create once, reuse
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false); // ignore properties that are not declared
	}

	//
//  - Naming convention based service discovery
	@Value("${daytrader.services.quotes}")
	private static String quotesServiceRoute;
	/**
	 * 
	 * @see TradeServices#getQuote(String)
	 *
	 */
    public QuoteDataBean getQuote(String symbol) throws Exception
    {
    	String url = quotesServiceRoute + "/quotes/" + symbol;
	   	String responseString = invokeEndpoint(url, "GET", null); // Entity must be null for http method GET.
        QuoteDataBean quoteData = mapper.readValue(responseString,QuoteDataBean.class);
        return quoteData;
    }
        
}
