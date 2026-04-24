package com.ibm.sample.daytrader.gateway.controller;

import com.ibm.sample.daytrader.gateway.core.beans.MarketSummaryDataBean;
import com.ibm.sample.daytrader.gateway.core.beans.RunStatsDataBean;
import com.ibm.sample.daytrader.gateway.service.GatewayService;
import com.ibm.sample.daytrader.gateway.utils.Log;
import com.ibm.sample.daytrader.gateway.entities.*;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

@Path("/")
@ApplicationScoped
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class GatewayController {

    @Inject
    GatewayService gatewayService;

    @POST @Path("/accounts")
    public Response register(AccountDataBean accountData) {
        Log.traceEnter("GatewayController.register()");
        String userID = accountData.getProfileID();
        String password = accountData.getProfile().getPassword();
        String fullname = accountData.getProfile().getFullName();
        String address = accountData.getProfile().getAddress();
        String email = accountData.getProfile().getEmail();
        String creditCard = accountData.getProfile().getCreditCard();
        BigDecimal openBalance = accountData.getOpenBalance();
        try {
            accountData = gatewayService.register(userID, password, fullname, address, email, creditCard, openBalance);
            Log.traceExit("GatewayController.register()");
            return Response.status(Response.Status.CREATED).header("Cache-Control","no-cache").entity(accountData).build();
        } catch(Throwable t) {
            Log.error("GatewayController.register()", t);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PUT @Path("/accounts/{userId}/profiles")
    public Response updateAccountProfile(@PathParam("userId") String userId, AccountProfileDataBean profileData) {
        Log.traceEnter("GatewayController.updateAccountProfile()");
        try {
            profileData = gatewayService.updateAccountProfile(profileData);
            if (profileData != null) return Response.ok(profileData).header("Cache-Control","no-cache").build();
            else return Response.status(Response.Status.NO_CONTENT).header("Cache-Control","no-cache").build();
        } catch(Throwable t) {
            Log.error("GatewayController.updateAccountProfile()", t);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GET @Path("/accounts/{userId}/profiles")
    public Response getAccountProfileData(@PathParam("userId") String userId) {
        Log.traceEnter("GatewayController.getAccountProfileData()");
        try {
            AccountProfileDataBean profileData = gatewayService.getAccountProfileData(userId);
            if (profileData != null) return Response.ok(profileData).header("Cache-Control","no-cache").build();
            else return Response.status(Response.Status.NO_CONTENT).header("Cache-Control","no-cache").build();
        } catch(Throwable t) {
            Log.error("GatewayController.getAccountProfileData()", t);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GET @Path("/accounts/{userId}")
    public Response getAccountData(@PathParam("userId") String userId) {
        Log.traceEnter("GatewayController.getAccountData()");
        try {
            AccountDataBean accountData = gatewayService.getAccountData(userId);
            if (accountData != null) return Response.ok(accountData).header("Cache-Control","no-cache").build();
            else return Response.status(Response.Status.NO_CONTENT).header("Cache-Control","no-cache").build();
        } catch(Throwable t) {
            Log.error("GatewayController.getAccountData()", t);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PATCH @Path("/login/{userId}")
    public Response login(@PathParam("userId") String userId, String password) {
        Log.traceEnter("GatewayController.login()");
        try {
            AccountDataBean accountData = gatewayService.login(userId, password);
            return Response.ok(accountData).header("Cache-Control","no-cache").build();
        } catch(NotAuthorizedException nae) {
            Log.error("GatewayController.login()", nae);
            return Response.status(Response.Status.UNAUTHORIZED).build();
        } catch(Throwable t) {
            Log.error("GatewayController.login()", t);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PATCH @Path("/logout/{userId}")
    public Response logout(@PathParam("userId") String userId) {
        Log.traceEnter("GatewayController.logout()");
        try {
            gatewayService.logout(userId);
            return Response.ok(true).build();
        } catch(Throwable t) {
            Log.error("GatewayController.logout()", t);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }

    @POST @Path("/admin/test")
    public Response testAPIRequest(Map<String,String> valMap) {
        return Response.ok(valMap.get("key")).build();
    }

    @POST @Path("/admin/tradeBuildDB")
    public Response tradeBuildDB(@QueryParam("limit") int limit, @QueryParam("offset") int offset) {
        Log.traceEnter("GatewayController.tradeBuildDB()");
        try {
            Boolean success = gatewayService.tradeBuildDB(limit, offset);
            return Response.status(Response.Status.CREATED).header("Cache-Control","no-cache").entity(success).build();
        } catch(Throwable t) {
            Log.error("GatewayController.tradeBuildDB()", t);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }

    @POST @Path("/admin/quotesBuildDB")
    public Response quotesBuildDB(@QueryParam("limit") int limit, @QueryParam("offset") int offset) {
        Log.traceEnter("GatewayController.quotesBuildDB()");
        try {
            Boolean success = gatewayService.quotesBuildDB(limit, offset);
            return Response.status(Response.Status.CREATED).header("Cache-Control","no-cache").entity(success).build();
        } catch(Throwable t) {
            Log.error("GatewayController.quotesBuildDB()", t);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }

    @POST @Path("/admin/recreateDBTables")
    public Response recreateDBTables() {
        Log.traceEnter("GatewayController.recreateDBTables()");
        try {
            boolean result = gatewayService.recreateDBTables();
            return Response.status(Response.Status.CREATED).header("Cache-Control","no-cache").entity(result).build();
        } catch(Throwable t) {
            Log.error("GatewayController.recreateDBTables()", t);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GET @Path("/admin/resetTrade")
    public Response resetTrade(@QueryParam("deleteAll") Boolean deleteAll) {
        Log.traceEnter("GatewayController.resetData()");
        try {
            RunStatsDataBean runStatsData = gatewayService.resetTrade(deleteAll);
            if (runStatsData != null) return Response.ok(runStatsData).header("Cache-Control","no-cache").build();
            else return Response.status(Response.Status.NO_CONTENT).header("Cache-Control","no-cache").build();
        } catch(Throwable t) {
            Log.error("GatewayController.resetTrade()", t);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GET @Path("/portfolios/{userId}/holdings")
    public Response getHoldings(@PathParam("userId") String userId) {
        Log.traceEnter("GatewayController.getHoldings()");
        try {
            Collection<HoldingDataBean> holdings = gatewayService.getHoldings(userId);
            if (holdings != null) return Response.ok(holdings).header("Cache-Control","no-cache").build();
            else return Response.status(Response.Status.NO_CONTENT).header("Cache-Control","no-cache").build();
        } catch(Throwable t) {
            Log.error("GatewayController.getHoldings()", t);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GET @Path("/portfolios/{userId}/orders")
    public Response getOrders(@PathParam("userId") String userId) {
        Log.traceEnter("GatewayController.getOrders()");
        try {
            Collection<OrderDataBean> orders = gatewayService.getOrders(userId);
            if (orders != null) return Response.ok(orders).header("Cache-Control","no-cache").build();
            else return Response.status(Response.Status.NO_CONTENT).header("Cache-Control","no-cache").build();
        } catch(Throwable t) {
            Log.error("GatewayController.getOrders()", t);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PATCH @Path("/portfolios/{userId}/orders")
    public Response getOrdersByStatus(@PathParam("userId") String userId, @QueryParam("status") String status) {
        Log.traceEnter("GatewayController.getOrdersByStatus()");
        Collection<OrderDataBean> orders = new ArrayList<>();
        try {
            if (status.equals("closed")) {
                orders = gatewayService.getClosedOrders(userId);
                if (orders != null) return Response.ok(orders).header("Cache-Control","no-cache").build();
                else return Response.status(Response.Status.NO_CONTENT).header("Cache-Control","no-cache").build();
            } else {
                return Response.status(Response.Status.BAD_REQUEST).header("Cache-Control","no-cache").entity(orders).build();
            }
        } catch(Throwable t) {
            Log.error("GatewayController.getOrdersByStatus()", t);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(orders).build();
        }
    }

    @POST @Path("/portfolios/{userId}/orders")
    public Response processOrder(@PathParam("userId") String userId, OrderDataBean orderData, @QueryParam("mode") Integer mode) {
        Log.traceEnter("GatewayController.processOrder()");
        try {
            if (orderData.getOrderType().equals("buy")) {
                orderData = gatewayService.buy(userId, orderData.getSymbol(), orderData.getQuantity(), mode);
                return Response.status(Response.Status.CREATED).header("Cache-Control","no-cache").entity(orderData).build();
            }
        } catch(NotFoundException nfe) { return Response.status(Response.Status.NOT_FOUND).build(); }
          catch(ClientErrorException cee) { return Response.status(Response.Status.CONFLICT).build(); }
          catch(Throwable t) { Log.error("GatewayController.processOrder()", t); return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build(); }

        try {
            if (orderData.getOrderType().equals("sell")) {
                orderData = gatewayService.sell(userId, orderData.getHoldingID(), mode);
                return Response.status(Response.Status.CREATED).header("Cache-Control","no-cache").entity(orderData).build();
            }
        } catch(NotFoundException nfe) { return Response.status(Response.Status.NOT_FOUND).build(); }
          catch(ClientErrorException cee) { return Response.status(Response.Status.CONFLICT).build(); }
          catch(Throwable t) { Log.error("GatewayController.processOrder()", t); return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build(); }

        return Response.status(Response.Status.BAD_REQUEST).header("Cache-Control","no-cache").entity(orderData).build();
    }

    @GET @Path("/quotes/{symbol}")
    public Response getQuote(@PathParam("symbol") String symbol) {
        Log.traceEnter("GatewayController.getQuote()");
        try {
            QuoteDataBean quoteData = gatewayService.getQuote(symbol);
            if (quoteData != null) return Response.ok(quoteData).header("Cache-Control","no-cache").build();
            else return Response.status(Response.Status.NO_CONTENT).header("Cache-Control","no-cache").build();
        } catch(Throwable t) {
            Log.error("GatewayController.getQuote()", t);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GET @Path("/quotes")
    public Response getAllQuotes(@QueryParam("limit") Integer limit, @QueryParam("offset") Integer offset) {
        Log.traceEnter("GatewayController.getAllQuotes()");
        try {
            Collection<QuoteDataBean> quotes = gatewayService.getAllQuotes(limit, offset);
            if (quotes != null) return Response.ok(quotes).header("Cache-Control","no-cache").build();
            else return Response.status(Response.Status.NO_CONTENT).header("Cache-Control","no-cache").build();
        } catch(Throwable t) {
            Log.error("GatewayController.getAllQuotes()", t);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }

    @POST @Path("/quotes")
    public Response createQuote(QuoteDataBean quoteData) {
        Log.traceEnter("GatewayController.createQuote()");
        try {
            quoteData = gatewayService.createQuote(quoteData.getSymbol(), quoteData.getCompanyName(), quoteData.getPrice());
            return Response.status(Response.Status.CREATED).header("Cache-Control","no-cache").entity(quoteData).build();
        } catch(Throwable t) {
            Log.error("GatewayController.createQuote()", t);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PATCH @Path("/quotes/{symbol}")
    public Response updateQuotePriceVolume(@PathParam("symbol") String symbol, @QueryParam("price") BigDecimal price, @QueryParam("volume") double volume) {
        Log.trace("GatewayController.updateQuotePriceVolume()");
        try {
            QuoteDataBean quoteData = gatewayService.updateQuotePriceVolume(symbol, price, volume);
            if (quoteData != null) return Response.ok(quoteData).header("Cache-Control","no-cache").build();
            else return Response.status(Response.Status.NO_CONTENT).header("Cache-Control","no-cache").build();
        } catch(Throwable t) {
            Log.error("GatewayController.updateQuotePriceVolume()", t);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GET @Path("/markets/{exchange}")
    public Response getMarketSummary(@PathParam("exchange") String exchange) {
        Log.traceEnter("GatewayController.getMarketSummary()");
        try {
            MarketSummaryDataBean marketSummary = gatewayService.getMarketSummary();
            return Response.ok(marketSummary).header("Cache-Control","no-cache").build();
        } catch(NotFoundException nfe) {
            return Response.status(Response.Status.NOT_FOUND).build();
        } catch(Throwable t) {
            Log.error("GatewayController.getMarketSummary()", t);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }
}
