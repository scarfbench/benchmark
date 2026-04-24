package com.ibm.sample.daytrader.gateway.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ibm.sample.daytrader.gateway.core.beans.MarketSummaryDataBean;
import com.ibm.sample.daytrader.gateway.core.beans.RunStatsDataBean;
import com.ibm.sample.daytrader.gateway.entities.QuoteDataBean;
import com.ibm.sample.daytrader.gateway.utils.Log;

import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import java.math.BigDecimal;
import java.util.Collection;

@ApplicationScoped
public class QuotesRemoteCallService extends BaseRemoteCallService {
    protected static ObjectMapper mapper;
    static { mapper = new ObjectMapper(); mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false); }

    @ConfigProperty(name = "daytrader.services.quotes", defaultValue = "http://localhost:4443")
    String quotesServiceRoute;

    public boolean tradeBuildDB(int limit, int offset) throws Exception {
        String url = quotesServiceRoute + "/admin/tradeBuildDB?limit="+limit+"&offset="+offset;
        Log.debug("QuotesRemoteCallService.quotesBuildDB() - " + url);
        String responseEntity = invokeEndpoint(url, "POST", "");
        return mapper.readValue(responseEntity, Boolean.class);
    }

    public RunStatsDataBean resetTrade(boolean deleteAll) throws Exception {
        String url = quotesServiceRoute + "/admin/resetTrade?deleteAll=" + deleteAll;
        Log.debug("QuotesRemoteCallService.resetTrade() - " + url);
        String responseEntity = invokeEndpoint(url, "GET", null);
        return mapper.readValue(responseEntity, RunStatsDataBean.class);
    }

    public boolean recreateDBTables() throws Exception {
        String url = quotesServiceRoute + "/admin/recreateDBTables";
        Log.debug("QuotesRemoteCallService.recreateDBTables() - " + url);
        String responseEntity = invokeEndpoint(url, "POST", "");
        return mapper.readValue(responseEntity, Boolean.class);
    }

    public MarketSummaryDataBean getMarketSummary() throws Exception {
        String exchange = "TSIA";
        String url = quotesServiceRoute + "/markets/" + exchange;
        Log.debug("QuotesRemoteCallService.getMarketSummary() - " + url);
        String responseString = invokeEndpoint(url, "GET", null);
        return mapper.readValue(responseString, MarketSummaryDataBean.class);
    }

    public QuoteDataBean createQuote(String symbol, String companyName, BigDecimal price) throws Exception {
        String url = quotesServiceRoute + "/quotes";
        Log.debug("QuotesRemoteCallService.createQuote() - " + url);
        QuoteDataBean quoteData = new QuoteDataBean();
        quoteData.setSymbol(symbol); quoteData.setCompanyName(companyName); quoteData.setPrice(price);
        String quoteDataInString = mapper.writeValueAsString(quoteData);
        String responseEntity = invokeEndpoint(url, "POST", quoteDataInString);
        return mapper.readValue(responseEntity, QuoteDataBean.class);
    }

    public QuoteDataBean getQuote(String symbol) throws Exception {
        String url = quotesServiceRoute + "/quotes/" + symbol;
        Log.debug("QuotesRemoteCallService.getQuote() - " + url);
        String responseString = invokeEndpoint(url, "GET", null);
        return mapper.readValue(responseString, QuoteDataBean.class);
    }

    public Collection<QuoteDataBean> getAllQuotes(int limit, int offset) throws Exception {
        String url = quotesServiceRoute + "/quotes?limit=" + limit + "&offset=" + offset;
        Log.debug("QuotesRemoteCallService.getAllQuotes() - " + url);
        String responseString = invokeEndpoint(url, "GET", null);
        return mapper.readValue(responseString, new TypeReference<Collection<QuoteDataBean>>(){});
    }

    public QuoteDataBean updateQuotePriceVolume(String symbol, BigDecimal price, double volume) throws Exception {
        String url = quotesServiceRoute + "/quotes/" + symbol + "?price=" + price + "&volume=" + volume;
        Log.debug("QuotesRemoteCallService.updateQuotePriceVolume() - " + url);
        String responseEntity = invokeEndpoint(url, "PATCH", "");
        return mapper.readValue(responseEntity, QuoteDataBean.class);
    }

    public QuoteDataBean updateQuotePriceVolumeInt(String symbol, BigDecimal changeFactor, double sharesTraded, boolean publishQuotePriceChange) throws Exception {
        return updateQuotePriceVolume(symbol, changeFactor, sharesTraded);
    }
}
