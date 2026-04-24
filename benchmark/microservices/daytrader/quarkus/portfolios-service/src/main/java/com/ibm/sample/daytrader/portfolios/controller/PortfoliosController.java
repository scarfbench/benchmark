package com.ibm.sample.daytrader.portfolios.controller;

import com.ibm.sample.daytrader.portfolios.service.PortfoliosService;
import com.ibm.sample.daytrader.portfolios.utils.Log;
import com.ibm.sample.daytrader.portfolios.direct.beans.RunStatsDataBean;
import com.ibm.sample.daytrader.portfolios.entities.AccountDataBean;
import com.ibm.sample.daytrader.portfolios.entities.HoldingDataBean;
import com.ibm.sample.daytrader.portfolios.entities.OrderDataBean;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

@Path("/")
@ApplicationScoped
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class PortfoliosController {

    @Inject
    PortfoliosService portfoliosService;

    @POST @Path("/portfolios/test")
    public Response testAPIRequest(Map<String, String> valMap) {
        return Response.ok(valMap.get("key")).build();
    }

    @POST @Path("/portfolios")
    public Response register(AccountDataBean accountData) {
        Log.traceEnter("PortfolioController.register()");
        try {
            accountData = portfoliosService.register(accountData);
            return Response.status(Response.Status.CREATED).header("Cache-Control", "no-cache").entity(accountData).build();
        } catch (Throwable t) {
            Log.error("PortfoliosController.register()", t);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GET @Path("/portfolios/{userId}")
    public Response getAccountData(@PathParam("userId") String userId) {
        Log.traceEnter("PortfolioController.getAccountData()");
        try {
            AccountDataBean accountData = portfoliosService.getAccountData(userId);
            if (accountData != null) return Response.ok(accountData).header("Cache-Control", "no-cache").build();
            else return Response.status(Response.Status.NO_CONTENT).header("Cache-Control", "no-cache").build();
        } catch (Throwable t) {
            Log.error("PortfoliosController.getAccountData()", t);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GET @Path("/portfolios/{userId}/holdings")
    public Response getHoldings(@PathParam("userId") String userId) {
        Log.traceEnter("PortfoliosController.getHoldings()");
        try {
            Collection<HoldingDataBean> holdings = portfoliosService.getHoldings(userId);
            if (holdings != null) return Response.ok(holdings).header("Cache-Control", "no-cache").build();
            else return Response.status(Response.Status.NO_CONTENT).header("Cache-Control", "no-cache").build();
        } catch (Throwable t) {
            Log.error("PortfoliosController.getHoldings()", t);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GET @Path("/portfolios/{userId}/orders")
    public Response getOrders(@PathParam("userId") String userId) {
        Log.traceEnter("PortfoliosController.getOrders()");
        try {
            Collection<OrderDataBean> orders = portfoliosService.getOrders(userId);
            if (orders != null) return Response.ok(orders).header("Cache-Control", "no-cache").build();
            else return Response.status(Response.Status.NO_CONTENT).header("Cache-Control", "no-cache").build();
        } catch (Throwable t) {
            Log.error("PortfoliosController.getOrders()", t);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PATCH @Path("/portfolios/{userId}/orders")
    public Response getOrdersByStatus(@PathParam("userId") String userId, @QueryParam("status") String status) {
        Log.traceEnter("PortfoliosController.getOrdersByStatus()");
        Collection<OrderDataBean> orders = new ArrayList<>();
        try {
            if (status.equals("closed")) {
                orders = portfoliosService.getClosedOrders(userId);
                if (orders != null) return Response.ok(orders).header("Cache-Control", "no-cache").build();
                else return Response.status(Response.Status.NO_CONTENT).header("Cache-Control", "no-cache").build();
            } else {
                return Response.status(Response.Status.BAD_REQUEST).header("Cache-Control", "no-cache").entity(orders).build();
            }
        } catch (Throwable t) {
            Log.error("PortfoliosController.getOrdersByStatus()", t);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(orders).build();
        }
    }

    @POST @Path("/portfolios/{userId}/orders")
    public Response processOrder(@PathParam("userId") String userId, OrderDataBean orderData, @QueryParam("mode") Integer mode) {
        Log.traceEnter("PortfoliosController.processOrder()");
        try {
            if (orderData.getOrderType().equals("buy")) {
                orderData = portfoliosService.buy(userId, orderData.getSymbol(), orderData.getQuantity(), mode);
                return Response.status(Response.Status.CREATED).header("Cache-Control", "no-cache").entity(orderData).build();
            }
        } catch (NotFoundException nfe) { return Response.status(Response.Status.NOT_FOUND).build(); }
          catch (ClientErrorException cee) { return Response.status(Response.Status.CONFLICT).build(); }
          catch (Throwable t) { Log.error("PortfoliosController.processOrder()", t); return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build(); }

        try {
            if (orderData.getOrderType().equals("sell")) {
                orderData = portfoliosService.sell(userId, orderData.getHoldingID(), mode);
                return Response.status(Response.Status.CREATED).header("Cache-Control", "no-cache").entity(orderData).build();
            }
        } catch (NotFoundException nfe) { return Response.status(Response.Status.NOT_FOUND).build(); }
          catch (ClientErrorException cee) { return Response.status(Response.Status.CONFLICT).build(); }
          catch (Throwable t) { Log.error("PortfoliosController.processOrder()", t); return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build(); }

        return Response.status(Response.Status.BAD_REQUEST).build();
    }

    @POST @Path("/admin/tradeBuildDB")
    public Response tradeBuildDB(@QueryParam("limit") int limit, @QueryParam("offset") int offset) {
        Log.traceEnter("PortfolioController.tradeBuildDB()");
        try {
            Boolean result = portfoliosService.tradeBuildDB(limit, offset);
            return Response.status(Response.Status.CREATED).header("Cache-Control", "no-cache").entity(result).build();
        } catch (Throwable t) {
            Log.error("PortfoliosController.tradeBuildDB()", t);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }

    @POST @Path("/admin/recreateDBTables")
    public Response recreateDBTables() {
        Log.traceEnter("PortfoliosController.recreateDBTables()");
        try {
            Boolean result = portfoliosService.recreateDBTables();
            return Response.status(Response.Status.CREATED).header("Cache-Control", "no-cache").entity(result).build();
        } catch (Throwable t) {
            Log.error("PortfoliosController.recreateDBTables()", t);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GET @Path("/admin/resetTrade")
    public Response resetTrade(@QueryParam("deleteAll") Boolean deleteAll) {
        Log.traceEnter("PortfoliosController.resetTrade()");
        try {
            RunStatsDataBean runStatsData = portfoliosService.resetTrade(deleteAll);
            if (runStatsData != null) return Response.ok(runStatsData).header("Cache-Control", "no-cache").build();
            else return Response.status(Response.Status.NO_CONTENT).header("Cache-Control", "no-cache").build();
        } catch (Throwable t) {
            Log.error("PortfoliosController.resetTrade()", t);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }
}
