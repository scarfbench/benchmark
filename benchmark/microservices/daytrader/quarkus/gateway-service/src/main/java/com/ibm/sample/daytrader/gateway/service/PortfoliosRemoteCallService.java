package com.ibm.sample.daytrader.gateway.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ibm.sample.daytrader.gateway.core.beans.RunStatsDataBean;
import com.ibm.sample.daytrader.gateway.entities.HoldingDataBean;
import com.ibm.sample.daytrader.gateway.entities.OrderDataBean;
import com.ibm.sample.daytrader.gateway.utils.Log;

import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import java.util.ArrayList;
import java.util.Collection;

@ApplicationScoped
public class PortfoliosRemoteCallService extends BaseRemoteCallService {
    protected static ObjectMapper mapper;
    static { mapper = new ObjectMapper(); mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false); }

    @ConfigProperty(name = "daytrader.services.portfolios", defaultValue = "http://localhost:3443")
    String portfoliosServiceRoute;

    public boolean tradeBuildDB(int limit, int offset) throws Exception {
        String url = portfoliosServiceRoute + "/admin/tradeBuildDB?limit="+limit+"&offset="+offset;
        Log.debug("PortfoliosRemoteCallService.tradeBuildDB() - " + url);
        String responseEntity = invokeEndpoint(url, "POST", "");
        return mapper.readValue(responseEntity, Boolean.class);
    }

    public RunStatsDataBean resetTrade(boolean deleteAll) throws Exception {
        String url = portfoliosServiceRoute + "/admin/resetTrade?deleteAll=" + deleteAll;
        Log.debug("PortfoliosRemoteCallService.resetTrade() - " + url);
        String responseEntity = invokeEndpoint(url, "GET", null);
        return mapper.readValue(responseEntity, RunStatsDataBean.class);
    }

    public boolean recreateDBTables() throws Exception {
        String url = portfoliosServiceRoute + "/admin/recreateDBTables";
        Log.debug("PortfoliosRemoteCallService.recreateDBTables() - " + url);
        String responseEntity = invokeEndpoint(url, "POST", "");
        return mapper.readValue(responseEntity, Boolean.class);
    }

    public OrderDataBean buy(String userID, String symbol, double quantity, int orderProcessingMode) throws Exception {
        OrderDataBean orderData = new OrderDataBean();
        orderData.setSymbol(symbol); orderData.setQuantity(quantity); orderData.setOrderType("buy"); orderData.setOrderStatus("open");
        String orderDataInString = mapper.writeValueAsString(orderData);
        String url = portfoliosServiceRoute + "/portfolios/" + userID + "/orders?mode=" + orderProcessingMode;
        Log.debug("PortfoliosRemoteCallService.buy() - " + url);
        String responseEntity = invokeEndpoint(url, "POST", orderDataInString);
        return mapper.readValue(responseEntity, OrderDataBean.class);
    }

    public OrderDataBean sell(String userID, Integer holdingID, int orderProcessingMode) throws Exception {
        OrderDataBean orderData = new OrderDataBean();
        orderData.setHoldingID(holdingID); orderData.setOrderType("sell"); orderData.setOrderStatus("open");
        String orderDataInString = mapper.writeValueAsString(orderData);
        String url = portfoliosServiceRoute + "/portfolios/" + userID + "/orders?mode=" + orderProcessingMode;
        Log.debug("PortfoliosRemoteCallService.sell() - " + url);
        String responseEntity = invokeEndpoint(url, "POST", orderDataInString);
        return mapper.readValue(responseEntity, OrderDataBean.class);
    }

    public Collection<OrderDataBean> getOrders(String userID) throws Exception {
        String url = portfoliosServiceRoute + "/portfolios/" + userID + "/orders";
        Log.debug("PortfoliosRemoteCallService.getOrders() - " + url);
        String responseString = invokeEndpoint(url, "GET", null);
        return mapper.readValue(responseString, new TypeReference<ArrayList<OrderDataBean>>(){});
    }

    public Collection<OrderDataBean> getClosedOrders(String userID) throws Exception {
        String url = portfoliosServiceRoute + "/portfolios/" + userID + "/orders?status=closed";
        Log.debug("PortfoliosRemoteCallService.getClosedOrders() - " + url);
        String responseString = invokeEndpoint(url, "PATCH", "");
        return mapper.readValue(responseString, new TypeReference<ArrayList<OrderDataBean>>(){});
    }

    public Collection<HoldingDataBean> getHoldings(String userID) throws Exception {
        String url = portfoliosServiceRoute + "/portfolios/" + userID + "/holdings";
        Log.debug("PortfoliosRemoteCallService.getHoldings() - " + url);
        String responseString = invokeEndpoint(url, "GET", null);
        return mapper.readValue(responseString, new TypeReference<ArrayList<HoldingDataBean>>(){});
    }
}
