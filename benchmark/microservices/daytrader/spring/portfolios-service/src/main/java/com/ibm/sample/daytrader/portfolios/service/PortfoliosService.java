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

//- Each microservice has its own log
import com.ibm.sample.daytrader.portfolios.entities.*;
import com.ibm.sample.daytrader.portfolios.direct.core.FinancialUtils;
import com.ibm.sample.daytrader.portfolios.direct.core.KeySequenceDirect;
import com.ibm.sample.daytrader.portfolios.direct.beans.RunStatsDataBean;
import com.ibm.sample.daytrader.portfolios.entities.*;
import com.ibm.sample.daytrader.portfolios.utils.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.ArrayList;

import javax.naming.InitialContext;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import javax.sql.DataSource;
import javax.ws.rs.ClientErrorException;
import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.core.Response;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;

import com.ibm.sample.daytrader.portfolios.utils.TradeConfig;
import org.springframework.stereotype.Service;

/**
 * A microservice to manage portfolios (stock holdings and orders).
 * 
 * @author
 *
 * Notes: during stage 3 cleaned up some of the code
 *   - Removed unused variables, methods, and SQL
 *   - Replaced collections with generic collections
 */

@Service
public class PortfoliosService
{
	//	- Each microservice has their own private database (datasource)
    private static String dsName = TradeConfig.PORTFOLIOS_DATASOURCE;

    //	-  @Resource annotation is not supported on static fields
    private static DataSource datasource = null;

    private static InitialContext context;
    
    //	- Enables portfolios microservice to consume accounts and quotes microservices
    private static QuotesRemoteCallService quotesService = new QuotesRemoteCallService();

    /**
     * Zero arg constructor for PortfoliosService
     */
    public PortfoliosService() {
        if (initialized == false) init();
    }
    
    /**
	 * 
	 * PortfoliosService#tradeBuildDB(int,int)
	 *
	 */
    public Boolean tradeBuildDB(int limit, int offset) throws Exception
    {
    	
    	// Moved this code from the web tier to the microservice where it belongs
    	// Generate sample data is a formal business operation in the Trade application.
    	
    	OrderDataBean orderData = null;
        BigDecimal initialBalance = null;
    	AccountDataBean accountData = null;
    
    	if (offset == 0) resetTrade(true); // delete any rows from db before repopiulating
	
        int holdings = 0;
        Connection conn = null;
        
        try 
        {
            conn = getConn();
        	for (int i = 0; i < limit; i++) 
        	{
        		int accountID = i + offset;
        		// Set the initial balance
        		if (accountID == 0) 
        		{ 
        			initialBalance = new BigDecimal(1000000); // uid:0 starts with a cool million.
        		}
        		else
        		{
        			initialBalance = new BigDecimal(TradeConfig.rndInt(100000) + 200000);
        		}
            
        		// Construct the account from random data
        		accountData = new AccountDataBean();
        		accountData.setAccountID(accountID);
        		accountData.setProfileID("uid:" + accountID);
        		accountData.setOpenBalance(initialBalance);
        		accountData.setBalance(initialBalance);
    		
        		// Register the user
        		register(conn,accountData);
            
        		//Create the user's holdings
        		// 0-MAX_HOLDING (inclusive), avg holdings per user = (MAX-0)/2
        		holdings = TradeConfig.rndInt(TradeConfig.getMAX_HOLDINGS() + 1);
        		for (int j = 0; j < holdings; j++) 
        		{
        			// Construct the order from random data
        			orderData = new OrderDataBean();
        			orderData.setOrderFee(TradeConfig.getOrderFee("buy"));
        			orderData.setSymbol(TradeConfig.rndSymbol());
        			orderData.setAccountID(accountID);
        			orderData.setQuantity(TradeConfig.rndQuantity());
        			orderData.setOrderType("buy");
        			orderData.setOrderStatus("open");
        			// Construct the quote from random data
        			QuoteDataBean quoteData = new QuoteDataBean();
        			quoteData.setPrice(new BigDecimal(TradeConfig.rndPrice()));
        			quoteData.setSymbol(orderData.getSymbol());
        			// Process the buy order for the given user
        			buy(conn,accountData, orderData, quoteData, TradeConfig.SYNCH);
        		} // end-for
        	} // end-for
    		commit(conn);
        } 
        catch (Exception e) 
        {
            rollBack(conn, e);
            throw e;
        } 
        finally 
        {
            releaseConn(conn);
        }
    	return true;
    } 
     
    /**
 	*
 	* added this method to inject the account data into the portfolios service so the 
 	* portfolios service doesn't have to call back to the account microservice. moreover, these
 	* fields are actually managed by the portfolios microservice; not accounts. the are cached
 	* in accounts for read only access
 	* 
 	* @author
 	*
 	* This was a net new method created; so it returns the portfolio data beans;
 	* Any new methods should be returning the portfolio data beans, but reuse of
 	* existing code can return account data beans.
 	*/
     public AccountDataBean register(AccountDataBean accountData) throws Exception 
     {
         Connection conn = null;
         try 
         {
             conn = getConn();
             register(conn,accountData);
             commit(conn);
         } catch (Exception e) 
         {
             rollBack(conn, e);
             throw e;
         } 
         finally 
         {
             releaseConn(conn);
         }
         return accountData;
     }
     
     /**
  	*
  	* added this method to inject the account data into the portfolios service so the 
  	* portfolios service doesn't have to call back to the account microservice. moreover, these
  	* fields are actually managed by the portfolios microservice; not accounts. the are cached
  	* in accounts for read only access
  	* 
  	* @author
  	*
    * reused from TradeJDBCDirect#getAccountData(Connection,String) with minor changes
    * This was a net new method created; so it returns the portfolio data beans; existing methods
    * continue to return account data beans so they can be reused without change
  	*/
     public AccountDataBean getAccountData(String userID) throws Exception {
         AccountDataBean accountData = null;
         Connection conn = null;
         try {
             conn = getConn();            
             accountData = getAccountData(conn,userID);
             commit(conn);

         } catch (Exception e) {
             rollBack(conn, e);
             throw e;
         } finally {
             releaseConn(conn);
         }
         return accountData;
     }
    
