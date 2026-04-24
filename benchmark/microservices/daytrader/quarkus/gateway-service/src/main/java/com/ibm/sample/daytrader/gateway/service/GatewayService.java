package com.ibm.sample.daytrader.gateway.service;

import java.math.BigDecimal;
import java.util.Collection;

import com.ibm.sample.daytrader.gateway.core.beans.MarketSummaryDataBean;
import com.ibm.sample.daytrader.gateway.core.beans.RunStatsDataBean;
import com.ibm.sample.daytrader.gateway.entities.*;
import com.ibm.sample.daytrader.gateway.utils.Log;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class GatewayService {

    @Inject AccountsRemoteCallService accountsService;
    @Inject PortfoliosRemoteCallService portfoliosService;
    @Inject QuotesRemoteCallService quotesService;

    public boolean tradeBuildDB(int limit, int offset) throws Exception {
        if (accountsService.tradeBuildDB(limit, offset)) {
            return portfoliosService.tradeBuildDB(limit, offset);
        }
        return false;
    }

    public boolean quotesBuildDB(int limit, int offset) throws Exception {
        return quotesService.tradeBuildDB(limit, offset);
    }

    public RunStatsDataBean resetTrade(boolean deleteAll) throws Exception {
        RunStatsDataBean quoteStatsData = quotesService.resetTrade(deleteAll);
        RunStatsDataBean portfolioStatsData = portfoliosService.resetTrade(deleteAll);
        RunStatsDataBean accountStatsData = accountsService.resetTrade(deleteAll);
        RunStatsDataBean runStatsData = accountStatsData;
        runStatsData.setTradeStockCount(quoteStatsData.getTradeStockCount());
        runStatsData.setHoldingCount(portfolioStatsData.getHoldingCount());
        runStatsData.setOrderCount(portfolioStatsData.getOrderCount());
        runStatsData.setBuyOrderCount(portfolioStatsData.getBuyOrderCount());
        runStatsData.setSellOrderCount(portfolioStatsData.getSellOrderCount());
        runStatsData.setCancelledOrderCount(portfolioStatsData.getCancelledOrderCount());
        runStatsData.setOpenOrderCount(portfolioStatsData.getOpenOrderCount());
        runStatsData.setDeletedOrderCount(portfolioStatsData.getDeletedOrderCount());
        return runStatsData;
    }

    public boolean recreateDBTables() throws Exception {
        if (accountsService.recreateDBTables()) { Log.trace("Succeeded in re-creating Accounts databases"); }
        else { Log.error("Failed to re-create Accounts databases"); return false; }
        if (portfoliosService.recreateDBTables()) { Log.trace("Succeeded in re-creating Portfolios databases"); }
        else { Log.error("Failed to re-create Portfolios databases"); return false; }
        if (quotesService.recreateDBTables()) { Log.trace("Succeeded in re-creating Quotes databases"); }
        else { Log.error("Failed to re-create Quotes databases"); return false; }
        return true;
    }

    public OrderDataBean buy(String userID, String symbol, double quantity, int orderProcessingMode) throws Exception {
        return portfoliosService.buy(userID, symbol, quantity, orderProcessingMode);
    }

    public OrderDataBean sell(String userID, Integer holdingID, int orderProcessingMode) throws Exception {
        return portfoliosService.sell(userID, holdingID, orderProcessingMode);
    }

    public MarketSummaryDataBean getMarketSummary() throws Exception { return quotesService.getMarketSummary(); }
    public Collection<OrderDataBean> getOrders(String userID) throws Exception { return portfoliosService.getOrders(userID); }
    public Collection<OrderDataBean> getClosedOrders(String userID) throws Exception { return portfoliosService.getClosedOrders(userID); }
    public QuoteDataBean createQuote(String symbol, String companyName, BigDecimal price) throws Exception { return quotesService.createQuote(symbol, companyName, price); }
    public QuoteDataBean getQuote(String symbol) throws Exception { return quotesService.getQuote(symbol); }
    public Collection<QuoteDataBean> getAllQuotes(int limit, int offset) throws Exception { return quotesService.getAllQuotes(limit, offset); }
    public Collection<HoldingDataBean> getHoldings(String userID) throws Exception { return portfoliosService.getHoldings(userID); }
    public AccountDataBean getAccountData(String userID) throws Exception { return accountsService.getAccountData(userID); }
    public AccountProfileDataBean getAccountProfileData(String userID) throws Exception { return accountsService.getAccountProfileData(userID); }
    public AccountProfileDataBean updateAccountProfile(AccountProfileDataBean profileData) throws Exception { return accountsService.updateAccountProfile(profileData); }
    public QuoteDataBean updateQuotePriceVolume(String symbol, BigDecimal changeFactor, double sharesTraded) throws Exception { return quotesService.updateQuotePriceVolume(symbol, changeFactor, sharesTraded); }
    public AccountDataBean login(String userID, String password) throws Exception { return accountsService.login(userID, password); }
    public void logout(String userID) throws Exception { accountsService.logout(userID); }
    public AccountDataBean register(String userID, String password, String fullname, String address, String email, String creditCard, BigDecimal openBalance) throws Exception {
        return accountsService.register(userID, password, fullname, address, email, creditCard, openBalance);
    }
    public QuoteDataBean updateQuotePriceVolumeInt(String symbol, BigDecimal changeFactor, double sharesTraded, boolean publishQuotePriceChange) throws Exception {
        return quotesService.updateQuotePriceVolumeInt(symbol, changeFactor, sharesTraded, publishQuotePriceChange);
    }
}
