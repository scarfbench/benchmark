package com.ibm.sample.daytrader.gateway.controller;

import com.ibm.sample.daytrader.gateway.core.beans.MarketSummaryDataBean;
import com.ibm.sample.daytrader.gateway.core.beans.RunStatsDataBean;
import com.ibm.sample.daytrader.gateway.service.GatewayService;
import com.ibm.sample.daytrader.gateway.utils.Log;
import com.ibm.sample.daytrader.gateway.entities.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/")
@ApplicationScoped
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class GatewayController {

    private static GatewayService gatewayService = new GatewayService();

    @POST @Path("/accounts")
    public Response register(AccountDataBean accountData) {
        Log.traceEnter("GatewayController.register()");
        try {
            String userID = accountData.getProfileID();
            String password = accountData.getProfile().getPassword();
            String fullname = accountData.getProfile().getFullName();
            String address = accountData.getProfile().getAddress();
            String email = accountData.getProfile().getEmail();
            String creditCard = accountData.getProfile().getCreditCard();
            BigDecimal openBalance = accountData.getOpenBalance();
            accountData = gatewayService.register(userID, password, fullname, address, email, creditCard, openBalance);
            return Response.status(Response.Status.CREATED).header("Cache-Control","no-cache").entity(accountData).build();
        } catch (Throwable t) {
            Log.error("GatewayController.register()", t);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PUT @Path("/accounts/{userId}/profiles")
    public Response updateAccountProfile(@PathParam("userId") String userId, AccountProfileDataBean profileData) {
        try {
            profileData = gatewayService.updateAccountProfile(profileData);
            if (profileData != null) return Response.ok().header("Cache-Control","no-cache").entity(profileData).build();
            else return Response.status(Response.Status.NO_CONTENT).build();
        } catch (Throwable t) { Log.error("GatewayController.updateAccountProfile()", t); return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build(); }
    }

    @GET @Path("/accounts/{userId}/profiles")
    public Response getAccountProfileData(@PathParam("userId") String userId) {
        try {
            AccountProfileDataBean profileData = gatewayService.getAccountProfileData(userId);
            if (profileData != null) return Response.ok().header("Cache-Control","no-cache").entity(profileData).build();
            else return Response.status(Response.Status.NO_CONTENT).build();
        } catch (Throwable t) { Log.error("GatewayController.getAccountProfileData()", t); return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build(); }
    }

    @GET @Path("/accounts/{userId}")
    public Response getAccountData(@PathParam("userId") String userId) {
        try {
            AccountDataBean accountData = gatewayService.getAccountData(userId);
            if (accountData != null) return Response.ok().header("Cache-Control","no-cache").entity(accountData).build();
            else return Response.status(Response.Status.NO_CONTENT).build();
        } catch (Throwable t) { Log.error("GatewayController.getAccountData()", t); return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build(); }
    }

    @PATCH @Path("/login/{userId}")
    public Response login(@PathParam("userId") String userId, String password) {
        try {
            AccountDataBean accountData = gatewayService.login(userId, password);
            return Response.ok().header("Cache-Control","no-cache").entity(accountData).build();
        } catch (javax.ws.rs.NotAuthorizedException nae) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        } catch (Throwable t) { Log.error("GatewayController.login()", t); return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build(); }
    }

    @PATCH @Path("/logout/{userId}")
    public Response logout(@PathParam("userId") String userId) {
        try { gatewayService.logout(userId); return Response.ok().entity(true).build();
        } catch (Throwable t) { Log.error("GatewayController.logout()", t); return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build(); }
    }

    @POST @Path("/admin/tradeBuildDB")
    public Response tradeBuildDB(@QueryParam("limit") int limit, @QueryParam("offset") int offset) {
        try { Boolean success = gatewayService.tradeBuildDB(limit, offset); return Response.status(Response.Status.CREATED).header("Cache-Control","no-cache").entity(success).build();
        } catch (Throwable t) { Log.error("GatewayController.tradeBuildDB()", t); return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build(); }
    }

    @POST @Path("/admin/quotesBuildDB")
    public Response quotesBuildDB(@QueryParam("limit") int limit, @QueryParam("offset") int offset) {
        try { Boolean success = gatewayService.quotesBuildDB(limit, offset); return Response.status(Response.Status.CREATED).header("Cache-Control","no-cache").entity(success).build();
        } catch (Throwable t) { Log.error("GatewayController.quotesBuildDB()", t); return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build(); }
    }

    @POST @Path("/admin/recreateDBTables")
    public Response recreateDBTables() {
        try { boolean result = gatewayService.recreateDBTables(); return Response.status(Response.Status.CREATED).header("Cache-Control","no-cache").entity(result).build();
        } catch (Throwable t) { Log.error("GatewayController.recreateDBTables()", t); return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build(); }
    }

    @GET @Path("/admin/resetTrade")
    public Response resetTrade(@QueryParam("deleteAll") Boolean deleteAll) {
        try { RunStatsDataBean runStatsData = gatewayService.resetTrade(deleteAll);
            if (runStatsData != null) return Response.ok().header("Cache-Control","no-cache").entity(runStatsData).build();
            else return Response.status(Response.Status.NO_CONTENT).build();
        } catch (Throwable t) { Log.error("GatewayController.resetTrade()", t); return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build(); }
    }

    @GET @Path("/portfolios/{userId}/holdings")
    public Response getHoldings(@PathParam("userId") String userId) {
        try { Collection<HoldingDataBean> holdings = gatewayService.getHoldings(userId);
            if (holdings != null) return Response.ok().header("Cache-Control","no-cache").entity(holdings).build();
            else return Response.status(Response.Status.NO_CONTENT).build();
        } catch (Throwable t) { Log.error("GatewayController.getHoldings()", t); return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build(); }
    }

    @GET @Path("/portfolios/{userId}/orders")
    public Response getOrders(@PathParam("userId") String userId) {
        try { Collection<OrderDataBean> orders = gatewayService.getOrders(userId);
            if (orders != null) return Response.ok().header("Cache-Control","no-cache").entity(orders).build();
            else return Response.status(Response.Status.NO_CONTENT).build();
        } catch (Throwable t) { Log.error("GatewayController.getOrders()", t); return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build(); }
    }

    @PATCH @Path("/portfolios/{userId}/orders")
    public Response getOrdersByStatus(@PathParam("userId") String userId, @QueryParam("status") String status) {
        try {
            if ("closed".equals(status)) {
                Collection<OrderDataBean> orders = gatewayService.getClosedOrders(userId);
                if (orders != null) return Response.ok().header("Cache-Control","no-cache").entity(orders).build();
                else return Response.status(Response.Status.NO_CONTENT).entity(new ArrayList<>()).build();
            } else {
                return Response.status(Response.Status.BAD_REQUEST).entity(new ArrayList<>()).build();
            }
        } catch (Throwable t) { Log.error("GatewayController.getOrdersByStatus()", t); return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build(); }
    }

    @POST @Path("/portfolios/{userId}/orders")
    public Response processOrder(@PathParam("userId") String userId, OrderDataBean orderData, @QueryParam("mode") Integer mode) {
        try {
            if ("buy".equals(orderData.getOrderType())) {
                orderData = gatewayService.buy(userId, orderData.getSymbol(), orderData.getQuantity(), mode);
                return Response.status(Response.Status.CREATED).header("Cache-Control","no-cache").entity(orderData).build();
            } else if ("sell".equals(orderData.getOrderType())) {
                orderData = gatewayService.sell(userId, orderData.getHoldingID(), mode);
                return Response.status(Response.Status.CREATED).header("Cache-Control","no-cache").entity(orderData).build();
            }
            return Response.status(Response.Status.BAD_REQUEST).entity(orderData).build();
        } catch (javax.ws.rs.NotFoundException nfe) { return Response.status(Response.Status.NOT_FOUND).build();
        } catch (javax.ws.rs.ClientErrorException cee) { return Response.status(Response.Status.CONFLICT).build();
        } catch (Throwable t) { Log.error("GatewayController.processOrder()", t); return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build(); }
    }

    @GET @Path("/quotes/{symbol}")
    public Response getQuote(@PathParam("symbol") String symbol) {
        try { QuoteDataBean quoteData = gatewayService.getQuote(symbol);
            if (quoteData != null) return Response.ok().header("Cache-Control","no-cache").entity(quoteData).build();
            else return Response.status(Response.Status.NO_CONTENT).build();
        } catch (Throwable t) { Log.error("GatewayController.getQuote()", t); return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build(); }
    }

    @GET @Path("/quotes")
    public Response getAllQuotes(@QueryParam("limit") Integer limit, @QueryParam("offset") Integer offset) {
        try { Collection<QuoteDataBean> quotes = gatewayService.getAllQuotes(limit, offset);
            if (quotes != null) return Response.ok().header("Cache-Control","no-cache").entity(quotes).build();
            else return Response.status(Response.Status.NO_CONTENT).build();
        } catch (Throwable t) { Log.error("GatewayController.getAllQuotes()", t); return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build(); }
    }

    @POST @Path("/quotes")
    public Response createQuote(QuoteDataBean quoteData) {
        try { quoteData = gatewayService.createQuote(quoteData.getSymbol(), quoteData.getCompanyName(), quoteData.getPrice());
            return Response.status(Response.Status.CREATED).header("Cache-Control","no-cache").entity(quoteData).build();
        } catch (Throwable t) { Log.error("GatewayController.createQuote()", t); return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build(); }
    }

    @PATCH @Path("/quotes/{symbol}")
    public Response updateQuotePriceVolume(@PathParam("symbol") String symbol, @QueryParam("price") BigDecimal price, @QueryParam("volume") double volume) {
        try { QuoteDataBean quoteData = gatewayService.updateQuotePriceVolume(symbol, price, volume);
            if (quoteData != null) return Response.ok().header("Cache-Control","no-cache").entity(quoteData).build();
            else return Response.status(Response.Status.NO_CONTENT).build();
        } catch (Throwable t) { Log.error("GatewayController.updateQuotePriceVolume()", t); return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build(); }
    }

    @GET @Path("/markets/{exchange}")
    public Response getMarketSummary(@PathParam("exchange") String exchange) {
        try { MarketSummaryDataBean marketSummary = gatewayService.getMarketSummary();
            return Response.ok().header("Cache-Control","no-cache").entity(marketSummary).build();
        } catch (javax.ws.rs.NotFoundException nfe) { return Response.status(Response.Status.NOT_FOUND).build();
        } catch (Throwable t) { Log.error("GatewayController.getMarketSummary()", t); return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build(); }
    }
}