    /**
  	*
  	* @see TradeServices#resetTrade(boolean)
  	*
  	*/
   	public RunStatsDataBean resetTrade(boolean deleteAll) throws Exception 
   	{
  		//		-  Reset usage statistics for account microservices and only the accounts
  		//         microservice. The other microservices will be responsible for resetting
        //    	   their own usage statistics

   		RunStatsDataBean runStatsData = new RunStatsDataBean();
   		Connection conn = null;
   		
		if (deleteAll) 
		{   
			// delete the rows and return
   			conn = getConn();
   			PreparedStatement stmt = null;
			try 
			{
				stmt = getStatement(conn, "delete from accountejb");
				stmt.executeUpdate();
				stmt.close();
                stmt = getStatement(conn, "delete from holdingejb");
                stmt.executeUpdate();
                stmt.close();
                stmt = getStatement(conn, "delete from orderejb");
                stmt.executeUpdate();
                stmt.close();
				// fix pkey unique constraint violation
				stmt = getStatement(conn, "delete from keygenejb");
				stmt.executeUpdate();
				stmt.close();
				commit(conn);
				//
				// (Re-)initialize the key generator
				KeySequenceDirect.initialize(getConn());
			}	
		    catch (Exception e) 
		    {
		   		rollBack(conn, e);
		   		throw e;
		    } 
		    finally 
		    {
		        releaseConn(conn);
		    }
			return runStatsData;
		}
		else
		{
			// calculate usage stats
   			conn = getConn();
   			PreparedStatement stmt = null;
   			ResultSet rs = null;
   			try 
   			{
   	            stmt = getStatement(conn, "delete from holdingejb where holdingejb.account_accountid is null");
   	            stmt.executeUpdate();
   	            stmt.close();

   	            // imported accountejb into portfolios microservice to do the inner join across the accountejb 
   	            // (in this microservice) instead of the accountejb (in the accounts microservice). As a result of this,
   	            // here were no changes to the sql statement
   	            stmt =
   	                   getStatement(
   	                       conn,
   	                       "delete from orderejb where account_accountid in (select accountid from accountejb a where a.profile_userid like 'ru:%')");
   	            stmt.executeUpdate();
   	            stmt.close();

   	            // imported accountejb into portfolios microservice to do the inner join across the accountejb 
   	            // (in this microservice) instead of the accountejb (in the accounts microservice). As a result of this,
   	            // here were no changes to the sql statement
   	            stmt =
   	                    getStatement(
   	                        conn,
   	                        "delete from holdingejb where account_accountid in (select accountid from accountejb a where a.profile_userid like 'ru:%')");
   	            stmt.executeUpdate();
   	            stmt.close();          
   	            
   	   			// Cound and delete random users (with id that start with "ru:%")
   	            // this code needs to be in the portfolios to accounts 
   	   			stmt = getStatement(conn, "delete from accountejb where profile_userid like 'ru:%'");
   	   			int newUserCount = stmt.executeUpdate();
   	   			runStatsData.setNewUserCount(newUserCount);
   	   			stmt.close();
   	            
   	            // count holdings for trade users
   	            // imported accountejb into portfolios microservice to do the inner join across the accountejb 
   	            // (in this microservice) instead of the accountejb (in the accounts microservice). As a result of this,
   	            // here were no changes to the sql statement
   	            stmt =
   	                getStatement(conn,
   	                    "select count(holdingid) as \"holdingCount\" from holdingejb h where h.account_accountid in "
   	                        + "(select accountid from accountejb a where a.profile_userid like 'uid:%')");

   	            rs = stmt.executeQuery();
   	            rs.next();
   	            int holdingCount = rs.getInt("holdingCount");
   	            runStatsData.setHoldingCount(holdingCount);
   	            stmt.close();
   	            rs.close();

   	            // count orders for trade users
   	            // imported accountejb into portfolios microservice to do the inner join across the accountejb 
   	            // (in this microservice) instead of the accountejb (in the accounts microservice). As a result of this,
   	            // here were no changes to the sql statement
   	            stmt =
   	                getStatement(conn,
   	                    "select count(orderid) as \"orderCount\" from orderejb o where o.account_accountid in "
   	                        + "(select accountid from accountejb a where a.profile_userid like 'uid:%')");

   	            rs = stmt.executeQuery();
   	            rs.next();
   	            int orderCount = rs.getInt("orderCount");
   	            runStatsData.setOrderCount(orderCount);
   	            stmt.close();
   	            rs.close();

   	            // count orders by type for trade users
   	            // imported accountejb into portfolios microservice to do the inner join across the accountejb 
   	            // (in this microservice) instead of the accountejb (in the accounts microservice). As a result of this,
   	            // here were no changes to the sql statement
   	            stmt =
   	                getStatement(conn,
   	                    "select count(orderid) \"buyOrderCount\"from orderejb o where (o.account_accountid in "
   	                        + "(select accountid from accountejb a where a.profile_userid like 'uid:%')) AND "
   	                        + " (o.orderType='buy')");

   	            rs = stmt.executeQuery();
   	            rs.next();
   	            int buyOrderCount = rs.getInt("buyOrderCount");
   	            runStatsData.setBuyOrderCount(buyOrderCount);
   	            stmt.close();
   	            rs.close();

   	            // count orders by type for trade users
   	            // imported accountejb into portfolios microservice to do the inner join across the accountejb 
   	            // (in this microservice) instead of the accountejb (in the accounts microservice). As a result of this,
   	            // here were no changes to the sql statement
   	            stmt =
   	                getStatement(conn,
   	                    "select count(orderid) \"sellOrderCount\"from orderejb o where (o.account_accountid in "
   	                        + "(select accountid from accountejb a where a.profile_userid like 'uid:%')) AND "
   	                        + " (o.orderType='sell')");

   	            rs = stmt.executeQuery();
   	            rs.next();
   	            int sellOrderCount = rs.getInt("sellOrderCount");
   	            runStatsData.setSellOrderCount(sellOrderCount);
   	            stmt.close();
   	            rs.close();

   	            // Delete cancelled orders
   	            stmt = getStatement(conn, "delete from orderejb where orderStatus='cancelled'");
   	            int cancelledOrderCount = stmt.executeUpdate();
   	            runStatsData.setCancelledOrderCount(cancelledOrderCount);
   	            stmt.close();
   	            rs.close();

   	            // count open orders by type for trade users
   	            // imported accountejb into portfolios microservice to do the inner join across the accountejb 
   	            // (in this microservice) instead of the accountejb (in the accounts microservice). As a result of this,
   	            // here were no changes to the sql statement
   	            stmt =
   	                getStatement(conn,
   	                    "select count(orderid) \"openOrderCount\"from orderejb o where (o.account_accountid in "
   	                        + "(select accountid from accountejb a where a.profile_userid like 'uid:%')) AND "
   	                        + " (o.orderStatus='open')");

   	            rs = stmt.executeQuery();
   	            rs.next();
   	            int openOrderCount = rs.getInt("openOrderCount");
   	            runStatsData.setOpenOrderCount(openOrderCount);

   	            stmt.close();
   	            rs.close();
   	            // Delete orders for holding which have been purchased and sold
   	            stmt = getStatement(conn, "delete from orderejb where holding_holdingid is null");
   	            int deletedOrderCount = stmt.executeUpdate();
   	            runStatsData.setDeletedOrderCount(deletedOrderCount);
   	            stmt.close();
   	            rs.close();

   	            commit(conn);
   			} 
   			catch (Exception e) 
   			{
   				rollBack(conn, e);
   				throw e;
   			} 
   			finally 
   			{
   				releaseConn(conn);
   			}

   			return runStatsData;
		}
   	}

