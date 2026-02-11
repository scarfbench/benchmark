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
import java.util.Date;

// MIGRATION: javax.* -> jakarta.*
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import jakarta.persistence.Transient;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Positive;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.ibm.websphere.samples.daytrader.util.Log;
import com.ibm.websphere.samples.daytrader.util.TradeConfig;

@Entity(name = "orderejb")
@Table(name = "orderejb")
@NamedQueries({
    @NamedQuery(name = "orderejb.findByOrderfee", query = "SELECT o FROM orderejb o WHERE o.orderFee = :orderfee"),
    @NamedQuery(name = "orderejb.findByOrderstatus", query = "SELECT o FROM orderejb o WHERE o.orderStatus = :orderstatus"),
    @NamedQuery(name = "orderejb.findByOrderid", query = "SELECT o FROM orderejb o WHERE o.orderID = :orderid"),
    @NamedQuery(name = "orderejb.findByAccountAccountid", query = "SELECT o FROM orderejb o WHERE o.account.accountID = :accountAccountid"),
    @NamedQuery(name = "orderejb.closedOrders", query = "SELECT o FROM orderejb o WHERE o.orderStatus = 'closed' AND o.account.profile.userID  = :userID"),
    @NamedQuery(name = "orderejb.completeClosedOrders", query = "UPDATE orderejb o SET o.orderStatus = 'completed' WHERE o.orderStatus = 'closed' AND o.account.profile.userID  = :userID")
})
public class OrderDataBean implements Serializable {

    private static final long serialVersionUID = 120650490200739057L;

    // MIGRATION: Simplified ID generation for Quarkus
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ORDERID", nullable = false)
    private Integer orderID;

    @Column(name = "ORDERTYPE")
    @NotBlank
    private String orderType;

    @Column(name = "ORDERSTATUS")
    @NotBlank
    private String orderStatus;

    @Column(name = "OPENDATE")
    @Temporal(TemporalType.TIMESTAMP)
    @PastOrPresent
    private Date openDate;

    @Column(name = "COMPLETIONDATE")
    @PastOrPresent
    @Temporal(TemporalType.TIMESTAMP)
    private Date completionDate;

    @NotNull
    @Column(name = "QUANTITY", nullable = false)
    private double quantity;

    @Column(name = "PRICE")
    @Positive
    private BigDecimal price;

