package com.ibm.sample.daytrader.gateway.entities;

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

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

import com.ibm.sample.daytrader.gateway.utils.Log;
import com.ibm.sample.daytrader.gateway.utils.TradeConfig;

public class AccountDataBean implements Serializable {

    private Integer accountID;              /* accountID */
    private int loginCount;                 /* loginCount */
    private int logoutCount;                /* logoutCount */
    private Date lastLogin;                 /* lastLogin Date */
    private Date creationDate;              /* creationDate */
    private String profileID;               /* userID */
    
// moved these fields from being persisted in the accounts database to the portfolios. 
//           it the portfolios microservice that maniupates these values. for that reason, they 
//           are now stroed in portfolios and cached in the accounts microservice. 
//
// TOOD: consider the database migration implications in the presence of microservices schema changes
    private BigDecimal balance;             /* (re)set whenever accounts created/updated */
    private BigDecimal openBalance;         /* (re)set whenever accounts created/updated */
    
// removed these unused relationships
//    private Collection<OrderDataBean> orders;
//    private Collection<HoldingDataBean> holdings;
    
    private AccountProfileDataBean profile;

    public AccountDataBean() {

    }

    public AccountDataBean(Integer accountID,
            int loginCount,
            int logoutCount,
            Date lastLogin,
            Date creationDate,
            BigDecimal balance,
            BigDecimal openBalance,
            String profileID) {
        setAccountID(accountID);
        setLoginCount(loginCount);
        setLogoutCount(logoutCount);
        setLastLogin(lastLogin);
        setCreationDate(creationDate);
        setProfileID(profileID);
        setBalance(balance); 
        setOpenBalance(openBalance);
    }

//    // deleted unused constructor
//    public AccountDataBean(int loginCount,
//            int logoutCount,
//            Date lastLogin,
//            Date creationDate,
//            BigDecimal balance,
//            BigDecimal openBalance,
//            String profileID) {
//        setLoginCount(loginCount);
//        setLogoutCount(logoutCount);
//        setLastLogin(lastLogin);
//        setCreationDate(creationDate);
//        setBalance(balance);
//        setOpenBalance(openBalance);
//        setProfileID(profileID);
//    }

    // used for testing purposes
    public static AccountDataBean getRandomInstance() {
        return new AccountDataBean(new Integer(TradeConfig.rndInt(100000)), //accountID
                TradeConfig.rndInt(10000), //loginCount
                TradeConfig.rndInt(10000), //logoutCount
                new java.util.Date(), //lastLogin
                new java.util.Date(TradeConfig.rndInt(Integer.MAX_VALUE)), //creationDate
                TradeConfig.rndBigDecimal(1000000.0f), //balance
                TradeConfig.rndBigDecimal(1000000.0f), //openBalance
                TradeConfig.rndUserID() //profileID
        );
    }

    public String toString() {
        return "\n\tAccount Data for account: " + getAccountID()
                + "\n\t\t   loginCount:" + getLoginCount()
                + "\n\t\t  logoutCount:" + getLogoutCount()
                + "\n\t\t    lastLogin:" + getLastLogin()
                + "\n\t\t creationDate:" + getCreationDate()
                + "\n\t\t      balance:" + getBalance()
                + "\n\t\t  openBalance:" + getOpenBalance()
                + "\n\t\t    profileID:" + getProfileID()
                ;
    }

    public String toHTML() {
        return "<BR>Account Data for account: <B>" + getAccountID() + "</B>"
                + "<LI>   loginCount:" + getLoginCount() + "</LI>"
                + "<LI>  logoutCount:" + getLogoutCount() + "</LI>"
                + "<LI>    lastLogin:" + getLastLogin() + "</LI>"
                + "<LI> creationDate:" + getCreationDate() + "</LI>"
                + "<LI>      balance:" + getBalance() + "</LI>"
                + "<LI>  openBalance:" + getOpenBalance() + "</LI>"
                + "<LI>    profileID:" + getProfileID() + "</LI>"
                ;
    }

    public void print() {
        Log.log(this.toString());
    }

    public Integer getAccountID() {
        return accountID;
    }

    public void setAccountID(Integer accountID) {
        this.accountID = accountID;
    }

    public int getLoginCount() {
        return loginCount;
    }

    public void setLoginCount(int loginCount) {
        this.loginCount = loginCount;
    }

    public int getLogoutCount() {
        return logoutCount;
    }

    public void setLogoutCount(int logoutCount) {
        this.logoutCount = logoutCount;
    }

    public Date getLastLogin() {
        return lastLogin;
    }

    public void setLastLogin(Date lastLogin) {
        this.lastLogin = lastLogin;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    public BigDecimal getBalance() {
    	return balance;
    }
    
    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }

    public BigDecimal getOpenBalance() {
        return openBalance;
    }
    
    public void setOpenBalance(BigDecimal openBalance) {
        this.openBalance = openBalance;
    }

    public String getProfileID() {
        return profileID;
    }

    public void setProfileID(String profileID) {
        this.profileID = profileID;
    }
    
    /* Disabled for D185273
     public String getUserID() {
         return getProfileID();
     }
     */

// moved unused fields
//    public Collection<OrderDataBean> getOrders() {
//        return orders;
//    }
//
//    public void setOrders(Collection<OrderDataBean> orders) {
//        this.orders = orders;
//    }
//    
//    public Collection<HoldingDataBean> getHoldings() {
//        return holdings;
//    }
//
//    public void setHoldings(Collection<HoldingDataBean> holdings) {
//        this.holdings = holdings;
//    }

    public AccountProfileDataBean getProfile() {
        return profile;
    }

    public void setProfile(AccountProfileDataBean profile) {
        this.profile = profile;
    }

// removed unused methods
//    public void login(String password) {
//        AccountProfileDataBean profile = getProfile();
//        if ((profile == null) || (profile.getPassword().equals(password) == false)) {
//            String error = "AccountBean:Login failure for account: " + getAccountID() +
//                    ((profile == null) ? "null AccountProfile" :
//                            "\n\tIncorrect password-->" + profile.getUserID() + ":" + profile.getPassword());
//            throw new RuntimeException(error);
//            
//        }
//
//        setLastLogin(new Timestamp(System.currentTimeMillis()));
//        setLoginCount(getLoginCount() + 1);
//    }
//
//    public void logout() {
//        setLogoutCount(getLogoutCount() + 1);
//    }

    //
    // Profile is always set so changed this methoe accordingly.
    // The accountID is not assigned until the object is stored,
    // This eliminates the warning from the original code
    // 
    @Override
    public int hashCode() {
        int hash = 0;
        hash += (this.profileID != null ? this.profileID.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof AccountDataBean)) {
            return false;
        }
        AccountDataBean other = (AccountDataBean)object;
        if (this.profileID != other.profileID && (this.profileID == null || !this.profileID.equals(other.profileID))) return false;
        return true;
    }
}