    /**
 	*
 	* @see TradeDBServices#recreateDBTables(Object[],PrintWriter)
 	*
 	*/
     public boolean recreateDBTables() throws Exception 
     {
         Object[] sqlBuffer = null;
         
 		//	- Each microservice recreates their own private database	
 		//		PrintWriter is ignored as each microservice runs in separate process from web
 		
     	String ddlFile = null;

     	// Discover the RDB vendor
     	String dbProductName = checkDBProductName();

     	// 1. Locate DDL file for the specified database
     	//    Didn't use try catch block here; the following code does not manage db
     	//    resources. as such it does not need to catch exceptions and close them.
   		if (dbProductName.startsWith("DB2/")) // if db is DB2
   		{
   			ddlFile = "/dbscripts/oracle/PortfoliosTable.ddl";
   		}
   		else if (dbProductName.startsWith("Apache Derby")) // if db is Derby
   		{
   			ddlFile = "/dbscripts/derby/PortfoliosTable.ddl";
   		}
   		else if (dbProductName.startsWith("Oracle")) // if the Db is Oracle
   		{
   			ddlFile = "/dbscripts/oracle/PortfoliosTable.ddl";
   		} 
   		else if (dbProductName.startsWith("MySQL")) // if the Db is MySQL
   		{
   			ddlFile = "/dbscripts/mysql/PortfoliosTable.ddl";
   		}
   		else if (dbProductName.startsWith("Informix Dynamic Server")) // if the Db is Informix dynamic server
   		{
   			ddlFile = "/dbscripts/informix/PortfoliosTable.ddl";
   		}
   		else if (dbProductName.startsWith("Microsoft SQL Server")) // if the Db is Microsoft SQLServer
   		{
   			ddlFile = "/dbscripts/sqlserver/PortfoliosTable.ddl";
   		}
   		else if (dbProductName.startsWith("PostgreSQL")) // if the Db is PostgreSQL
   		{
   			ddlFile = "/dbscripts/postgre/PortfoliosTable.ddl";
   		} 
   		else // Unsupported "Other" Database
   		{
   			ddlFile = "/dbscripts/other/PortfoliosTable.ddl";
   			Log.debug("PortfoliosService:recreateDBTables() - " + dbProductName + " is unsupported/untested; use it at your own risk");
   		}

     	// 2. Parse the DDL file and fill the SQL commands into a buffer
     	//    Didn't use try catch block here; the following code does not manage db
     	//    resources. as such it does not need to catch exceptions and close them.
   		sqlBuffer = parseDDLToBuffer(this.getClass().getResourceAsStream(ddlFile));   	
   		if ((sqlBuffer == null) || (sqlBuffer.length == 0)) 
   		{
   			throw new InternalServerErrorException("DDL file " + ddlFile + " for specified database " + dbProductName + " is empty");
   		}

     	// 3. Drop and recreate the database tables...");
   		//    Use try/catch/finally to manage db resources
 		Connection conn = null;
   		try 
   		{
   			conn = getConn();
   			Statement stmt = conn.createStatement();
   			int bufferLength = sqlBuffer.length;
   			for (int i = 0; i < bufferLength; i++) 
   			{
   				try 
   				{
   					stmt.executeUpdate((String) sqlBuffer[i]);
   					// commit(conn);
   				} 
   				catch (SQLException ex) 
   				{
   					if (((String) sqlBuffer[i]).indexOf("DROP TABLE") > 0) 
   					{
		         		StringWriter sw = new StringWriter();
		         		ex.printStackTrace(new PrintWriter(sw));
		         		String exceptionAsString = sw.toString();
		        		// Ignore exception; table may not exist
   					}
   					else
   					{
   						throw ex;
   					}
   				}
     		}
     		stmt.close();
     		commit(conn);
     	}
     	catch (Exception e) 
     	{
     		rollBack(conn, e);
     		throw e;
     	} 
     	finally 
     	{
     		releaseConn(conn);
     	}
    		
		//
		// (Re-)initialize the key generator
		KeySequenceDirect.initialize(getConn());
     	return true;
     }
     
