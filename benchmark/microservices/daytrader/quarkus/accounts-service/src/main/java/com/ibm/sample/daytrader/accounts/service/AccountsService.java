package com.ibm.sample.daytrader.accounts.service;

import com.ibm.sample.daytrader.accounts.beans.RunStatsDataBean;
import com.ibm.sample.daytrader.accounts.direct.KeySequenceDirect;
import com.ibm.sample.daytrader.accounts.entities.AccountDataBean;
import com.ibm.sample.daytrader.accounts.entities.AccountProfileDataBean;
import com.ibm.sample.daytrader.accounts.utils.Log;
import com.ibm.sample.daytrader.accounts.utils.TradeConfig;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.NotAuthorizedException;
import jakarta.ws.rs.InternalServerErrorException;

import javax.sql.DataSource;
import java.io.*;
import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.Collection;

@ApplicationScoped
public class AccountsService {

    @Inject
    DataSource datasource;

    @Inject
    PortfoliosRemoteCallService portfoliosService;

    private volatile boolean initialized = false;

    private synchronized void ensureInit() {
        if (!initialized) {
            try {
                KeySequenceDirect.initialize(datasource.getConnection());
                TradeConfig.setPublishQuotePriceChange(false);
                initialized = true;
            } catch (Exception e) {
                Log.error("AccountsService:init() - error initializing datasource", e);
            }
        }
    }

    public Boolean tradeBuildDB(int limit, int offset) throws Exception {
        ensureInit();
        if (offset == 0) resetTrade(true);

        Connection conn = null;
        try {
            conn = getConn();
            for (int i = 0; i < limit; i++) {
                int accountID = i + offset;
                String userID = "uid:" + accountID;
                String fullname = TradeConfig.rndFullName();
                String email = TradeConfig.rndEmail(userID);
                String address = TradeConfig.rndAddress();
                String creditcard = TradeConfig.rndCreditCard();
                BigDecimal initialBalance;
                if (accountID == 0) {
                    initialBalance = new BigDecimal(1000000);
                } else {
                    initialBalance = new BigDecimal(TradeConfig.rndInt(100000) + 200000);
                }
                register(conn, userID, "xxx", fullname, address, email, creditcard, initialBalance);
            }
            commit(conn);
        } catch (Exception e) {
            rollBack(conn, e);
            throw e;
        } finally {
            releaseConn(conn);
        }
        return true;
    }

    public RunStatsDataBean resetTrade(boolean deleteAll) throws Exception {
        ensureInit();
        RunStatsDataBean runStatsData = new RunStatsDataBean();
        Connection conn = null;

        if (deleteAll) {
            conn = getConn();
            PreparedStatement stmt = null;
            try {
                stmt = getStatement(conn, "delete from accountejb");
                stmt.executeUpdate();
                stmt.close();
                stmt = getStatement(conn, "delete from accountprofileejb");
                stmt.executeUpdate();
                stmt.close();
                stmt = getStatement(conn, "delete from keygenejb");
                stmt.executeUpdate();
                stmt.close();
                commit(conn);
                KeySequenceDirect.initialize(datasource.getConnection());
            } catch (Exception e) {
                rollBack(conn, e);
                throw e;
            } finally {
                releaseConn(conn);
            }
            return runStatsData;
        } else {
            conn = getConn();
            PreparedStatement stmt = null;
            ResultSet rs = null;
            try {
                stmt = getStatement(conn, "delete from accountprofileejb where userid like 'ru:%'");
                stmt.executeUpdate();
                stmt.close();

                stmt = getStatement(conn, "delete from accountejb where profile_userid like 'ru:%'");
                int newUserCount = stmt.executeUpdate();
                runStatsData.setNewUserCount(newUserCount);
                stmt.close();

                stmt = getStatement(conn, "select count(accountid) as \"tradeUserCount\" from accountejb a where a.profile_userid like 'uid:%'");
                rs = stmt.executeQuery();
                rs.next();
                int tradeUserCount = rs.getInt("tradeUserCount");
                runStatsData.setTradeUserCount(tradeUserCount);
                stmt.close();

                stmt = getStatement(conn, "select sum(loginCount) as \"sumLoginCount\", sum(logoutCount) as \"sumLogoutCount\" from accountejb a where a.profile_userID like 'uid:%'");
                rs = stmt.executeQuery();
                rs.next();
                int sumLoginCount = rs.getInt("sumLoginCount");
                int sumLogoutCount = rs.getInt("sumLogoutCount");
                runStatsData.setSumLoginCount(sumLoginCount);
                runStatsData.setSumLogoutCount(sumLogoutCount);
                stmt.close();
                rs.close();

                stmt = getStatement(conn, "update accountejb set logoutCount=0,loginCount=0 where profile_userID like 'uid:%'");
                stmt.executeUpdate();
                stmt.close();

                commit(conn);
            } catch (Exception e) {
                rollBack(conn, e);
                throw e;
            } finally {
                releaseConn(conn);
            }
            return runStatsData;
        }
    }

