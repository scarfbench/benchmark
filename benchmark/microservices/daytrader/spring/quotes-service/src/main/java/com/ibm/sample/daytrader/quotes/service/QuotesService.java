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

package com.ibm.sample.daytrader.quotes.service;

//- Each microservice has its own log

import com.ibm.sample.daytrader.quotes.core.beans.MarketSummaryDataBean;
import com.ibm.sample.daytrader.quotes.core.beans.RunStatsDataBean;
import com.ibm.sample.daytrader.quotes.core.direct.FinancialUtils;
import com.ibm.sample.daytrader.quotes.entities.QuoteDataBean;
import com.ibm.sample.daytrader.quotes.utils.Log;

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
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.NotFoundException;

import com.ibm.sample.daytrader.quotes.utils.TradeConfig;

import org.springframework.stereotype.Service;

/**
 * A microservice to retrieve real time stock quotes.
 * 
 * @author
 *
 */

@Service
public class QuotesService
{
	//	- Each microservice has their own private database (datasource)
    private static String dsName = TradeConfig.QUOTES_DATASOURCE;

    //	-  @Resource annotation is not supported on static fields
    
    private static DataSource datasource = null;

    private static BigDecimal ZERO = new BigDecimal(0.0);

    private static InitialContext context;

    /**
     * Zero arg constructor for QuotesService
     */
    public QuotesService() {
        if (initialized == false) init();
    }