	/**
	 * 
	 * @see TradeServices#buy(String, String, double, int)
	 *
	 */
    public OrderDataBean buy(String userID, String symbol, double quantity, Integer mode) throws Exception 
	{       
    	//
        // Mode is ignored for now. Later it will be used for async order processing
        Connection conn = null;
        OrderDataBean orderData = null;

        try {

            conn = getConn();
			
            AccountDataBean accountData = getAccountData(conn,userID);
            
        	// Construct the order data
        	orderData = new OrderDataBean();
        	orderData.setOrderFee(TradeConfig.getOrderFee("buy"));
        	orderData.setAccountID(accountData.getAccountID());
        	orderData.setSymbol(symbol);
    		orderData.setQuantity(quantity);
    		orderData.setOrderType("buy");
    		orderData.setOrderStatus("open");
    		
    		// 
    		// Async processing would create and return the order as indicated
    		// above, publish an order created event, then process the rest of
    		// the order below this comment
    		
			//
            // Ask quotes microservice for the data instead of accessing directly
            QuoteDataBean quoteData = quotesService.getQuote(symbol);
    		
    		orderData = buy(conn, accountData, orderData, quoteData, mode);
    		
    		commit(conn);
    		
    		return orderData;
        
        } catch (Exception e) {
            rollBack(conn, e);
            throw e;
        } finally {
            releaseConn(conn);
        }
        
    }

	/**
	 * 
	 * @see TradeServices#sell(String, Integer, Integer)
	 *
	 */
    public OrderDataBean sell(String userID, Integer holdingID, Integer mode) throws Exception 
	{
    	// Mode is ignored for now. Later it will be used for async order processing
        Connection conn = null;
        OrderDataBean orderData = null;

        /*
         * total = (quantity * purchasePrice) + orderFee
         */
        BigDecimal total;

        try 
        {
            conn = getConn();
            AccountDataBean accountData = getAccountData(conn,userID);
            if ((accountData == null)) 
            {
                throw new NotFoundException("Unable to find account for userID: " + userID);
            }
            
            HoldingDataBean holdingData = getHoldingData(conn,holdingID.intValue());
            if (holdingData == null)
            {
            	throw new NotFoundException("Unable to find holding for holdingID: " + holdingID);
            }
            
            // 
            //  Ask quotes microservice for the data instead of accessing directly
            QuoteDataBean quoteData = quotesService.getQuote(holdingData.getQuoteID());
            if (quoteData == null)
        	{
            	throw new NotFoundException("Unable to find quote for symbol: " + holdingData.getQuoteID());
        	}

            double quantity = holdingData.getQuantity();
            orderData = createOrder(conn, accountData, quoteData, holdingData, "sell", quantity);

            // Set the holdingSymbol purchaseDate to selling to signify the sell is "inflight"
            updateHoldingStatus(conn, holdingData.getHoldingID(), holdingData.getQuoteID());

            // UPDATE -- account should be credited during completeOrder
            BigDecimal price = quoteData.getPrice();
            BigDecimal orderFee = orderData.getOrderFee();
            total = (new BigDecimal(quantity).multiply(price)).subtract(orderFee);
            creditAccountBalance(conn, accountData, total);

            completeOrder(conn, orderData.getOrderID());

            orderData = getOrderData(conn, orderData.getOrderID().intValue());

            commit(conn);

        } catch (Exception e) {
            rollBack(conn, e);
            throw e;
        } finally {
            releaseConn(conn);
        }

        return orderData;
    }
    
	/**
	 * 
	 * @see TradeServices#getOrders(String)
	 *
	 */
    public Collection<OrderDataBean> getOrders(String userID) throws Exception {
        Collection<OrderDataBean> orderDataBeans = new ArrayList<OrderDataBean>();
        Connection conn = null;
        try 
        {
            conn = getConn();
            PreparedStatement stmt = getStatement(conn, getOrdersByUserSQL);
            stmt.setString(1, userID);

            ResultSet rs = stmt.executeQuery();

            // TODO: return top 50 orders for now -- next version will add a
            // getAllOrders method
            // also need to get orders sorted by order id descending
            int i = 0;
            while ((rs.next()) && (i++ < 50)) {
                OrderDataBean orderData = getOrderDataFromResultSet(rs);
                orderDataBeans.add(orderData);
            }

            stmt.close();
            commit(conn);

        } catch (Exception e) {
            rollBack(conn, e);
            throw e;
        } finally {
            releaseConn(conn);
        }
        return orderDataBeans;
    }

