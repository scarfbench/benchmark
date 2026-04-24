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
package com.ibm.sample.daytrader.gateway.entities;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

import com.ibm.sample.daytrader.gateway.utils.Log;
import com.ibm.sample.daytrader.gateway.utils.TradeConfig;

public class HoldingDataBean implements Serializable {

    /* persistent/relationship fields */

    private Integer holdingID;              /* holdingID */
    private double quantity;                /* quantity */
    private BigDecimal purchasePrice;       /* purchasePrice */
    private Date purchaseDate;              /* purchaseDate */
    private String quoteID;                 /* Holding(*)  ---> Quote(1) */
    
// moved unused field into the user aggregate 
//    private AccountDataBean account;
    
// removed unused field
//    private QuoteDataBean quote;

//    @Version
//    private Integer optLock;

    public HoldingDataBean() {
    }

    public HoldingDataBean(Integer holdingID,
            double quantity,
            BigDecimal purchasePrice,
            Date purchaseDate,
            String quoteID) {
        setHoldingID(holdingID);
        setQuantity(quantity);
        setPurchasePrice(purchasePrice);
        setPurchaseDate(purchaseDate);
        setQuoteID(quoteID);
    }
  
    // added for integration testing
    public static HoldingDataBean getRandomInstance() {
    	return new HoldingDataBean(
    			new Integer(TradeConfig.rndInt(100000)),     				//holdingID
    			TradeConfig.rndQuantity(),                     				//quantity
    			TradeConfig.rndBigDecimal(1000.0f),             			//purchasePrice
    			new java.util.Date(TradeConfig.rndInt(Integer.MAX_VALUE)), 	//purchaseDate
    			TradeConfig.rndSymbol()                        				// symbol
    		);
    }
    
// removed unused constructor
//    public HoldingDataBean(double quantity,
//            BigDecimal purchasePrice,
//            Date purchaseDate,
//            AccountDataBean account,
//            QuoteDataBean quote) {
//        setQuantity(quantity);
//        setPurchasePrice(purchasePrice);
//        setPurchaseDate(purchaseDate);
//        setAccount(account);
//        setQuote(quote);
//    }

    public String toString() {
        return "\n\tHolding Data for holding: " + getHoldingID()
                + "\n\t\t      quantity:" + getQuantity()
                + "\n\t\t purchasePrice:" + getPurchasePrice()
                + "\n\t\t  purchaseDate:" + getPurchaseDate()
                + "\n\t\t       quoteID:" + getQuoteID()
                ;
    }

    public String toHTML() {
        return "<BR>Holding Data for holding: " + getHoldingID() + "</B>"
                + "<LI>      quantity:" + getQuantity() + "</LI>"
                + "<LI> purchasePrice:" + getPurchasePrice() + "</LI>"
                + "<LI>  purchaseDate:" + getPurchaseDate() + "</LI>"
                + "<LI>       quoteID:" + getQuoteID() + "</LI>"
                ;
    }

    public void print() {
        Log.log(this.toString());
    }

    public Integer getHoldingID() {
        return holdingID;
    }

    public void setHoldingID(Integer holdingID) {
        this.holdingID = holdingID;
    }

    public double getQuantity() {
        return quantity;
    }

    public void setQuantity(double quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getPurchasePrice() {
        return purchasePrice;
    }

    public void setPurchasePrice(BigDecimal purchasePrice) {
        this.purchasePrice = purchasePrice;
    }

    public Date getPurchaseDate() {
        return purchaseDate;
    }

    public void setPurchaseDate(Date purchaseDate) {
        this.purchaseDate = purchaseDate;
    }

    public String getQuoteID() {
        return quoteID;
    }

    public void setQuoteID(String quoteID) {
        this.quoteID = quoteID;
    }

// removed unused methods
//    public AccountDataBean getAccount() {
//        return account;
//    }
//
//    public void setAccount(AccountDataBean account) {
//        this.account = account;
//    }

    /* Disabled for D185273
     public String getSymbol() {
         return getQuoteID();
     }
     */
    
 // removed unused methods
//    public QuoteDataBean getQuote() {
//        return quote;
//    }
//
//    public void setQuote(QuoteDataBean quote) {
//        this.quote = quote;
//    }
    
    @Override
    public int hashCode() {
        int hash = 0;
        hash += (this.holdingID != null ? this.holdingID.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof HoldingDataBean)) {
            return false;
        }
        HoldingDataBean other = (HoldingDataBean) object;
        if (this.holdingID != other.holdingID && (this.holdingID == null || !this.holdingID.equals(other.holdingID))) return false;
        return true;
    }
}