    @Column(name = "ORDERFEE")
    @Positive
    private BigDecimal orderFee;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ACCOUNT_ACCOUNTID")
    private AccountDataBean account;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "QUOTE_SYMBOL")
    private QuoteDataBean quote;

    @JsonIgnore
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "HOLDING_HOLDINGID")
    private HoldingDataBean holding;

    @Transient
    private String symbol;

    public OrderDataBean() {
    }

    public OrderDataBean(Integer orderID, String orderType, String orderStatus, 
            Date openDate, Date completionDate, double quantity, 
            BigDecimal price, BigDecimal orderFee, String symbol) {
        setOrderID(orderID);
        setOrderType(orderType);
        setOrderStatus(orderStatus);
        setOpenDate(openDate);
        setCompletionDate(completionDate);
        setQuantity(quantity);
        setPrice(price);
        setOrderFee(orderFee);
        setSymbol(symbol);
    }

    public OrderDataBean(String orderType, String orderStatus, Date openDate, 
            Date completionDate, double quantity, BigDecimal price, 
            BigDecimal orderFee, AccountDataBean account, QuoteDataBean quote, 
            HoldingDataBean holding) {
        setOrderType(orderType);
        setOrderStatus(orderStatus);
        setOpenDate(openDate);
        setCompletionDate(completionDate);
        setQuantity(quantity);
        setPrice(price);
        setOrderFee(orderFee);
        setAccount(account);
        setQuote(quote);
        setHolding(holding);
    }

    public static OrderDataBean getRandomInstance() {
        return new OrderDataBean(
            Integer.valueOf(TradeConfig.rndInt(100000)), 
            TradeConfig.rndBoolean() ? "buy" : "sell", 
            "open", 
            new java.util.Date(TradeConfig.rndInt(Integer.MAX_VALUE)), 
            new java.util.Date(TradeConfig.rndInt(Integer.MAX_VALUE)), 
            TradeConfig.rndQuantity(),
            TradeConfig.rndBigDecimal(1000.0f), 
            TradeConfig.rndBigDecimal(1000.0f), 
            TradeConfig.rndSymbol()
        );
    }

    @Override
    public String toString() {
        return "Order " + getOrderID() 
            + "\n\t      orderType: " + getOrderType() 
            + "\n\t    orderStatus: " + getOrderStatus() 
            + "\n\t       openDate: " + getOpenDate() 
            + "\n\t completionDate: " + getCompletionDate() 
            + "\n\t       quantity: " + getQuantity() 
            + "\n\t          price: " + getPrice() 
            + "\n\t       orderFee: " + getOrderFee() 
            + "\n\t         symbol: " + getSymbol();
    }

    public String toHTML() {
        return "<BR>Order <B>" + getOrderID() + "</B>" 
            + "<LI>      orderType: " + getOrderType() + "</LI>" 
            + "<LI>    orderStatus: " + getOrderStatus() + "</LI>" 
            + "<LI>       openDate: " + getOpenDate() + "</LI>" 
            + "<LI> completionDate: " + getCompletionDate() + "</LI>"
            + "<LI>       quantity: " + getQuantity() + "</LI>" 
            + "<LI>          price: " + getPrice() + "</LI>" 
            + "<LI>       orderFee: " + getOrderFee() + "</LI>" 
            + "<LI>         symbol: " + getSymbol() + "</LI>";
    }

    public void print() {
        Log.log(this.toString());
    }

    public Integer getOrderID() {
        return orderID;
    }

    public void setOrderID(Integer orderID) {
        this.orderID = orderID;
    }

    public String getOrderType() {
        return orderType;
    }

    public void setOrderType(String orderType) {
        this.orderType = orderType;
    }

    public String getOrderStatus() {
        return orderStatus;
    }

    public void setOrderStatus(String orderStatus) {
        this.orderStatus = orderStatus;
    }

    public Date getOpenDate() {
        return openDate;
    }

    public void setOpenDate(Date openDate) {
        this.openDate = openDate;
    }

    public Date getCompletionDate() {
        return completionDate;
    }

    public void setCompletionDate(Date completionDate) {
        this.completionDate = completionDate;
    }

    public double getQuantity() {
        return quantity;
    }

    public void setQuantity(double quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public BigDecimal getOrderFee() {
        return orderFee;
    }

    public void setOrderFee(BigDecimal orderFee) {
        this.orderFee = orderFee;
    }

    public String getSymbol() {
        if (quote != null) {
            return quote.getSymbol();
        }
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public AccountDataBean getAccount() {
        return account;
    }

    public void setAccount(AccountDataBean account) {
        this.account = account;
    }

    public QuoteDataBean getQuote() {
        return quote;
    }

    public void setQuote(QuoteDataBean quote) {
        this.quote = quote;
    }

    public HoldingDataBean getHolding() {
        return holding;
    }

    public void setHolding(HoldingDataBean holding) {
        this.holding = holding;
    }

    public boolean isBuy() {
        return "buy".equalsIgnoreCase(orderType);
    }

    public boolean isSell() {
        return "sell".equalsIgnoreCase(orderType);
    }

    public boolean isOpen() {
        return "open".equalsIgnoreCase(orderStatus);
    }

    public boolean isCompleted() {
        return "completed".equalsIgnoreCase(orderStatus);
    }

    public boolean isCancelled() {
        return "cancelled".equalsIgnoreCase(orderStatus);
    }

    public void cancel() {
        setOrderStatus("cancelled");
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (this.orderID != null ? this.orderID.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof OrderDataBean)) {
            return false;
        }
        OrderDataBean other = (OrderDataBean) object;
        if (this.orderID != other.orderID && (this.orderID == null || !this.orderID.equals(other.orderID))) {
            return false;
        }
        return true;
    }
}