	/**
	 * 
	 * Gets the closed orders, transitions them to completed, and returns the completed orders.
	 *
	 */
    public Collection<OrderDataBean> getClosedOrders(String userID) throws Exception {
        Collection<OrderDataBean> orderDataBeans = new ArrayList<OrderDataBean>();
        Connection conn = null;
        try 
        {
            conn = getConn();
            // As soon as an order has been processed it goes to the closed state. When user open 
            // their account page it notifies the user of orders that are in the closed state and
            // transitions them to the completed state. To do that it calls this method. It reads
            // the closed orders, transitions them to the completed state, and returns the list 
            // of completed orders.
            PreparedStatement stmt = getStatement(conn, getClosedOrdersSQL);
            stmt.setString(1, userID);

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                OrderDataBean orderData = getOrderDataFromResultSet(rs);
                orderData.setOrderStatus("completed");
                updateOrderStatus(conn, orderData.getOrderID(), orderData.getOrderStatus());
                orderDataBeans.add(orderData);
            }

            stmt.close();
            commit(conn);
        } catch (Exception e) {
            rollBack(conn, e);
            throw e;
        } finally {
            releaseConn(conn);
        }
        return orderDataBeans;
    }

	/**
	 * 
	 * @see TradeServices#getHoldings(String)
	 *
	 */
    public Collection<HoldingDataBean> getHoldings(String userID) throws Exception {
        Collection<HoldingDataBean> holdingDataBeans = new ArrayList<HoldingDataBean>();
        Connection conn = null;
        try 
        {
            conn = getConn();
            PreparedStatement stmt = getStatement(conn, getHoldingsForUserSQL);
            stmt.setString(1, userID);

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                HoldingDataBean holdingData = getHoldingDataFromResultSet(rs);
                holdingDataBeans.add(holdingData);
            }

            stmt.close();
            commit(conn);

        } catch (Exception e) {
            rollBack(conn, e);
            throw e;
        } finally {
            releaseConn(conn);
        }
        return holdingDataBeans;
    }    
	
	// Private helper functions    
    
    private AccountDataBean register(Connection conn, AccountDataBean accountData) throws Exception 
    {
        PreparedStatement stmt = getStatement(conn, createAccountSQL);

        String userID = accountData.getProfileID();
        Integer accountID = accountData.getAccountID();
        BigDecimal balance =  accountData.getBalance();
        BigDecimal openBalance =  accountData.getOpenBalance();

        stmt.setInt(1, accountID.intValue());
        stmt.setBigDecimal(2, openBalance);
        stmt.setBigDecimal(3, balance);
        stmt.setString(4, userID);
        stmt.executeUpdate();
        stmt.close();
        
        return accountData;
    }
    
    private void creditAccountBalance(Connection conn, AccountDataBean accountData, BigDecimal credit) throws Exception {
        PreparedStatement stmt = getStatement(conn, creditAccountBalanceSQL);

        stmt.setBigDecimal(1, credit);
        stmt.setInt(2, accountData.getAccountID().intValue());

        stmt.executeUpdate();
        stmt.close();

    }

	/**
	 * 
	 * @see TradeServices#buy(String, String, double, int)
	 *
	 */
    private OrderDataBean buy(Connection conn, 
    		AccountDataBean accountData, OrderDataBean orderData, QuoteDataBean quoteData, Integer mode) throws Exception 
	{
    	// Mode is ignored for now. Later it will be used for async order processing

        /*
         * total = (quantity * purchasePrice) + orderFee
         */
        BigDecimal total;
            
        HoldingDataBean holdingData = null; // the completeOrder operation will create the holding and associate it with the order

        orderData = createOrder(conn, accountData, quoteData, holdingData, "buy", orderData.getQuantity());

        // Update -- account should be credited during completeOrder
        BigDecimal price = quoteData.getPrice();
        BigDecimal orderFee = orderData.getOrderFee();
        total = (new BigDecimal(orderData.getQuantity()).multiply(price)).add(orderFee);

        // subtract total from account balance
        creditAccountBalance(conn, accountData, total.negate());

        completeOrder(conn, orderData.getOrderID());

        orderData = getOrderData(conn, orderData.getOrderID().intValue());

        return orderData;
    }
    
    private OrderDataBean completeOrder(Connection conn, Integer orderID) throws Exception 
    {
        OrderDataBean orderData = null;

        PreparedStatement stmt = getStatement(conn, getOrderSQL);
        stmt.setInt(1, orderID.intValue());

        ResultSet rs = stmt.executeQuery();

        if (!rs.next()) {
            rs.close();
            stmt.close();
            throw new NotFoundException("Unable to find order with ID: " + orderID);
        }
        orderData = getOrderDataFromResultSet(rs);

        String orderType = orderData.getOrderType();
        String orderStatus = orderData.getOrderStatus();

        // if (order.isCompleted())
        if ((orderStatus.compareToIgnoreCase("completed") == 0)
            || (orderStatus.compareToIgnoreCase("alertcompleted") == 0)
            || (orderStatus.compareToIgnoreCase("cancelled") == 0))
            throw new ClientErrorException("Attempt to complete an order that is already complete", Response.Status.CONFLICT);

        int accountID = rs.getInt("account_accountID");
        String quoteID = rs.getString("quote_symbol");
        int holdingID = rs.getInt("holding_holdingID");

        BigDecimal price = orderData.getPrice();
        double quantity = orderData.getQuantity();

        // get the data for the account and quote
        // the holding will be created for a buy or extracted for a sell

        /*
         * Use the AccountID and Quote Symbol from the Order AccountDataBean accountData = getAccountData(accountID,
         * conn); QuoteDataBean quoteData = getQuoteData(conn, quoteID);
         */
        
        String userID = getAccountProfileData(conn, new Integer(accountID)).getUserID();

        HoldingDataBean holdingData = null;

//        Log.debug("PortfoliosService:completeOrder()--> Completing Order " + orderData.getOrderID() + "\n\t Order info: "
//                + orderData + "\n\t Account info: " + accountID + "\n\t Quote info: " + quoteID);

        // if (order.isBuy())
        if (orderType.compareToIgnoreCase("buy") == 0) {
            /*
             * Complete a Buy operation - create a new Holding for the Account - deduct the Order cost from the Account
             * balance
             */

            holdingData = createHolding(conn, accountID, quoteID, quantity, price);
            updateOrderHolding(conn, orderID.intValue(), holdingData.getHoldingID().intValue());
        }

        // if (order.isSell()) {
        if (orderType.compareToIgnoreCase("sell") == 0) {
            /*
             * Complete a Sell operation - remove the Holding from the Account - deposit the Order proceeds to the
             * Account balance
             */
            holdingData = getHoldingData(conn, holdingID);
            if (holdingData == null)
                Log.debug("PortfoliosService:completeOrder:sell -- user: " + userID + " already sold holding: " + holdingID);
            else
                removeHolding(conn, holdingID, orderID.intValue());

        }

        orderData.setOrderStatus("closed");
        updateOrderStatus(conn, orderData.getOrderID(), orderData.getOrderStatus() );

//        Log.debug("PortfoliosService:completeOrder()--> Completed Order " + orderData.getOrderID() + "\n\t Order info: "
//                + orderData + "\n\t Account info: " + accountID + "\n\t Quote info: " + quoteID + "\n\t Holding info: "
//                + holdingData);

        rs.close();
        stmt.close();

// public (buy/sell) method responsible for transaction demarcation
//        commit(conn);

        // signify this order for user userID is complete
        /*TradeAction tradeAction = new TradeAction(this);
        tradeAction.orderCompleted(userID, orderID);*/

        return orderData;
    }

    private AccountProfileDataBean getAccountProfileData(Connection conn, Integer accountID) throws Exception {
        PreparedStatement stmt = getStatement(conn, getAccountProfileForAccountSQL);
        stmt.setInt(1, accountID.intValue());

        ResultSet rs = stmt.executeQuery();

        AccountProfileDataBean accountProfileData = getAccountProfileDataFromResultSet(rs);
        stmt.close();
        return accountProfileData;
    }

    // changed this code to get the account profile from the portfolios table (actually it is called account)
    // in this microservice instead of having to make a remote call to the accounts which would vialote best practices
    // for hirarchical call tree. kept this method to isolate the rest of this microservice from changes.
    private AccountProfileDataBean getAccountProfileDataFromResultSet(ResultSet rs) throws Exception {
        AccountProfileDataBean profileData = null;

        if (!rs.next())
        	Log.debug("PortfoliosService:getAccountProfileData() - cannot find profile; result set is empty");
        else
        {
        	// code change to populate the account profile with the data required by this microservice
            profileData = new AccountProfileDataBean();
            profileData.setUserID(rs.getString("profile_userID"));
        }
        return profileData;
    }
    
    private AccountDataBean getAccountData(Connection conn, String userID) throws Exception {
        PreparedStatement stmt = getStatement(conn, getAccountForUserSQL);
        stmt.setString(1, userID);
        ResultSet rs = stmt.executeQuery();
        AccountDataBean accountData = getAccountDataFromResultSet(rs);
        stmt.close();
        return accountData;
    }

    // this is a shim. it users the portfolios table (account) in this microservice
    // to map the user name to its corresponring account, and constructs an account data beans
    // that includes the account related values from the portfolio. These values are those
    // used by the business logic in this microservice. So it insulates the remaionder of this 
    // microservice from the accounts microservcie
    private AccountDataBean getAccountDataFromResultSet(ResultSet rs) throws Exception {
        AccountDataBean accountData = null;

        if (!rs.next())
            Log.debug("TradeJDBCDirect:getAccountDataFromResultSet -- cannot find account; result set is empty");
        else
        {
        	// code change to only get those fields that we store in the portfoloios microservice
        	// code change to populate the account profile with the data required by this microservice
            accountData = new AccountDataBean();
            accountData.setProfileID(rs.getString("profile_userID"));
            accountData.setAccountID(new Integer(rs.getInt("accountID")));
            accountData.setBalance(rs.getBigDecimal("balance"));
            accountData.setOpenBalance(rs.getBigDecimal("openBalance"));
        }
        return accountData;
    }

    private HoldingDataBean createHolding(Connection conn, int accountID, String symbol, double quantity,
        BigDecimal purchasePrice) throws Exception {

        Timestamp purchaseDate = new Timestamp(System.currentTimeMillis());
        PreparedStatement stmt = getStatement(conn, createHoldingSQL);

        Integer holdingID = KeySequenceDirect.getNextID("holding");
        stmt.setInt(1, holdingID.intValue());
        stmt.setTimestamp(2, purchaseDate);
        stmt.setBigDecimal(3, purchasePrice);
        stmt.setDouble(4, quantity);
        stmt.setString(5, symbol);
        stmt.setInt(6, accountID);
        stmt.executeUpdate();

        stmt.close();

        return getHoldingData(conn, holdingID.intValue());
    }

    private void removeHolding(Connection conn, int holdingID, int orderID) throws Exception {
        PreparedStatement stmt = getStatement(conn, removeHoldingSQL);

        stmt.setInt(1, holdingID);
        stmt.executeUpdate();
        stmt.close();

        // set the HoldingID to NULL for the purchase and sell order now that
        // the holding as been removed
        stmt = getStatement(conn, removeHoldingFromOrderSQL);

        stmt.setInt(1, holdingID);
        stmt.executeUpdate();
        stmt.close();

    }

    private OrderDataBean createOrder(Connection conn, AccountDataBean accountData, QuoteDataBean quoteData,
        HoldingDataBean holdingData, String orderType, double quantity) throws Exception {

        Timestamp currentDate = new Timestamp(System.currentTimeMillis());

        PreparedStatement stmt = getStatement(conn, createOrderSQL);

        Integer orderID = KeySequenceDirect.getNextID("order");
        stmt.setInt(1, orderID.intValue());
        stmt.setString(2, orderType);
        stmt.setString(3, "open");
        stmt.setTimestamp(4, currentDate);
        stmt.setDouble(5, quantity);
        stmt.setBigDecimal(6, quoteData.getPrice().setScale(FinancialUtils.SCALE, FinancialUtils.ROUND));
        stmt.setBigDecimal(7, TradeConfig.getOrderFee(orderType));
        stmt.setInt(8, accountData.getAccountID().intValue());
        if (holdingData == null)
            stmt.setNull(9, java.sql.Types.INTEGER);
        else
            stmt.setInt(9, holdingData.getHoldingID().intValue());
        stmt.setString(10, quoteData.getSymbol());
        stmt.executeUpdate();

        stmt.close();

        return getOrderData(conn, orderID.intValue());
    }

    private HoldingDataBean getHoldingData(Connection conn, int holdingID) throws Exception {
        HoldingDataBean holdingData = null;
        
        PreparedStatement stmt = getStatement(conn, getHoldingSQL);
        stmt.setInt(1, holdingID);
        
        ResultSet rs = stmt.executeQuery();
        
        if (!rs.next())
            Log.debug("PortfoliosService:getHoldingData() - unable to find holding with ID " + holdingID);
        else
            holdingData = getHoldingDataFromResultSet(rs);

        rs.close();
        stmt.close();
        return holdingData;
    }

    private OrderDataBean getOrderData(Connection conn, int orderID) throws Exception 
    {
        OrderDataBean orderData = null;
        
        PreparedStatement stmt = getStatement(conn, getOrderSQL);
        stmt.setInt(1, orderID);
        ResultSet rs = stmt.executeQuery();
        
        if (!rs.next())
            Log.debug("PortfoliosService:getOrderData() - unable to find order with ID " + orderID);
        else
            orderData = getOrderDataFromResultSet(rs);
        
        rs.close();
        stmt.close();
        
        return orderData;
    }

    // Set Timestamp to zero to denote sell is inflight
    // UPDATE -- could add a "status" attribute to holding
    private void updateHoldingStatus(Connection conn, Integer holdingID, String symbol) throws Exception {
        Timestamp ts = new Timestamp(0);
        PreparedStatement stmt = getStatement(conn, "update holdingejb set purchasedate= ? where holdingid = ?");

        stmt.setTimestamp(1, ts);
        stmt.setInt(2, holdingID.intValue());
        stmt.executeUpdate();
        stmt.close();
    }

    private void updateOrderStatus(Connection conn, Integer orderID, String status) throws Exception {
        PreparedStatement stmt = getStatement(conn, updateOrderStatusSQL);

        stmt.setString(1, status);
        stmt.setTimestamp(2, new Timestamp(System.currentTimeMillis()));
        stmt.setInt(3, orderID.intValue());
        stmt.executeUpdate();
        stmt.close();
    }

    private void updateOrderHolding(Connection conn, int orderID, int holdingID) throws Exception {
        PreparedStatement stmt = getStatement(conn, updateOrderHoldingSQL);

        stmt.setInt(1, holdingID);
        stmt.setInt(2, orderID);
        stmt.executeUpdate();
        stmt.close();
    }

    private HoldingDataBean getHoldingDataFromResultSet(ResultSet rs) throws Exception {
        HoldingDataBean holdingData = null;

        holdingData =
            new HoldingDataBean(new Integer(rs.getInt("holdingID")), rs.getDouble("quantity"), rs
                .getBigDecimal("purchasePrice"), rs.getTimestamp("purchaseDate"), rs.getString("quote_symbol"));
        return holdingData;
    }
    
    private OrderDataBean getOrderDataFromResultSet(ResultSet rs) throws Exception {
    	
        OrderDataBean orderData = null;
        orderData =
            new OrderDataBean(new Integer(rs.getInt("orderID")), rs.getString("orderType"),
                rs.getString("orderStatus"), rs.getTimestamp("openDate"), rs.getTimestamp("completionDate"), rs
                    .getDouble("quantity"), rs.getBigDecimal("price"), rs.getBigDecimal("orderFee"), rs
                    .getString("quote_symbol")); 
        orderData.setAccountID(new Integer(rs.getInt("account_accountid")));
        return orderData;
    }

    // Common database utilities
    
    private String checkDBProductName() throws Exception 
	{
        Connection conn = null;
        String dbProductName = null;

        try {
            conn = getConn();
            DatabaseMetaData dbmd = conn.getMetaData();
            dbProductName = dbmd.getDatabaseProductName();
            commit(conn);
        } catch (SQLException e) {
   			rollBack(conn, e);
   			throw e;
        } finally {
            releaseConn(conn);
        }
        return dbProductName;
    }
    
    private Object[] parseDDLToBuffer(InputStream ddlFile) throws Exception 
    {
        BufferedReader br = null;
        Collection<String> sqlBuffer = new ArrayList<String>(30); // initial capacity 30 assuming we have 30 ddl-sql statements to read
        try 
        {
            br = new BufferedReader(new InputStreamReader(ddlFile));
            String s;
            String sql = new String();
            while ((s = br.readLine()) != null) 
            {
                s = s.trim();
                if ((s.length() != 0) && (s.charAt(0) != '#')) // Empty lines or lines starting with "#" are ignored
                {
                    sql = sql + " " + s;
                    if (s.endsWith(";")) // reached end of sql statement
                    {
                        sql = sql.replace(';', ' '); // remove the semicolon
                        // System.out.println (sql);
                        sqlBuffer.add(sql);
                       sql = "";
                    }
                }
            }
        } 
        catch (IOException ex) 
        {
            throw ex;
        } 
        finally 
        {
            if (br != null) 
            {
                try 
                {
                    br.close();
                } 
                catch (Throwable t) 
                {
             		StringWriter sw = new StringWriter();
               		t.printStackTrace(new PrintWriter(sw));
               		String exceptionAsString = sw.toString();
                	Log.debug("QuotesService:parseDDLToBuffer() - Ignored exception closing a buffer reader:\n" + exceptionAsString);
                }
            }
        }
        return sqlBuffer.toArray();
    }

    /*
     * Lookup the TradeData datasource
     */
    private void getDataSource() throws Exception {
        datasource = (DataSource) context.lookup(dsName);
    }

    /*
     * Allocate a new connection to the datasource
     */

    private Connection getConn() throws Exception 
    {
        if (datasource == null) getDataSource();

        Connection conn = datasource.getConnection();
        conn.setAutoCommit(false);

        return conn;
    }

    /*
     * Commit the provided connection if not null
     */
    private void commit(Connection conn) throws Exception {
        if (conn != null) conn.commit();
    }

    /*
     * Rollback the statement for the given connection
     */
    private void rollBack(Connection conn, Exception e) throws Exception 
    {
        if (conn != null) conn.rollback();  
    }

    private void releaseConn(Connection conn) throws Exception 
    {        
        if (conn != null)
        {
        	try 
        	{
        		conn.close();
        	} 
        	catch (Throwable t) 
        	{
         		StringWriter sw = new StringWriter();
         		t.printStackTrace(new PrintWriter(sw));
         		String exceptionAsString = sw.toString();
        		Log.debug("PortfoliosService:releaseConn() - Ignored exception closing a connection: \n" + exceptionAsString);
        	}
        } 
    }
    
    /*
     * Allocate a new prepared statment for this connection
     */
    private PreparedStatement getStatement(Connection conn, String sql) throws Exception {
        return conn.prepareStatement(sql);
    }
    
	//		-  Kept the sql statements for the portfolios microservices; removed the others
  
