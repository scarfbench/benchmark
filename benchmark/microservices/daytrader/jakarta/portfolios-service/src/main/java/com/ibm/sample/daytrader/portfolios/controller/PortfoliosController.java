package com.ibm.sample.daytrader.portfolios.controller;

import com.ibm.sample.daytrader.portfolios.service.PortfoliosService;
import com.ibm.sample.daytrader.portfolios.utils.Log;
import com.ibm.sample.daytrader.portfolios.direct.beans.RunStatsDataBean;
import com.ibm.sample.daytrader.portfolios.entities.AccountDataBean;
import com.ibm.sample.daytrader.portfolios.entities.HoldingDataBean;
import com.ibm.sample.daytrader.portfolios.entities.OrderDataBean;

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
public class PortfoliosController {

    private static PortfoliosService portfoliosService = new PortfoliosService();

    @POST @Path("/portfolios")
    public Response register(AccountDataBean accountData) {
        try { accountData = portfoliosService.register(accountData);
            return Response.status(Response.Status.CREATED).header("Cache-Control","no-cache").entity(accountData).build();
        } catch (Throwable t) { Log.error("PortfoliosController.register()", t); return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build(); }
    }

    @GET @Path("/portfolios/{userId}")
    public Response getAccountData(@PathParam("userId") String userId) {
        try { AccountDataBean accountData = portfoliosService.getAccountData(userId);
            if (accountData != null) return Response.ok().header("Cache-Control","no-cache").entity(accountData).build();
            else return Response.status(Response.Status.NO_CONTENT).build();
        } catch (Throwable t) { Log.error("PortfoliosController.getAccountData()", t); return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build(); }
    }

    @GET @Path("/portfolios/{userId}/holdings")
    public Response getHoldings(@PathParam("userId") String userId) {
        try { Collection<HoldingDataBean> holdings = portfoliosService.getHoldings(userId);
            if (holdings != null) return Response.ok().header("Cache-Control","no-cache").entity(holdings).build();
            else return Response.status(Response.Status.NO_CONTENT).build();
        } catch (Throwable t) { Log.error("PortfoliosController.getHoldings()", t); return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build(); }
    }

    @GET @Path("/portfolios/{userId}/orders")
    public Response getOrders(@PathParam("userId") String userId) {
        try { Collection<OrderDataBean> orders = portfoliosService.getOrders(userId);
            if (orders != null) return Response.ok().header("Cache-Control","no-cache").entity(orders).build();
            else return Response.status(Response.Status.NO_CONTENT).build();
        } catch (Throwable t) { Log.error("PortfoliosController.getOrders()", t); return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build(); }
    }

    @PATCH @Path("/portfolios/{userId}/orders")
    public Response getOrdersByStatus(@PathParam("userId") String userId, @QueryParam("status") String status) {
        try {
            if ("closed".equals(status)) {
                Collection<OrderDataBean> orders = portfoliosService.getClosedOrders(userId);
                if (orders != null) return Response.ok().header("Cache-Control","no-cache").entity(orders).build();
                else return Response.status(Response.Status.NO_CONTENT).entity(new ArrayList<>()).build();
            } else return Response.status(Response.Status.BAD_REQUEST).entity(new ArrayList<>()).build();
        } catch (Throwable t) { Log.error("PortfoliosController.getOrdersByStatus()", t); return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build(); }
    }

    @POST @Path("/portfolios/{userId}/orders")
    public Response processOrder(@PathParam("userId") String userId, OrderDataBean orderData, @QueryParam("mode") Integer mode) {
        try {
            if ("buy".equals(orderData.getOrderType())) {
                orderData = portfoliosService.buy(userId, orderData.getSymbol(), orderData.getQuantity(), mode);
                return Response.status(Response.Status.CREATED).header("Cache-Control","no-cache").entity(orderData).build();
            } else if ("sell".equals(orderData.getOrderType())) {
                orderData = portfoliosService.sell(userId, orderData.getHoldingID(), mode);
                return Response.status(Response.Status.CREATED).header("Cache-Control","no-cache").entity(orderData).build();
            }
            return Response.status(Response.Status.BAD_REQUEST).build();
        } catch (javax.ws.rs.NotFoundException nfe) { return Response.status(Response.Status.NOT_FOUND).build();
        } catch (javax.ws.rs.ClientErrorException cee) { return Response.status(Response.Status.CONFLICT).build();
        } catch (Throwable t) { Log.error("PortfoliosController.processOrder()", t); return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build(); }
    }

    @POST @Path("/admin/tradeBuildDB")
    public Response tradeBuildDB(@QueryParam("limit") int limit, @QueryParam("offset") int offset) {
        try { Boolean result = portfoliosService.tradeBuildDB(limit, offset);
            return Response.status(Response.Status.CREATED).header("Cache-Control","no-cache").entity(result).build();
        } catch (Throwable t) { Log.error("PortfoliosController.tradeBuildDB()", t); return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build(); }
    }

    @POST @Path("/admin/recreateDBTables")
    public Response recreateDBTables() {
        try { Boolean result = portfoliosService.recreateDBTables();
            return Response.status(Response.Status.CREATED).header("Cache-Control","no-cache").entity(result).build();
        } catch (Throwable t) { Log.error("PortfoliosController.recreateDBTables()", t); return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build(); }
    }

    @GET @Path("/admin/resetTrade")
    public Response resetTrade(@QueryParam("deleteAll") Boolean deleteAll) {
        try { RunStatsDataBean runStatsData = portfoliosService.resetTrade(deleteAll);
            if (runStatsData != null) return Response.ok().header("Cache-Control","no-cache").entity(runStatsData).build();
            else return Response.status(Response.Status.NO_CONTENT).build();
        } catch (Throwable t) { Log.error("PortfoliosController.resetTrade()", t); return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build(); }
    }
}