    public boolean recreateDBTables() throws Exception {
        ensureInit();
        Object[] sqlBuffer = null;
        String ddlFile = null;
        String dbProductName = checkDBProductName();

        if (dbProductName.startsWith("Apache Derby")) {
            ddlFile = "/dbscripts/derby/AccountsTable.ddl";
        } else {
            ddlFile = "/dbscripts/derby/AccountsTable.ddl";
            Log.debug("AccountsService:recreateDBTables() - " + dbProductName + " defaulting to derby DDL");
        }

        sqlBuffer = parseDDLToBuffer(this.getClass().getResourceAsStream(ddlFile));
        if ((sqlBuffer == null) || (sqlBuffer.length == 0)) {
            throw new InternalServerErrorException("DDL file " + ddlFile + " is empty");
        }

        Connection conn = null;
        try {
            conn = getConn();
            Statement stmt = conn.createStatement();
            int bufferLength = sqlBuffer.length;
            for (int i = 0; i < bufferLength; i++) {
                try {
                    stmt.executeUpdate((String) sqlBuffer[i]);
                } catch (SQLException ex) {
                    if (((String) sqlBuffer[i]).indexOf("DROP TABLE") > 0) {
                        // Ignore exception; table may not exist
                    } else {
                        throw ex;
                    }
                }
            }
            stmt.close();
            commit(conn);
        } catch (Exception e) {
            rollBack(conn, e);
            throw e;
        } finally {
            releaseConn(conn);
        }

        KeySequenceDirect.initialize(datasource.getConnection());
        return true;
    }