// Changed this query to get the portfolio (it is then used to construct profile data)
//    private final static String getAccountProfileForAccountSQL =
//            "select * from accountprofileejb ap where ap.userid = "
//                + "(select profile_userid from accountejb a where a.accountid=?)";
    private final static String getAccountProfileForAccountSQL =
            "select * from accountejb a where a.accountid=?";
    
// Refactored this SQL it worked but was ereally incorrectly coded.
// Note that account already has a userid so there was no reason to have an
// inner join. The sql should have been written like the following to start.
// If it had been coded correectly there would have been no changes. But in
// this case we had to change it.
//    private static final String getAccountForUserSQL =
//            "select * from accountejb a where a.profile_userid = "
//                + "( select userid from accountprofileejb ap where ap.userid = ?)";
    private static final String getAccountForUserSQL =
            "select * from accountejb where profile_userid = ?";
    
    private static final String createHoldingSQL =
        "insert into holdingejb "
            + "( holdingid, purchaseDate, purchasePrice, quantity, quote_symbol, account_accountid ) "
            + "VALUES (  ?  ,  ?  ,  ?  ,  ?  ,  ?  ,  ? )";

    private static final String createOrderSQL =
        "insert into orderejb "
            + "( orderid, ordertype, orderstatus, opendate, quantity, price, orderfee, account_accountid,  holding_holdingid, quote_symbol) "
            + "VALUES (  ?  ,  ?  ,  ?  ,  ?  ,  ?  ,  ?  ,  ?  , ? , ? , ?)";

    private static final String removeHoldingSQL = "delete from holdingejb where holdingid = ?";

    private static final String removeHoldingFromOrderSQL =
        "update orderejb set holding_holdingid=null where holding_holdingid = ?";

    private static final String getHoldingSQL = "select * from holdingejb h where h.holdingid = ?";
    
	// 	updated on 2018-06-20
	//		- Refactored  this inner join to use the portfolios talbe instead of the accounts
    //        Note that portfolios table is inside this microservice while the accounts table
    //        is in a separate microservice. So that SQL would not work.
    //
    //        Instead of using the inner join another option is to remove the inner join from
    //        this SQL statement; add a SQL statement query to get the portfolo by its userid;
    //        change the code to get the userid from the portfolios then execute this query 
    //        (without the innerjoin)
    //
    //		  We selected the first option as it eliminates the need for a second database call
    //
    //        No changes required
    private static final String getHoldingsForUserSQL =
            "select * from holdingejb h where h.account_accountid = "
                    + "(select a.accountid from accountejb a where a.profile_userid = ?)";

    private static final String getOrderSQL = "select * from orderejb o where o.orderid = ?";

	// 	updated on 2018-06-20
	//		- Refactored  this inner join to use the portfolios talbe instead of the accounts
    //        Note that portfolios table is inside this microservice while the accounts table
    //        is in a separate microservice. So that SQL would not work.
    //
    //        Instead of using the inner join another option is to remove the inner join from
    //        this SQL statement; add a SQL statement query to get the portfolo by its userid;
    //        change the code to get the userid from the portfolios tabpe then execute this 
    //        query (wihtout the innerjoin) 
    //
    //		  We selected the first option as it eliminates the need for a second database call
    //
    //        No changes required
    private static final String getOrdersByUserSQL =
            "select * from orderejb o where o.account_accountid = "
                + "(select a.accountid from accountejb a where a.profile_userid = ?)";

	// 	updated on 2018-06-20
	//		- Moved this SQL from the accounts into the portfolios where it is used (high cohesion)
    private static final String getClosedOrdersSQL =
            "select * from orderejb o " + "where o.orderstatus = 'closed' AND o.account_accountid = "
                    + "(select a.accountid from accountejb a where a.profile_userid = ?)";