	/**
	 * 
	 * QuotesService#tradeBuildDB(int,int)
	 *
	 *
	 * Generate sample data is a formal business operation in the Trade application.
	 */
	public Boolean tradeBuildDB(int limit, int offset ) throws Exception
	{
		int index = offset;
        if (index==0) resetTrade(true); // delete any rows from db prior to repopulating
     
        Connection conn = null;
        try 
        {
            conn = getConn();
        	for (int i = 0; i < limit; i++) 
        	{
        	   String symbol = "s:" + index;
        	   String companyName = "S" + index + " Incorporated";
       	       createQuote(symbol, companyName, new java.math.BigDecimal(TradeConfig.rndPrice()));
       	       index++;
        	}
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
	 * @see TradeServices#resetTrade(boolean)
	 *
	 */
	public RunStatsDataBean resetTrade(boolean deleteAll) throws Exception
	{
		//		-  Reset usage statistics for the quotes microservice and only the quotes
		//         microservice. The other microservices will be responsible for resetting
		//         their own usage statistics
			
        RunStatsDataBean runStatsData = new RunStatsDataBean();
        Connection conn = null;

        if (deleteAll) 		
        {
        	// delete the rows and return
            conn = getConn();
            PreparedStatement stmt = null;
            
            try 
            {
                stmt = getStatement(conn, "delete from quoteejb");
                stmt.executeUpdate();
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
            return runStatsData;
        }
        else
        {
        	// calculate usage stats and return
   			conn = getConn();
   			PreparedStatement stmt = null;
   			ResultSet rs = null;
   			
   			try 
   			{
   				// Count of trade stocks
   				stmt =
   					getStatement(conn,
   						"select count(symbol) as \"tradeStockCount\" from quoteejb a where a.symbol like 's:%'");
   				rs = stmt.executeQuery();
   				rs.next();
   				int tradeStockCount = rs.getInt("tradeStockCount");
   				runStatsData.setTradeStockCount(tradeStockCount);
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
        }
        return runStatsData;
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
	   			ddlFile = "/dbscripts/oracle/QuotesTable.ddl";
	   		}
	   		else if (dbProductName.startsWith("Apache Derby")) // if db is Derby
	   		{
	   			ddlFile = "/dbscripts/derby/QuotesTable.ddl";
	   		}
	   		else if (dbProductName.startsWith("Oracle")) // if the Db is Oracle
	   		{
	   			ddlFile = "/dbscripts/oracle/QuotesTable.ddl";
	   		} 
	   		else if (dbProductName.startsWith("MySQL")) // if the Db is MySQL
	   		{
	   			ddlFile = "/dbscripts/mysql/QuotesTable.ddl";
	   		}
	   		else if (dbProductName.startsWith("Informix Dynamic Server")) // if the Db is Informix dynamic server
	   		{
	   			ddlFile = "/dbscripts/informix/QuotesTable.ddl";
	   		}
	   		else if (dbProductName.startsWith("Microsoft SQL Server")) // if the Db is Microsoft SQLServer
	   		{
	   			ddlFile = "/dbscripts/sqlserver/QuotesTable.ddl";
	   		}
	   		else if (dbProductName.startsWith("PostgreSQL")) // if the Db is PostgreSQL
	   		{
	   			ddlFile = "/dbscripts/postgre/QuotesTable.ddl";
	   		} 
	   		else // Unsupported "Other" Database
	   		{
	   			ddlFile = "/dbscripts/other/QuotesTable.ddl";
	   			Log.debug("QuotesService:recreateDBTables() - " + dbProductName + " is unsupported/untested; use it at your own risk");
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
	   			for (int i = 0; i < bufferLength; i++) {
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
	   						throw ex;
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
	   		
	   		return true;
	    }
	     
    /**
     * @see TradeServices#getMarketSummary()
     */
    
    public MarketSummaryDataBean getMarketSummary(String exchange) throws Exception {
    	
    	// Added exchange on 7-10-2018 (03); it is ignored for now.
    	// Later you can select the exchange, e.g. NYSE, NASDAQ, AMEX to get
    	// the top gainers and losers in that exchange

        MarketSummaryDataBean marketSummaryData = null;
        Connection conn = null;
        try {          
            conn = getConn();
            PreparedStatement stmt =
                getStatement(conn, getTSIAQuotesOrderByChangeSQL, ResultSet.TYPE_SCROLL_INSENSITIVE,
                    ResultSet.CONCUR_READ_ONLY);

        	// 	- Collection is a raw type; should be parameterized
            Collection<QuoteDataBean> topGainersData = new ArrayList<QuoteDataBean>(5);
            Collection<QuoteDataBean> topLosersData = new ArrayList<QuoteDataBean>(5);

            ResultSet rs = stmt.executeQuery();

            int count = 0;
            while (rs.next() && (count++ < 5)) {
                QuoteDataBean quoteData = getQuoteDataFromResultSet(rs);
                topLosersData.add(quoteData);
            }

            stmt.close();
            stmt =
                getStatement(conn, "select * from quoteejb q where q.symbol like 's:1__' order by q.change1 DESC",
                    ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            rs = stmt.executeQuery();

            count = 0;
            while (rs.next() && (count++ < 5)) {
                QuoteDataBean quoteData = getQuoteDataFromResultSet(rs);
                topGainersData.add(quoteData);
            }

            stmt.close();

            BigDecimal TSIA = ZERO;
            BigDecimal openTSIA = ZERO;
            double volume = 0.0;

            if ((topGainersData.size() > 0) || (topLosersData.size() > 0)) {

                stmt = getStatement(conn, getTSIASQL);
                rs = stmt.executeQuery();

                if (!rs.next())
                    throw new NotFoundException("Error with getTSIASQL -- no results");
                else
                    TSIA = rs.getBigDecimal("TSIA");
                stmt.close();

                stmt = getStatement(conn, getOpenTSIASQL);
                rs = stmt.executeQuery();

                if (!rs.next())
                    throw new NotFoundException("Error with getOpenTSIASQL -- no results");
                else
                    openTSIA = rs.getBigDecimal("openTSIA");
                stmt.close();

                stmt = getStatement(conn, getTSIATotalVolumeSQL);
                rs = stmt.executeQuery();

                if (!rs.next())
                    throw new NotFoundException("Error with getTSIATotalVolumeSQL -- no results");
                else
                    volume = rs.getDouble("totalVolume");
                stmt.close();
            }
            commit(conn);
            marketSummaryData = new MarketSummaryDataBean(TSIA, openTSIA, volume, topGainersData, topLosersData);
			}

        catch (Exception e) {
            rollBack(conn, e);
            throw e;
        } finally {
            releaseConn(conn);
        }
        return marketSummaryData;

    }
	    
    /**
     * @see TradeServices#createQuote(String, String, BigDecimal)
     */
    public QuoteDataBean createQuote(String symbol, String companyName, BigDecimal price) throws Exception {

        QuoteDataBean quoteData = null;
        Connection conn = null;
        try {
            conn = getConn();
            quoteData = createQuote( conn, symbol, companyName, price );
            commit(conn);
        } catch (Exception e) {
            rollBack(conn, e);            
            throw e;
        } finally {
            releaseConn(conn);
        }
        return quoteData;
    }

    /**
     * @see TradeServices#getQuote(String)
     */
    public QuoteDataBean getQuote(String symbol) throws Exception {
        QuoteDataBean quoteData = null;
        Connection conn = null;

        try {
            conn = getConn();
            quoteData = getQuote(conn, symbol);
            commit(conn);
        } catch (Exception e) {
            rollBack(conn, e);
            throw e;
        } finally {
            releaseConn(conn);
        }
        return quoteData;
    }

    /**
     * @see TradeServices#getAllQuotes(String)
     */
    public Collection<QuoteDataBean> getAllQuotes() throws Exception {
        Collection<QuoteDataBean> quotes = new ArrayList<QuoteDataBean>();
        QuoteDataBean quoteData = null;

        Connection conn = null;
        try {
            conn = getConn();

            PreparedStatement stmt = getStatement(conn, getAllQuotesSQL);

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) { // fixed defect: while(!rs.next)
                quoteData = getQuoteDataFromResultSet(rs);
                quotes.add(quoteData);
            }

            stmt.close();
            commit(conn);
        } catch (Exception e) {
            rollBack(conn, e);
            throw e;
        } finally {
            releaseConn(conn);
        }

        return quotes;
    }
	
	/**
	 * 
	 * @see TradeServices#updateQuotePriceVolumeInt(String,BigDecimal,double)
	 *
	 */	
    public QuoteDataBean updateQuotePriceVolumeInt(String symbol, BigDecimal changeFactor, double sharesTraded) throws Exception {

        if (TradeConfig.getUpdateQuotePrices() == false)
            return new QuoteDataBean();

        QuoteDataBean quoteData = null;
        Connection conn = null;

        try {
            conn = getConn();

            quoteData = getQuoteForUpdate(conn, symbol);
            BigDecimal oldPrice = quoteData.getPrice();
            double newVolume = quoteData.getVolume() + sharesTraded;

            if (oldPrice.equals(TradeConfig.PENNY_STOCK_PRICE)) {
                changeFactor = TradeConfig.PENNY_STOCK_RECOVERY_MIRACLE_MULTIPLIER;
            } else if (oldPrice.compareTo(TradeConfig.MAXIMUM_STOCK_PRICE) > 0) {
                changeFactor = TradeConfig.MAXIMUM_STOCK_SPLIT_MULTIPLIER;
            }

            BigDecimal newPrice = changeFactor.multiply(oldPrice).setScale(2, BigDecimal.ROUND_HALF_UP);

            updateQuotePriceVolume(conn, quoteData.getSymbol(), newPrice, newVolume);
            quoteData = getQuote(conn, symbol);

            commit(conn);

        } catch (Exception e) {
            rollBack(conn, e);
            throw e;
        } finally {
            releaseConn(conn);
        }
        return quoteData;
    }

	// Private helper functions
    
    // added private method so it could be called by any number of top-level business operations
    private QuoteDataBean createQuote(Connection conn, String symbol, String companyName, BigDecimal price) throws Exception 
    {
        price = price.setScale(FinancialUtils.SCALE, FinancialUtils.ROUND);
        double volume = 0.0, change = 0.0;
        
        PreparedStatement stmt = getStatement(conn, createQuoteSQL);
        stmt.setString(1, symbol); // symbol
        stmt.setString(2, companyName); // companyName
        stmt.setDouble(3, volume); // volume
        stmt.setBigDecimal(4, price); // price
        stmt.setBigDecimal(5, price); // open
        stmt.setBigDecimal(6, price); // low
        stmt.setBigDecimal(7, price); // high
        stmt.setDouble(8, change); // change
        stmt.executeUpdate();
        stmt.close();
 
        return new QuoteDataBean(symbol, companyName, volume, price, price, price, price, change);
    }
    
    private QuoteDataBean getQuote(Connection conn, String symbol) throws Exception {
        QuoteDataBean quoteData = null;
        PreparedStatement stmt = getStatement(conn, getQuoteSQL);
        stmt.setString(1, symbol); // symbol

        ResultSet rs = stmt.executeQuery();

        if (!rs.next())
            Log.debug("QuotesService:getQuote() -- quote not found for symbol: " + symbol);
        else
            quoteData = getQuoteDataFromResultSet(rs);

        stmt.close();

        return quoteData;
    }
	
    private QuoteDataBean getQuoteForUpdate(Connection conn, String symbol) throws Exception {
        QuoteDataBean quoteData = null;
        PreparedStatement stmt = getStatement(conn, getQuoteForUpdateSQL);
        stmt.setString(1, symbol); // symbol

        ResultSet rs = stmt.executeQuery();

        if (!rs.next())
            Log.debug("QuotesService:getQuoteForUpdate() -- quote not found for symbol: " + symbol);

        else
            quoteData = getQuoteDataFromResultSet(rs);

        stmt.close();

        return quoteData;
    }

    private void updateQuotePriceVolume(Connection conn, String symbol, BigDecimal newPrice, double newVolume)
        throws Exception {

        PreparedStatement stmt = getStatement(conn, updateQuotePriceVolumeSQL);

        stmt.setBigDecimal(1, newPrice);
        stmt.setBigDecimal(2, newPrice);
        stmt.setDouble(3, newVolume);
        stmt.setString(4, symbol);

        stmt.executeUpdate();
        stmt.close();
    }

    private QuoteDataBean getQuoteDataFromResultSet(ResultSet rs) throws Exception {
        QuoteDataBean quoteData = null;

        quoteData =
            new QuoteDataBean(rs.getString("symbol"), rs.getString("companyName"), rs.getDouble("volume"), rs
                .getBigDecimal("price"), rs.getBigDecimal("open1"), rs.getBigDecimal("low"), rs.getBigDecimal("high"),
                rs.getDouble("change1"));
        return quoteData;
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
        		Log.debug("QuotesService:releaseConn() - Ignored exception closing a connection: \n" + exceptionAsString);
        	}
        } 
    }
    
    /*
     * Allocate a new prepared statment for this connection
     */
    private PreparedStatement getStatement(Connection conn, String sql) throws Exception {
        return conn.prepareStatement(sql);
    }

    private PreparedStatement getStatement(Connection conn, String sql, int type, int concurrency) throws Exception {
        return conn.prepareStatement(sql, type, concurrency);
    }
	
	//		-  Kept the sql statements for the quotes microservices; removed the others

    private static final String createQuoteSQL =
        "insert into quoteejb " + "( symbol, companyName, volume, price, open1, low, high, change1 ) "
            + "VALUES (  ?  ,  ?  ,  ?  ,  ?  ,  ?  ,  ?  ,  ?  ,  ?  )";

    private static final String getQuoteSQL = "select * from quoteejb q where q.symbol=?";

    private static final String getAllQuotesSQL = "select * from quoteejb q";

    private static final String getQuoteForUpdateSQL = "select * from quoteejb q where q.symbol=? For Update";

    private static final String getTSIAQuotesOrderByChangeSQL =
        "select * from quoteejb q " + "where q.symbol like 's:1__' order by q.change1";

    private static final String getTSIASQL =
        "select SUM(price)/count(*) as TSIA from quoteejb q " + "where q.symbol like 's:1__'";

    private static final String getOpenTSIASQL =
        "select SUM(open1)/count(*) as openTSIA from quoteejb q " + "where q.symbol like 's:1__'";

    private static final String getTSIATotalVolumeSQL =
        "select SUM(volume) as totalVolume from quoteejb q " + "where q.symbol like 's:1__'";
 
    //	- The value of the field is never used 
    //    private static final String updateQuoteVolumeSQL =
    //        "update quoteejb set " + "volume = volume + ? " + "where symbol = ?";

    private static final String updateQuotePriceVolumeSQL =
        "update quoteejb set " + "price = ?, change1 = ? - open1, volume = ? " + "where symbol = ?";

    private static boolean initialized = false;
    
    public static synchronized void init() 
    {
        if (initialized) return;

        try {
            context = new InitialContext();
            datasource = (DataSource) context.lookup(dsName);
        } catch (Exception e) {
        	Log.error("QuotesService:init() - error on JNDI lookup of " + dsName + "-- QuotesService will not work",e);
            return;
        }
        TradeConfig.setPublishQuotePriceChange(false);
        initialized = true;
    }

    public static void destroy() {
        return;
    }
    
}
