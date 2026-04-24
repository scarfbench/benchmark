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
package com.ibm.sample.daytrader.web;

//
//
//	- Moved database creation code into the microservices
//	- Fixed getRealPath returns null from SpringBoot executable jar
//  - Moved sample data generation into the microservices
//  - Generate sample data is a formal business operation in the Trade application.

//import java.util.ArrayList;

import com.ibm.sample.daytrader.core.api.TradeDBServices;
import com.ibm.sample.daytrader.core.direct.TradeJDBCDirect;
import com.ibm.sample.daytrader.utils.TradeConfig;

/**
 * TradeBuildDB uses operations provided by the TradeApplication to 
 *   (a) create the Database tables 
 *   (b) populate a DayTrader database without creating the tables. 
 * Specifically, a new DayTrader User population is created using
 * UserIDs of the form "uid:xxx" where xxx is a sequential number 
 * (e.g. uid:0, uid:1, etc.). New stocks are also created of the form "s:xxx",
 * again where xxx represents sequential numbers (e.g. s:1, s:2, etc.)
 */
public class TradeBuildDB {

    /**
     * Re-create the DayTrader db tables and populate them OR just populate a 
     * DayTrader DB, logging to the provided output stream
     */
    public TradeBuildDB(java.io.PrintWriter out, String warPath) throws Exception {

    	TradeAction tradeAction = new TradeAction();

        out.println("<HEAD><BR><EM> TradeBuildDB: Building DayTrader Database...</EM><BR>"
            + "This operation will take several minutes. Please wait...</HEAD>");
        out.println("<BODY>");

        if (warPath != null) 
        {
            TradeDBServices tradeDB = new TradeJDBCDirect();

            boolean success = false;
            Object[] sqlBuffer = null;

            // send the sql commands buffer to drop and recreate the Daytrader tables
            out.println("<BR>TradeBuildDB: **** Dropping and Recreating the DayTrader tables... ****</BR>");
            try 
            {
                success = tradeDB.recreateDBTables(sqlBuffer, out);
                if (!success) 
                {
                    out.println("<BR>TradeBuildDB: **** Unable to drop and recreate DayTrader Db Tables, "+
                        "please check for database consistency before continuing ****</BR></BODY>");
                }
                else
                {
                	out.println("<BR>TradeBuildDB: **** DayTrader tables successfully created! ****</BR><BR><b> "+
                			"Please use the \"Repopulate Daytrader Database\" link to populate your database.</b></BR><BR><BR></BODY>");
                }
            } 
            catch (Exception e) 
            {
                out.println("<BR>TradeBuildDB: **** Unable to drop and recreate DayTrader Db Tables, "+
                        "please check for database consistency before continuing ****</BR></BODY>");
            }
            return;
        } // end of createDBTables

        boolean success = false;
        
        TradeJDBCDirect tradesService = new TradeJDBCDirect();
        
        // Re-populate the quotes
        out.println("<BR>TradeBuildDB: **** Creating " + TradeConfig.getMAX_QUOTES() + " Quotes ****</BR>");
        try 
        {
        	int limit = 10;
        	for (int offset = 0; offset < TradeConfig.getMAX_QUOTES(); offset+=limit) 
        	{
        		tradesService.quotesBuildDB(limit,offset);
        		out.print("....." + "s:" + offset);
        		if (offset % 100 == 0) 
        		{
        			out.println(" -<BR>");
        			out.flush();
        		}
        	}
        }
        catch (Exception e)
       	{
        	out.println("<BR>TradeBuildDB: **** Unable to create Trade Quotes (s:0, s:1, ...) " +
                    "please check for database consistency before continuing ****</BR></BODY>");
        	return;        		
       	} 
        
        // Re-populate the users
        out.println("<BR>TradeBuildDB: **** Registering " + TradeConfig.getMAX_USERS() + " Users ****</BR>");
        try
        {
        	int limit = 50;
        	for (int offset = 0; offset < TradeConfig.getMAX_USERS(); offset+=limit) 
        	{
        		tradesService.tradeBuildDB(limit,offset);
        		if (offset % limit == 0) 
        		{
        			String userID = "uid:" + offset;
        			int holdings = tradesService.getHoldings(userID).size();
            		out.print("Account# " + offset  + " userID=" + userID + " has " + holdings + " holdings." );
        			out.println("<BR>");
        			out.flush();
        		}
        	}
        }
        catch (Exception e)
       	{
        	out.println("<BR>TradeBuildDB: **** Unable to register Trade Users (uid:0, uid:1, ...) " +
                    "please check for database consistency before continuing ****</BR></BODY>");
        	return;        		
       	} 
    	out.println("</BODY>");
    	return;  

    }
    
}