// removed unused SQL
//    private static final String getAllOrdersSQL =
//            "select * from orderejb o " + "where o.account_accountid = "
//                + "(select a.accountid from accountejb a where a.profile_userid = ?)";
    
	// 	updated on 2018-06-20
	//		- Moved this SQL from the accounts into the portfolios where it is used (high cohesion)
    private static final String createAccountSQL =
            "insert into accountejb "
                + "( accountid, openBalance, balance, profile_userid) "
                + "VALUES (  ?  ,  ?  ,  ?  ,  ?  )";
    
    private static final String updateOrderStatusSQL =
        "update orderejb set " + "orderstatus = ?, completiondate = ? " + "where orderid = ?";

    private static final String updateOrderHoldingSQL =
        "update orderejb set " + "holding_holdingID = ? " + "where orderid = ?";
    
    private static final String creditAccountBalanceSQL =
            "update accountejb set " + "balance = balance + ? " + "where accountid = ?";

    private static boolean initialized = false;
    
    public static synchronized void init() 
    {
        if (initialized) return;

        try {
            context = new InitialContext();
            datasource = (DataSource) context.lookup(dsName);
        } catch (Exception e) {
        	Log.error("PortfoliosService:init() - error on JNDI lookup of " + dsName + " -- PortfoliosService will not work",e);
            return;
        }
        TradeConfig.setPublishQuotePriceChange(false);
        initialized = true;
    }

    public static void destroy() {
        return;
    }

}
