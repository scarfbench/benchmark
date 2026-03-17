/**
 * (C) Copyright IBM Corporation 2015.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.ibm.websphere.samples.daytrader.entities;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.Date;

// MIGRATION: javax.* -> jakarta.*
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import jakarta.persistence.Transient;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.PositiveOrZero;

import com.fasterxml.jackson.annotation.JsonIgnore;

import com.ibm.websphere.samples.daytrader.util.Log;
import com.ibm.websphere.samples.daytrader.util.TradeConfig;

@Entity(name = "accountejb")
@Table(name = "accountejb")
public class AccountDataBean implements Serializable {

    private static final long serialVersionUID = 8437841265136840545L;

    // MIGRATION: Simplified ID generation - Quarkus/Hibernate handles this better with IDENTITY
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ACCOUNTID", nullable = false)
    private Integer accountID;

    @NotNull
    @PositiveOrZero
    @Column(name = "LOGINCOUNT", nullable = false)
    private int loginCount;

    @NotNull
    @PositiveOrZero
    @Column(name = "LOGOUTCOUNT", nullable = false)
    private int logoutCount;

    @Column(name = "LASTLOGIN")
    @Temporal(TemporalType.TIMESTAMP)
    @PastOrPresent
    private Date lastLogin;

    @Column(name = "CREATIONDATE")
    @Temporal(TemporalType.TIMESTAMP)
    @PastOrPresent
    private Date creationDate;

    @Column(name = "BALANCE")
    private BigDecimal balance;

    @Column(name = "OPENBALANCE")
    private BigDecimal openBalance;

    @JsonIgnore
    @OneToMany(mappedBy = "account", fetch = FetchType.LAZY)
    private Collection<OrderDataBean> orders;

    @JsonIgnore
    @OneToMany(mappedBy = "account", fetch = FetchType.LAZY)
    private Collection<HoldingDataBean> holdings;

    @JsonIgnore
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "PROFILE_USERID")
    private AccountProfileDataBean profile;

    @Transient
    private String profileID;

    public AccountDataBean() {
    }

    public AccountDataBean(Integer accountID, int loginCount, int logoutCount, 
            Date lastLogin, Date creationDate, BigDecimal balance, 
            BigDecimal openBalance, String profileID) {
        setAccountID(accountID);
        setLoginCount(loginCount);
        setLogoutCount(logoutCount);
        setLastLogin(lastLogin);
        setCreationDate(creationDate);
        setBalance(balance);
        setOpenBalance(openBalance);
        setProfileID(profileID);
    }

    public AccountDataBean(int loginCount, int logoutCount, Date lastLogin, 
            Date creationDate, BigDecimal balance, BigDecimal openBalance, 
            String profileID) {
        setLoginCount(loginCount);
        setLogoutCount(logoutCount);
        setLastLogin(lastLogin);
        setCreationDate(creationDate);
        setBalance(balance);
        setOpenBalance(openBalance);
        setProfileID(profileID);
    }

    public void login(String password) {
        AccountProfileDataBean profile = getProfile();
        if (profile == null) {
            throw new RuntimeException("AccountDataBean:login -- Cannot find profile");
        }
        if (!password.equals(profile.getPassword())) {
            throw new RuntimeException("AccountDataBean:login -- Password does not match");
        }
        setLoginCount(getLoginCount() + 1);
        setLastLogin(new Date());
    }

    public void logout() {
        setLogoutCount(getLogoutCount() + 1);
    }

    @Override
    public String toString() {
        return "AccountDataBean [accountID=" + accountID + ", loginCount=" + loginCount 
            + ", logoutCount=" + logoutCount + ", lastLogin=" + lastLogin 
            + ", creationDate=" + creationDate + ", balance=" + balance 
            + ", openBalance=" + openBalance + "]";
    }

    public String toHTML() {
        return "<BR>AccountData [accountID=" + accountID + ", loginCount=" + loginCount
            + ", logoutCount=" + logoutCount + ", lastLogin=" + lastLogin
            + ", creationDate=" + creationDate + ", balance=" + balance
            + ", openBalance=" + openBalance + "]";
    }

    // Getters and Setters
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

    public Collection<OrderDataBean> getOrders() {
        return orders;
    }

    public void setOrders(Collection<OrderDataBean> orders) {
        this.orders = orders;
    }

    public Collection<HoldingDataBean> getHoldings() {
        return holdings;
    }

    public void setHoldings(Collection<HoldingDataBean> holdings) {
        this.holdings = holdings;
    }

    public AccountProfileDataBean getProfile() {
        return profile;
    }

    public void setProfile(AccountProfileDataBean profile) {
        this.profile = profile;
    }

    public String getProfileID() {
        return profileID;
    }

    public void setProfileID(String profileID) {
        this.profileID = profileID;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (this.accountID != null ? this.accountID.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof AccountDataBean)) {
            return false;
        }
        AccountDataBean other = (AccountDataBean) object;
        if (this.accountID != null && !this.accountID.equals(other.accountID)) {
            return false;
        }
        return true;
    }
}
