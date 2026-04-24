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

// - Each microservice has its own log
import com.ibm.sample.daytrader.accounts.beans.RunStatsDataBean;
import com.ibm.sample.daytrader.accounts.direct.KeySequenceDirect;
import com.ibm.sample.daytrader.accounts.entities.AccountDataBean;
import com.ibm.sample.daytrader.accounts.entities.AccountProfileDataBean;
import com.ibm.sample.daytrader.accounts.utils.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.ArrayList;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import javax.sql.DataSource;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;

import javax.naming.InitialContext;

import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.NotAuthorizedException;

import com.ibm.sample.daytrader.accounts.utils.TradeConfig;

import org.springframework.stereotype.Service;


/**
 * A microservice to manage accounts (and profiles).
 *
 */

@Service
public class AccountsService
{
	
    // Accounts has accounts related information and business operations.
	// Portfolios related information and business operations have been  
	// moved to the Portfolios Service.
	
    private static PortfoliosRemoteCallService portfoliosService = new PortfoliosRemoteCallService();
	
	//	- Each microservice has their own private database (datasource)
    private static final String dsName = TradeConfig.ACCOUNTS_DATASOURCE;

    //	-  @Resource annotation is not supported on static fields
    private static DataSource datasource = null;

    private static InitialContext context;

    /**
     * Zero arg constructor for AccountsService
     */
    public AccountsService() {
        if (initialized == false) init();
    }
    
