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
package com.ibm.websphere.samples.daytrader.beans;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;

import com.ibm.websphere.samples.daytrader.entities.QuoteDataBean;
import com.ibm.websphere.samples.daytrader.util.FinancialUtils;
import com.ibm.websphere.samples.daytrader.util.Log;
import com.ibm.websphere.samples.daytrader.util.TradeConfig;

/**
 * MIGRATION NOTE: Removed javax.json dependency as it's not commonly used in Quarkus.
 * Use Jackson (which Quarkus uses by default) for JSON serialization instead.
 */
public class MarketSummaryDataBean implements Serializable {

    private static final long serialVersionUID = 650652242288745600L;
    
    private BigDecimal TSIA;           /* Trade Stock Index Average */
    private BigDecimal openTSIA;       /* Trade Stock Index Average at the open */
    private double volume;             /* volume of shares traded */
    private Collection<QuoteDataBean> topGainers;  /* Collection of top gaining stocks */
    private Collection<QuoteDataBean> topLosers;   /* Collection of top losing stocks */
    private Date summaryDate;          /* Date this summary was taken */
    private BigDecimal gainPercent = null;

    public MarketSummaryDataBean() {
    }

    public MarketSummaryDataBean(BigDecimal TSIA, BigDecimal openTSIA, double volume, 
            Collection<QuoteDataBean> topGainers, Collection<QuoteDataBean> topLosers) {
        setTSIA(TSIA);
        setOpenTSIA(openTSIA);
        setVolume(volume);
        setTopGainers(topGainers);
        setTopLosers(topLosers);
        setSummaryDate(new java.sql.Date(System.currentTimeMillis()));
        gainPercent = FinancialUtils.computeGainPercent(getTSIA(), getOpenTSIA());
    }

    public static MarketSummaryDataBean getRandomInstance() {
        Collection<QuoteDataBean> gain = new ArrayList<>();
        Collection<QuoteDataBean> lose = new ArrayList<>();

        for (int ii = 0; ii < 5; ii++) {
            QuoteDataBean quote1 = QuoteDataBean.getRandomInstance();
            QuoteDataBean quote2 = QuoteDataBean.getRandomInstance();
            gain.add(quote1);
            lose.add(quote2);
        }

        return new MarketSummaryDataBean(
            TradeConfig.rndBigDecimal(1000000.0f), 
            TradeConfig.rndBigDecimal(1000000.0f), 
            TradeConfig.rndQuantity(), 
            gain, 
            lose
        );
    }

    @Override
    public String toString() {
        String ret = "\n\tMarket Summary at: " + getSummaryDate() 
            + "\n\t\t        TSIA:" + getTSIA() 
            + "\n\t\t    openTSIA:" + getOpenTSIA()
            + "\n\t\t        gain:" + getGainPercent() 
            + "\n\t\t      volume:" + getVolume();

        if ((getTopGainers() == null) || (getTopLosers() == null)) {
            return ret;
        }
        
        ret += "\n\t\t   Current Top Gainers:";
        for (QuoteDataBean quoteData : getTopGainers()) {
            ret += ("\n\t\t\t" + quoteData.toString());
        }
        
        ret += "\n\t\t   Current Top Losers:";
        for (QuoteDataBean quoteData : getTopLosers()) {
            ret += ("\n\t\t\t" + quoteData.toString());
        }
        return ret;
    }

    public String toHTML() {
        String ret = "<BR>Market Summary at: " + getSummaryDate() 
            + "<LI>        TSIA:" + getTSIA() + "</LI>" 
            + "<LI>    openTSIA:" + getOpenTSIA() + "</LI>"
            + "<LI>      volume:" + getVolume() + "</LI>";
            
        if ((getTopGainers() == null) || (getTopLosers() == null)) {
            return ret;
        }
        
        ret += "<BR> Current Top Gainers:";
        for (QuoteDataBean quoteData : getTopGainers()) {
            ret += ("<LI>" + quoteData.toString() + "</LI>");
        }
        
        ret += "<BR>   Current Top Losers:";
        for (QuoteDataBean quoteData : getTopLosers()) {
            ret += ("<LI>" + quoteData.toString() + "</LI>");
        }
        return ret;
    }

    public void print() {
        Log.log(this.toString());
    }

    // Getters and Setters
    public BigDecimal getGainPercent() {
        if (gainPercent == null) {
            gainPercent = FinancialUtils.computeGainPercent(getTSIA(), getOpenTSIA());
        }
        return gainPercent;
    }

    public BigDecimal getTSIA() {
        return TSIA;
    }

    public void setTSIA(BigDecimal tSIA) {
        TSIA = tSIA;
    }

    public BigDecimal getOpenTSIA() {
        return openTSIA;
    }

    public void setOpenTSIA(BigDecimal openTSIA) {
        this.openTSIA = openTSIA;
    }

    public double getVolume() {
        return volume;
    }

    public void setVolume(double volume) {
        this.volume = volume;
    }

    public Collection<QuoteDataBean> getTopGainers() {
        return topGainers;
    }

    public void setTopGainers(Collection<QuoteDataBean> topGainers) {
        this.topGainers = topGainers;
    }

    public Collection<QuoteDataBean> getTopLosers() {
        return topLosers;
    }

    public void setTopLosers(Collection<QuoteDataBean> topLosers) {
        this.topLosers = topLosers;
    }

    public Date getSummaryDate() {
        return summaryDate;
    }

    public void setSummaryDate(Date summaryDate) {
        this.summaryDate = summaryDate;
    }
}
