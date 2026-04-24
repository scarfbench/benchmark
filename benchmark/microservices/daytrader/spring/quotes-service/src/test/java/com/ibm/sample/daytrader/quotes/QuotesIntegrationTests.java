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

package com.ibm.sample.daytrader.quotes;

import com.ibm.sample.daytrader.quotes.service.QuotesRemoteCallService;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.ibm.sample.daytrader.quotes.core.beans.MarketSummaryDataBean;
import com.ibm.sample.daytrader.quotes.core.beans.RunStatsDataBean;
import com.ibm.sample.daytrader.quotes.entities.QuoteDataBean;
import com.ibm.sample.daytrader.quotes.utils.TradeConfig;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.util.Collection;

//
//Don't do any logging from the integration tests unless you send it to its own logger;
//otherwise the test log messages will be written to the server log and the server log
//messages will not written. For debugging, you will need the server log messages.

public class QuotesIntegrationTests {
	
	// Remote call service for testing this microservice
	private static QuotesRemoteCallService quotesService = new QuotesRemoteCallService();
	
	// Random data for testing this microservice
    private static QuoteDataBean quoteData = QuoteDataBean.getRandomInstance();
    private static int limit = TradeConfig.getMAX_QUOTES(); // populate quotes for the test
    private static int offset = 0;
    
    // Create/populate database before test
    @Before
    public void runBeforeTestMethod() 
    {
    	quoteData.setSymbol("s:" + (limit-1)); // market summary analyzes stocks with symbols s:100 .. s:199

    	try // create the database 
     	{
        	quotesService.recreateDBTables();
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
        	quotesService.tradeBuildDB(limit,offset);
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
        	RunStatsDataBean runStats = quotesService.resetTrade(true);
        }
  	   catch(Throwable t)
  	   {
  		   StringWriter sw = new StringWriter();
  		   t.printStackTrace(new PrintWriter(sw));
  		   String exceptionAsString = sw.toString();
  		   fail("resetTrade(true) threw exception " + exceptionAsString);
  	   	}
   }
    
    @Test
    public void testTradeBuildDB() throws Exception 
    {   
 	   try 
 	   { 
 		   assertTrue( quotesService.tradeBuildDB( limit, offset ));
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
   public void testGetMarketSummary() throws Exception 
   {
	   try 
	   { 
		   MarketSummaryDataBean marketData = quotesService.getMarketSummary();
		   // Market summary only considers stocks with a symbol like 's:1__')
		   if (limit > 100 )
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
		   for (int i = 0; i < limit; i++)
		   {
			   quoteData.setSymbol("s:" + i);
			   actualValue= quotesService.getQuote(quoteData.getSymbol());
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
		   Collection<QuoteDataBean> actualQuotes = quotesService.getAllQuotes(limit, offset);
		   assertTrue( actualQuotes.size() == limit );
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
		   QuoteDataBean actualQuote = quotesService.updateQuotePriceVolume( quoteData.getSymbol(), price, volume );
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