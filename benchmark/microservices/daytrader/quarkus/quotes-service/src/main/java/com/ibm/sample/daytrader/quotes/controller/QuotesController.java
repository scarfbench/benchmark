package com.ibm.sample.daytrader.quotes.controller;

import com.ibm.sample.daytrader.quotes.service.QuotesService;
import com.ibm.sample.daytrader.quotes.utils.Log;
import com.ibm.sample.daytrader.quotes.core.beans.MarketSummaryDataBean;
import com.ibm.sample.daytrader.quotes.core.beans.RunStatsDataBean;
import com.ibm.sample.daytrader.quotes.entities.QuoteDataBean;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Map;

@Path("/")
@ApplicationScoped
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class QuotesController {

    @Inject
    QuotesService quotesService;

    @POST @Path("/quotes/test")
    public Response testAPIRequest(Map<String, String> valMap) {
        return Response.ok(valMap.get("key")).build();
    }

    @GET @Path("/quotes/{symbol}")
    public Response getQuote(@PathParam("symbol") String symbol) {
        Log.traceEnter("QuotesController.getQuote()");
        try {
            QuoteDataBean quoteData = quotesService.getQuote(symbol);
            if (quoteData != null) return Response.ok(quoteData).header("Cache-Control", "no-cache").build();
            else return Response.status(Response.Status.NO_CONTENT).header("Cache-Control", "no-cache").build();
        } catch (Throwable t) {
            Log.error("QuotesController.getQuote()", t);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GET @Path("/quotes")
    public Response getAllQuotes(@QueryParam("limit") Integer limit, @QueryParam("offset") Integer offset) {
        Log.traceEnter("QuotesController.getAllQuotes()");
        try {
            Collection<QuoteDataBean> quotes = quotesService.getAllQuotes();
            if (quotes != null) return Response.ok(quotes).header("Cache-Control", "no-cache").build();
            else return Response.status(Response.Status.NO_CONTENT).header("Cache-Control", "no-cache").build();
        } catch (Throwable t) {
            Log.error("QuotesController.getAllQuotes()", t);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }

    @POST @Path("/quotes")
    public Response createQuote(QuoteDataBean quoteData) {
        Log.traceEnter("QuotesController.createQuote()");
        try {
            quoteData = quotesService.createQuote(quoteData.getSymbol(), quoteData.getCompanyName(), quoteData.getPrice());
            return Response.status(Response.Status.CREATED).header("Cache-Control", "no-cache").entity(quoteData).build();
        } catch (Throwable t) {
            Log.error("QuotesController.createQuote()", t);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PATCH @Path("/quotes/{symbol}")
    public Response updateQuotePriceVolume(@PathParam("symbol") String symbol, @QueryParam("price") BigDecimal price, @QueryParam("volume") double volume) {
        Log.traceEnter("QuotesController.updateQuotePriceVolume()");
        try {
            QuoteDataBean quoteData = quotesService.updateQuotePriceVolumeInt(symbol, price, volume);
            if (quoteData != null) return Response.ok(quoteData).header("Cache-Control", "no-cache").build();
            else return Response.status(Response.Status.NO_CONTENT).header("Cache-Control", "no-cache").build();
        } catch (Throwable t) {
            Log.error("QuotesController.updateQuotePriceVolume()", t);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GET @Path("/markets/{exchange}")
    public Response getMarketSummary(@PathParam("exchange") String exchange) {
        Log.traceEnter("QuotesController.getMarketSummary()");
        try {
            MarketSummaryDataBean marketSummary = quotesService.getMarketSummary(exchange);
            return Response.ok(marketSummary).header("Cache-Control", "no-cache").build();
        } catch (NotFoundException nfe) {
            return Response.status(Response.Status.NOT_FOUND).build();
        } catch (Throwable t) {
            Log.error("QuotesController.getMarketSummary()", t);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }

    @POST @Path("/admin/tradeBuildDB")
    public Response tradeBuildDB(@QueryParam("limit") Integer limit, @QueryParam("offset") Integer offset) {
        Log.traceEnter("QuotesController.tradeBuildDB()");
        try {
            Boolean success = quotesService.tradeBuildDB(limit, offset);
            return Response.status(Response.Status.CREATED).header("Cache-Control", "no-cache").entity(success).build();
        } catch (Throwable t) {
            Log.error("QuotesController.tradeBuildDB()", t);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }

    @POST @Path("/admin/recreateDBTables")
    public Response recreateDBTables() {
        Log.traceEnter("QuotesController.recreateDBTables()");
        try {
            Boolean result = quotesService.recreateDBTables();
            return Response.status(Response.Status.CREATED).header("Cache-Control", "no-cache").entity(result).build();
        } catch (Throwable t) {
            Log.error("QuotesController.recreateDBTables()", t);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GET @Path("/admin/resetTrade")
    public Response resetTrade(@QueryParam("deleteAll") Boolean deleteAll) {
        Log.traceEnter("QuotesController.resetTrade()");
        try {
            RunStatsDataBean runStatsData = quotesService.resetTrade(deleteAll);
            if (runStatsData != null) return Response.ok(runStatsData).header("Cache-Control", "no-cache").build();
            else return Response.status(Response.Status.NO_CONTENT).header("Cache-Control", "no-cache").build();
        } catch (Throwable t) {
            Log.error("QuotesController.resetTrade()", t);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }
}