	/**
	 * 
	 * AccountsService#tradeBuildDB(int,int)
	 * 
	 * Generate sample data is a formal business operation in the Trade application.
	 *
	 */
    public Boolean tradeBuildDB(int limit, int offset) throws Exception
    {
    	if (offset == 0) resetTrade(true); // delete any rows from db before re-populating
        
        Connection conn = null;
        try 
        {
            conn = getConn();
            
        	// Moved this code from the web tier into the microservice where it belongs
        	for (int i = 0; i < limit; i++) 
        	{
        		int accountID = i + offset;
        		String userID = "uid:" + accountID;
        	    String fullname = TradeConfig.rndFullName();
        	    String email = TradeConfig.rndEmail(userID);
        	    String address = TradeConfig.rndAddress();
        	    String creditcard = TradeConfig.rndCreditCard();
                BigDecimal initialBalance = null;
                if (accountID == 0) 
                {
                	initialBalance = new BigDecimal(1000000); // uid:0 starts with a cool million.
                }
                else
                {
                    initialBalance = new BigDecimal(TradeConfig.rndInt(100000) + 200000);
                }
                register(conn, userID, "xxx", fullname, address, email, creditcard, initialBalance);
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
				stmt = getStatement(conn, "delete from accountprofileejb");
				stmt.executeUpdate();
				stmt.close();
				// Fixed pkey unique constraint violation
				stmt = getStatement(conn, "delete from keygenejb");
				stmt.executeUpdate();
				stmt.close();
				commit(conn);
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
			// calculate usage stats and return
   			conn = getConn();
   			PreparedStatement stmt = null;
   			ResultSet rs = null;
   			try 
   			{
   				// Count and delete random users (with id that start with "ru:%")
   				stmt = getStatement(conn, "delete from accountprofileejb where userid like 'ru:%'");
   				stmt.executeUpdate();
   				stmt.close();

   				// Count and delete random users (with id that start with "ru:%")
   				// Moved this code into the accounts and portfolios microservices
   				stmt = getStatement(conn, "delete from accountejb where profile_userid like 'ru:%'");
   				int newUserCount = stmt.executeUpdate();
   				runStatsData.setNewUserCount(newUserCount);
   				stmt.close();

   				// Count of trade users
   				stmt =	getStatement(conn,
   					"select count(accountid) as \"tradeUserCount\" from accountejb a where a.profile_userid like 'uid:%'");
   				rs = stmt.executeQuery();
   				rs.next();
   				int tradeUserCount = rs.getInt("tradeUserCount");
   				runStatsData.setTradeUserCount(tradeUserCount);
   				stmt.close();

   				// Count of trade users login, logout
   				stmt = getStatement(conn,
                    "select sum(loginCount) as \"sumLoginCount\", sum(logoutCount) as \"sumLogoutCount\" from accountejb a where  a.profile_userID like 'uid:%'");
   				rs = stmt.executeQuery();
   				rs.next();
   				int sumLoginCount = rs.getInt("sumLoginCount");
   				int sumLogoutCount = rs.getInt("sumLogoutCount");
   				runStatsData.setSumLoginCount(sumLoginCount);
   				runStatsData.setSumLogoutCount(sumLogoutCount);
   				stmt.close();
   				rs.close();

   				// Update logoutcount and loginCount back to zero
   				stmt = getStatement(conn, 
   					"update accountejb set logoutCount=0,loginCount=0 where profile_userID like 'uid:%'");
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
   			ddlFile = "/dbscripts/oracle/AccountsTable.ddl";
   		}
   		else if (dbProductName.startsWith("Apache Derby")) // if db is Derby
   		{
   			ddlFile = "/dbscripts/derby/AccountsTable.ddl";
   		}
   		else if (dbProductName.startsWith("Oracle")) // if the Db is Oracle
   		{
   			ddlFile = "/dbscripts/oracle/AccountsTable.ddl";
   		} 
   		else if (dbProductName.startsWith("MySQL")) // if the Db is MySQL
   		{
   			ddlFile = "/dbscripts/mysql/AccountsTable.ddl";
   		}
   		else if (dbProductName.startsWith("Informix Dynamic Server")) // if the Db is Informix dynamic server
   		{
   			ddlFile = "/dbscripts/informix/AccountsTable.ddl";
   		}
   		else if (dbProductName.startsWith("Microsoft SQL Server")) // if the Db is Microsoft SQLServer
   		{
   			ddlFile = "/dbscripts/sqlserver/AccountsTable.ddl";
   		}
   		else if (dbProductName.startsWith("PostgreSQL")) // if the Db is PostgreSQL
   		{
   			ddlFile = "/dbscripts/postgre/AccountsTable.ddl";
   		} 
   		else // Unsupported "Other" Database
   		{
   			ddlFile = "/dbscripts/other/AccountsTable.ddl";
   			Log.debug("AccountsService:recreateDBTables() - " + dbProductName + " is unsupported/untested; use it at your own risk");
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
		        		// Ignore exception; table may non-exist
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
   		
   		// (Re-)initialize the key generator
		KeySequenceDirect.initialize(getConn());
    	return true;
    }
	
   /**
	*
	* @see TradeServices#getAccountData(String)
	*
	*/
    public AccountDataBean getAccountData(String userID) throws Exception {
        AccountDataBean accountData = null;
        Connection conn = null;
        try {
            conn = getConn();
            accountData = getAccountData(conn, userID);
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
	* @see TradeServices#getAccountProfileData(String)
	*
	*/
    public AccountProfileDataBean getAccountProfileData(String userID) throws Exception {
        AccountProfileDataBean accountProfileData = null;
        Connection conn = null;

        try {
            conn = getConn();
            accountProfileData = getAccountProfileData(conn, userID);
            commit(conn);
        } catch (Exception e) {
            rollBack(conn, e);
            throw e;
        } finally {
            releaseConn(conn);
        }
        return accountProfileData;
    }
	
   /**
	*
	* @see TradeServices#updateAccountProfile(AccountProfileDataBean)
	*
	*/
    public AccountProfileDataBean updateAccountProfile(AccountProfileDataBean profileData) throws Exception {
        AccountProfileDataBean accountProfileData = null;
        Connection conn = null;

        try {
            conn = getConn();
            updateAccountProfile(conn, profileData);

            accountProfileData = getAccountProfileData(conn, profileData.getUserID());
            commit(conn);
        } catch (Exception e) {
            rollBack(conn, e);
            throw e;
        } finally {
            releaseConn(conn);
        }
        return accountProfileData;
    }
	
   /**
	*
	* @see TradeServices#login(String,String)	
	*
	*/
    public AccountDataBean login(String userID, String password) throws Exception {
    	// notes login only needs to return the userid as that is all that is used by web
        AccountDataBean accountData = null;
        Connection conn = null;
        try {
            conn = getConn();
            PreparedStatement stmt = getStatement(conn, getAccountProfileSQL);
            stmt.setString(1, userID);

            ResultSet rs = stmt.executeQuery();
            if (!rs.next()) {
                throw new NotAuthorizedException("Failure to find profile for user: " + userID);
            }

            String pw = rs.getString("passwd");
            stmt.close();
            if ((pw == null) || (pw.equals(password) == false)) {
                throw new NotAuthorizedException("Incorrect password: " +  password + " for user: " + userID);
            }

            stmt = getStatement(conn, loginSQL);
            stmt.setTimestamp(1, new Timestamp(System.currentTimeMillis()));
            stmt.setString(2, userID);

            stmt.executeUpdate();
            // ?assert rows==1?
            stmt.close();

            stmt = getStatement(conn, getAccountForUserSQL);
            stmt.setString(1, userID);
            rs = stmt.executeQuery();

            if (!rs.next()) {
                throw new NotAuthorizedException("Failure to find account for user: " + userID);
            }
            else
            	// note: login only needs to return the userid 
            	// as that is all that is used by web so don't
            	// ask the portfolios for the balance
            	accountData = getAccountDataFromResultSet(rs);

            stmt.close();
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
	* @see TradeServices#logout(String)
	*
	*/
    public boolean logout(String userID) throws Exception 
    { 
    	boolean result = false;
        
        Connection conn = null;       
        try {
            conn = getConn();
            PreparedStatement stmt = getStatement(conn, logoutSQL);
            stmt.setString(1, userID);
            stmt.executeUpdate();
            stmt.close();
            commit(conn);
            result = true;
        } catch (Exception e) {
            rollBack(conn, e);
            throw e;
        } finally {
            releaseConn(conn);
        }
        return result;
    }

   /**
	*
	* @see TradeServices#register(String,String,String,String,String,String,BigDecimal)
	*
	*/
    public AccountDataBean register(String userID, String password, String fullname, 
    		String address, String email, String creditCard, BigDecimal openBalance) throws Exception 
    {  
        AccountDataBean accountData = null;
        Connection conn = null;
        try 
        {
            conn = getConn();
            accountData = register(conn, userID, password, fullname, address, email, creditCard, openBalance);
            
            // Send the portfolios microservice the account data it needs to operate independent of 
            // accounts. Note that this data is read only by the accounts, but read-write by the portfolios. 
            // Thus, we cache them in accounts, and store them persistently in portfolios. 
            accountData = portfoliosService.register(accountData); 
            
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
        return accountData;
    }
 	
	// Private helper functions	
    
    /**
 	*
 	* @see TradeServices#register(String,String,String,String,String,String,BigDecimal)
 	*
 	*/
     private AccountDataBean register(Connection conn, String userID, String password, String fullname, 
    		 String address, String email, String creditCard, BigDecimal openBalance) throws Exception {

    	 // Created this method simply to clean up the public register()
    	 
         PreparedStatement stmt = getStatement(conn, createAccountSQL);
         
         Integer accountID = KeySequenceDirect.getNextID("account");
         
         BigDecimal balance = openBalance;
         Timestamp creationDate = new Timestamp(System.currentTimeMillis());
         Timestamp lastLogin = creationDate;
         int loginCount = 0;
         int logoutCount = 0;

         stmt.setInt(1, accountID.intValue());
         stmt.setTimestamp(2, creationDate);
// Don't persist these fields in the accounts microservice; persist them in portfolios
//             stmt.setBigDecimal(3, openBalance);
//             stmt.setBigDecimal(4, balance);
         stmt.setTimestamp(3, lastLogin);
         stmt.setInt(4, loginCount);
         stmt.setInt(5, logoutCount);
         stmt.setString(6, userID);
         stmt.executeUpdate();
         stmt.close();

         stmt = getStatement(conn, createAccountProfileSQL);
         stmt.setString(1, userID);
         stmt.setString(2, password);
         stmt.setString(3, fullname);
         stmt.setString(4, address);
         stmt.setString(5, email);
         stmt.setString(6, creditCard);
         stmt.executeUpdate();
         stmt.close();
         
         AccountDataBean accountData = new AccountDataBean(accountID, loginCount, logoutCount, lastLogin, creationDate, balance, openBalance, userID);          
         AccountProfileDataBean profileData = new AccountProfileDataBean(userID, password, fullname, address, email, creditCard);         
         accountData.setProfile(profileData);

         return accountData;   
     }
    
    private AccountDataBean getAccountData(Connection conn, String userID) throws Exception {
        PreparedStatement stmt = getStatement(conn, getAccountForUserSQL);
        stmt.setString(1, userID);
        ResultSet rs = stmt.executeQuery();
        AccountDataBean accountData = null;
        if (!rs.next())
        {
        	Log.debug("AccountsService:getAccountData() - cannot find account for user: " + userID);
        }
        else 
        {
        	accountData = getAccountDataFromResultSet(rs);
        	// ask the portfolios for the most recent balance so web app can display it
        	// changed this code to get the balance and open balance from the portfolio
        	AccountDataBean portfolioData = portfoliosService.getAccountData(accountData.getProfileID());
        	accountData.setBalance(portfolioData.getBalance());
        	accountData.setOpenBalance(portfolioData.getOpenBalance());
        }
        stmt.close();
        
        return accountData;
    }

    private AccountProfileDataBean getAccountProfileData(Connection conn, String userID) throws Exception {
        PreparedStatement stmt = getStatement(conn, getAccountProfileSQL);
        stmt.setString(1, userID);

        ResultSet rs = stmt.executeQuery();

        AccountProfileDataBean accountProfileData= null;
        if (!rs.next())
        {
        	Log.debug("AccountsService:getAccountProfileData() - cannot find profile for user: " + userID);
        }
        else
        {
        	accountProfileData = getAccountProfileDataFromResultSet(rs);
        }
        
        stmt.close();
        
        return accountProfileData;
    }

    private void updateAccountProfile(Connection conn, AccountProfileDataBean profileData) throws Exception {
        PreparedStatement stmt = getStatement(conn, updateAccountProfileSQL);

        stmt.setString(1, profileData.getPassword());
        stmt.setString(2, profileData.getFullName());
        stmt.setString(3, profileData.getAddress());
        stmt.setString(4, profileData.getEmail());
        stmt.setString(5, profileData.getCreditCard());
        stmt.setString(6, profileData.getUserID());

        stmt.executeUpdate();
        stmt.close();
    }
		
    private AccountDataBean getAccountDataFromResultSet(ResultSet rs) throws Exception {
        AccountDataBean accountData = null;

        // decided to remove the fields from this table instead of keeping them
        // Although that would have reduced the impact on the code, it would have perhaps
        // caused confusion where accounts may start using those fields directly instead
        // of getting them from portfolios so elected to delete these fields. We did not
        // make changes to the constructor since those fields are still part of account;
        // it made sense to leave them in the constructor which also helped to limit the
        // code changes
        accountData =
            new AccountDataBean(new Integer(rs.getInt("accountID")), rs.getInt("loginCount"), rs
                 .getInt("logoutCount"), rs.getTimestamp("lastLogin"), rs.getTimestamp("creationDate"), 
                 null, null,
                 //rs.getBigDecimal("balance"), rs.getBigDecimal("openBalance"), 
                 rs.getString("profile_userID"));     
        
        return accountData;
    }

    private AccountProfileDataBean getAccountProfileDataFromResultSet(ResultSet rs) throws Exception {
        AccountProfileDataBean accountProfileData = null;

        accountProfileData =
            new AccountProfileDataBean(rs.getString("userID"), rs.getString("passwd"), rs.getString("fullName"), rs
                .getString("address"), rs.getString("email"), rs.getString("creditCard"));

        return accountProfileData;
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
                	Log.debug("AccountsService:parseDDLToBuffer() - Ignored exception closing a buffer reader:\n" + exceptionAsString);
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
        		Log.debug("AccountsService:releaseConn() - Ignored exception closing a connection: \n" + exceptionAsString);
        	}
        } 
    }
    
    /*
     * Allocate a new prepared statement for this connection
     */
	 
    private PreparedStatement getStatement(Connection conn, String sql) throws Exception {
        return conn.prepareStatement(sql);
    }
	
	//		-  Kept the sql statements for the accounts microservices; removed the others

    private static final String createAccountSQL =
        "insert into accountejb "
            + "( accountid, creationDate, "
            //+ " openBalance, balance, " -- removed from accounts db
            + "lastLogin, loginCount, logoutCount, profile_userid) "
            + "VALUES (  ?  ,  ?  ,  "
            // +  ?  ,  ?  ,  -- removed from accounts db
            + "?  ,  ?  ,  ?  ,  ?  )";

    private static final String createAccountProfileSQL =
        "insert into accountprofileejb " + "( userid, passwd, fullname, address, email, creditcard ) "
            + "VALUES (  ?  ,  ?  ,  ?  ,  ?  ,  ?  ,  ?  )";

    private final static String updateAccountProfileSQL =
        "update accountprofileejb set " + "passwd = ?, fullname = ?, address = ?, email = ?, creditcard = ? "
            + "where userid = (select profile_userid from accountejb a " + "where a.profile_userid=?)";

    private final static String loginSQL =
        "update accountejb set lastLogin=?, logincount=logincount+1 " + "where profile_userid=?";

    private static final String logoutSQL =
        "update accountejb set logoutcount=logoutcount+1 " + "where profile_userid=?";

    private final static String getAccountProfileSQL =
        "select * from accountprofileejb ap where ap.userid = "
            + "(select profile_userid from accountejb a where a.profile_userid=?)";
    
    private static final String getAccountForUserSQL =
    	"select * from accountejb a where a.profile_userid = "
    		+ "( select userid from accountprofileejb ap where ap.userid = ?)";

// Moved to portfolios
//    
//    private final static String getAccountProfileForAccountSQL =
//        "select * from accountprofileejb ap where ap.userid = "
//            + "(select profile_userid from accountejb a where a.accountid=?)";
//
//    private static final String creditAccountBalanceSQL =
//        "update accountejb set " + "balance = balance + ? " + "where accountid = ?";

    private static boolean initialized = false;

    public static synchronized void init() 
    {
        if (initialized) return;

        try {
            context = new InitialContext();
            datasource = (DataSource) context.lookup(dsName);
        } catch (Exception e) {
        	Log.error("AccountsService:init() - error on JNDI lookup of " + dsName + " -- AccountsService will not work",e);
            return;
        }
        TradeConfig.setPublishQuotePriceChange(false);
        initialized = true;
    }

    public static void destroy() {
        return;
    }

}