    public AccountDataBean getAccountData(String userID) throws Exception {
        ensureInit();
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

    public AccountProfileDataBean getAccountProfileData(String userID) throws Exception {
        ensureInit();
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

    public AccountProfileDataBean updateAccountProfile(AccountProfileDataBean profileData) throws Exception {
        ensureInit();
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

    public AccountDataBean login(String userID, String password) throws Exception {
        ensureInit();
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
            if ((pw == null) || (!pw.equals(password))) {
                throw new NotAuthorizedException("Incorrect password for user: " + userID);
            }
            stmt = getStatement(conn, loginSQL);
            stmt.setTimestamp(1, new Timestamp(System.currentTimeMillis()));
            stmt.setString(2, userID);
            stmt.executeUpdate();
            stmt.close();

            stmt = getStatement(conn, getAccountForUserSQL);
            stmt.setString(1, userID);
            rs = stmt.executeQuery();
            if (!rs.next()) {
                throw new NotAuthorizedException("Failure to find account for user: " + userID);
            } else {
                accountData = getAccountDataFromResultSet(rs);
            }
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

    public boolean logout(String userID) throws Exception {
        ensureInit();
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

    public AccountDataBean register(String userID, String password, String fullname,
            String address, String email, String creditCard, BigDecimal openBalance) throws Exception {
        ensureInit();
        AccountDataBean accountData = null;
        Connection conn = null;
        try {
            conn = getConn();
            accountData = register(conn, userID, password, fullname, address, email, creditCard, openBalance);
            accountData = portfoliosService.register(accountData);
            commit(conn);
        } catch (Exception e) {
            rollBack(conn, e);
            throw e;
        } finally {
            releaseConn(conn);
        }
        return accountData;
    }

    private AccountDataBean register(Connection conn, String userID, String password, String fullname,
            String address, String email, String creditCard, BigDecimal openBalance) throws Exception {
        PreparedStatement stmt = getStatement(conn, createAccountSQL);
        Integer accountID = KeySequenceDirect.getNextID("account");
        BigDecimal balance = openBalance;
        Timestamp creationDate = new Timestamp(System.currentTimeMillis());
        Timestamp lastLogin = creationDate;
        int loginCount = 0;
        int logoutCount = 0;

        stmt.setInt(1, accountID.intValue());
        stmt.setTimestamp(2, creationDate);
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
        if (!rs.next()) {
            Log.debug("AccountsService:getAccountData() - cannot find account for user: " + userID);
        } else {
            accountData = getAccountDataFromResultSet(rs);
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
        AccountProfileDataBean accountProfileData = null;
        if (!rs.next()) {
            Log.debug("AccountsService:getAccountProfileData() - cannot find profile for user: " + userID);
        } else {
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
        return new AccountDataBean(
                Integer.valueOf(rs.getInt("accountID")),
                rs.getInt("loginCount"),
                rs.getInt("logoutCount"),
                rs.getTimestamp("lastLogin"),
                rs.getTimestamp("creationDate"),
                null, null,
                rs.getString("profile_userID"));
    }

    private AccountProfileDataBean getAccountProfileDataFromResultSet(ResultSet rs) throws Exception {
        return new AccountProfileDataBean(
                rs.getString("userID"),
                rs.getString("passwd"),
                rs.getString("fullName"),
                rs.getString("address"),
                rs.getString("email"),
                rs.getString("creditCard"));
    }

    private String checkDBProductName() throws Exception {
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

    private Object[] parseDDLToBuffer(InputStream ddlFile) throws Exception {
        BufferedReader br = null;
        Collection<String> sqlBuffer = new ArrayList<>(30);
        try {
            br = new BufferedReader(new InputStreamReader(ddlFile));
            String s;
            String sql = "";
            while ((s = br.readLine()) != null) {
                s = s.trim();
                if ((s.length() != 0) && (s.charAt(0) != '#')) {
                    sql = sql + " " + s;
                    if (s.endsWith(";")) {
                        sql = sql.replace(';', ' ');
                        sqlBuffer.add(sql);
                        sql = "";
                    }
                }
            }
        } finally {
            if (br != null) {
                try { br.close(); } catch (Throwable t) { }
            }
        }
        return sqlBuffer.toArray();
    }

    private Connection getConn() throws Exception {
        Connection conn = datasource.getConnection();
        conn.setAutoCommit(false);
        return conn;
    }

    private void commit(Connection conn) throws Exception {
        if (conn != null) conn.commit();
    }

    private void rollBack(Connection conn, Exception e) throws Exception {
        if (conn != null) conn.rollback();
    }

    private void releaseConn(Connection conn) throws Exception {
        if (conn != null) {
            try { conn.close(); } catch (Throwable t) { }
        }
    }

    private PreparedStatement getStatement(Connection conn, String sql) throws Exception {
        return conn.prepareStatement(sql);
    }

    private static final String createAccountSQL =
        "insert into accountejb (accountid, creationDate, lastLogin, loginCount, logoutCount, profile_userid) VALUES (?, ?, ?, ?, ?, ?)";

    private static final String createAccountProfileSQL =
        "insert into accountprofileejb (userid, passwd, fullname, address, email, creditcard) VALUES (?, ?, ?, ?, ?, ?)";

    private static final String updateAccountProfileSQL =
        "update accountprofileejb set passwd = ?, fullname = ?, address = ?, email = ?, creditcard = ? where userid = (select profile_userid from accountejb a where a.profile_userid=?)";

    private static final String loginSQL =
        "update accountejb set lastLogin=?, logincount=logincount+1 where profile_userid=?";

    private static final String logoutSQL =
        "update accountejb set logoutcount=logoutcount+1 where profile_userid=?";

    private static final String getAccountProfileSQL =
        "select * from accountprofileejb ap where ap.userid = (select profile_userid from accountejb a where a.profile_userid=?)";

    private static final String getAccountForUserSQL =
        "select * from accountejb a where a.profile_userid = (select userid from accountprofileejb ap where ap.userid = ?)";
}
