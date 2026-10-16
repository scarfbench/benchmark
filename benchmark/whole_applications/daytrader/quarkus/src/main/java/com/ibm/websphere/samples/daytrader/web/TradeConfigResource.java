package com.ibm.websphere.samples.daytrader.web;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import com.ibm.websphere.samples.daytrader.util.TradeConfig;

/**
 * Exposes DayTrader runtime configuration at GET /config.
 * The smoke test expects this page to exist and contain configuration
 * parameters such as max_users, max_quotes and orderfee.
 */
@Path("/config")
@Produces(MediaType.TEXT_HTML)
public class TradeConfigResource {

    @GET
    public Response getConfig() {
        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html>");
        html.append("<html><head><title>DayTrader Configuration</title></head><body>");
        html.append("<h1>DayTrader Configuration</h1>");
        html.append("<table border='1' cellpadding='5'>");
        html.append("<tr><th>Parameter</th><th>Value</th></tr>");
        html.append("<tr><td>max_users</td><td>").append(TradeConfig.getMAX_USERS()).append("</td></tr>");
        html.append("<tr><td>max_quotes</td><td>").append(TradeConfig.getMAX_QUOTES()).append("</td></tr>");
        html.append("<tr><td>orderfee</td><td>").append(TradeConfig.getOrderFee("BUY")).append("</td></tr>");
        html.append("<tr><td>Runtime Mode</td><td>Quarkus</td></tr>");
        html.append("</table>");
        html.append("<p><a href='/rest/app'>Back to DayTrader</a></p>");
        html.append("</body></html>");
        return Response.ok(html.toString()).build();
    }
}
