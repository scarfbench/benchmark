/**
 * (C) Copyright IBM Corporation 2015, 2024.
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
package com.ibm.websphere.samples.daytrader.web;

import java.math.BigDecimal;
import java.util.Collection;

import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.FormParam;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import com.ibm.websphere.samples.daytrader.entities.AccountDataBean;
import com.ibm.websphere.samples.daytrader.entities.AccountProfileDataBean;
import com.ibm.websphere.samples.daytrader.entities.HoldingDataBean;
import com.ibm.websphere.samples.daytrader.entities.OrderDataBean;
import com.ibm.websphere.samples.daytrader.entities.QuoteDataBean;
import com.ibm.websphere.samples.daytrader.interfaces.TradeServices;
import com.ibm.websphere.samples.daytrader.util.Log;

/**
 * TradeAppResource provides the web interface to Trade at /app
 * This replaces TradeAppServlet from Jakarta EE version
 * 
 * MIGRATION NOTE: In Quarkus, we use JAX-RS instead of Servlets.
 * The JSP pages are replaced with simple HTML responses.
 * For a production app, you would use Qute templates.
 */
@Path("/app")
@Produces(MediaType.TEXT_HTML)
public class TradeAppResource {

    @Inject
    TradeServices tradeService;

    // Simple session simulation (in production, use proper session management)
    private static final ThreadLocal<String> currentUser = new ThreadLocal<>();

    @GET
    public Response welcome(@QueryParam("action") String action,
                           @QueryParam("uid") String uid,
                           @QueryParam("symbols") String symbols) {
        
        if (action == null || action.isEmpty()) {
            return Response.ok(buildWelcomePage("")).build();
        }
        
        switch (action) {
            case "logout":
                currentUser.remove();
                return Response.ok(buildWelcomePage("You have been logged out.")).build();
            case "quotes":
                return handleQuotes(symbols);
            case "home":
                if (currentUser.get() != null) {
                    return handleHome(currentUser.get());
                }
                return Response.ok(buildWelcomePage("Please log in first.")).build();
            case "portfolio":
                if (currentUser.get() != null) {
                    return handlePortfolio(currentUser.get());
                }
                return Response.ok(buildWelcomePage("Please log in first.")).build();
            case "account":
                if (currentUser.get() != null) {
                    return handleAccount(currentUser.get());
                }
                return Response.ok(buildWelcomePage("Please log in first.")).build();
            default:
                return Response.ok(buildWelcomePage("Unknown action: " + action)).build();
        }
    }

    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response handlePost(@FormParam("action") String action,
                               @FormParam("uid") String uid,
                               @FormParam("passwd") String passwd,
                               @FormParam("symbol") String symbol,
                               @FormParam("quantity") String quantity,
                               @FormParam("holdingID") String holdingID,
                               @FormParam("symbols") String symbols) {
        
        if (action == null) {
            return Response.ok(buildWelcomePage("")).build();
        }

        switch (action) {
            case "login":
                return handleLogin(uid, passwd);
            case "buy":
                return handleBuy(currentUser.get(), symbol, quantity);
            case "sell":
                return handleSell(currentUser.get(), holdingID);
            case "quotes":
                return handleQuotes(symbols);
            default:
                return welcome(action, uid, symbols);
        }
    }

    private Response handleLogin(String uid, String passwd) {
        try {
            AccountDataBean account = tradeService.login(uid, passwd);
            if (account != null) {
                currentUser.set(uid);
                return handleHome(uid);
            } else {
                return Response.ok(buildWelcomePage("Login failed. Invalid username or password.")).build();
            }
        } catch (Exception e) {
            Log.error(e, "Login failed for user: " + uid);
            return Response.ok(buildWelcomePage("Login error: " + e.getMessage())).build();
        }
    }

    private Response handleHome(String userID) {
        try {
            AccountDataBean account = tradeService.getAccountData(userID);
            Collection<HoldingDataBean> holdings = tradeService.getHoldings(userID);
            
            StringBuilder html = new StringBuilder();
            html.append(getHeader(userID));
            html.append("<h2>Welcome, ").append(userID).append("!</h2>");
            html.append("<h3>Account Summary</h3>");
            html.append("<table border='1' cellpadding='5'>");
            html.append("<tr><td>Account ID:</td><td>").append(account.getAccountID()).append("</td></tr>");
            html.append("<tr><td>Balance:</td><td>$").append(account.getBalance()).append("</td></tr>");
            html.append("<tr><td>Open Balance:</td><td>$").append(account.getOpenBalance()).append("</td></tr>");
            html.append("<tr><td>Holdings:</td><td>").append(holdings.size()).append("</td></tr>");
            html.append("</table>");
            
            // Market Summary
            html.append("<h3>Quick Quote</h3>");
            html.append("<form method='post' action='/rest/app'>");
            html.append("<input type='hidden' name='action' value='quotes'/>");
            html.append("Symbols: <input type='text' name='symbols' placeholder='s:0,s:1,s:2'/>");
            html.append("<input type='submit' value='Get Quotes'/>");
            html.append("</form>");
            
            html.append(getFooter());
            return Response.ok(html.toString()).build();
        } catch (Exception e) {
            Log.error(e, "Error getting home page for user: " + userID);
            return Response.ok(buildWelcomePage("Error: " + e.getMessage())).build();
        }
    }

    private Response handlePortfolio(String userID) {
        try {
            Collection<HoldingDataBean> holdings = tradeService.getHoldings(userID);
            
            StringBuilder html = new StringBuilder();
            html.append(getHeader(userID));
            html.append("<h2>Portfolio for ").append(userID).append("</h2>");
            html.append("<table border='1' cellpadding='5'>");
            html.append("<tr><th>Holding ID</th><th>Symbol</th><th>Quantity</th><th>Purchase Price</th><th>Purchase Date</th><th>Action</th></tr>");
            
            for (HoldingDataBean holding : holdings) {
                html.append("<tr>");
                html.append("<td>").append(holding.getHoldingID()).append("</td>");
                html.append("<td>").append(holding.getQuoteID()).append("</td>");
                html.append("<td>").append(holding.getQuantity()).append("</td>");
                html.append("<td>$").append(holding.getPurchasePrice()).append("</td>");
                html.append("<td>").append(holding.getPurchaseDate()).append("</td>");
                html.append("<td>");
                html.append("<form method='post' action='/rest/app' style='display:inline'>");
                html.append("<input type='hidden' name='action' value='sell'/>");
                html.append("<input type='hidden' name='holdingID' value='").append(holding.getHoldingID()).append("'/>");
                html.append("<input type='submit' value='Sell'/>");
                html.append("</form>");
                html.append("</td>");
                html.append("</tr>");
            }
            html.append("</table>");
            
            // Buy form
            html.append("<h3>Buy Stock</h3>");
            html.append("<form method='post' action='/rest/app'>");
            html.append("<input type='hidden' name='action' value='buy'/>");
            html.append("Symbol: <input type='text' name='symbol' placeholder='s:0'/> ");
            html.append("Quantity: <input type='text' name='quantity' placeholder='100'/> ");
            html.append("<input type='submit' value='Buy'/>");
            html.append("</form>");
            
            html.append(getFooter());
            return Response.ok(html.toString()).build();
        } catch (Exception e) {
            Log.error(e, "Error getting portfolio for user: " + userID);
            return Response.ok(buildWelcomePage("Error: " + e.getMessage())).build();
        }
    }

    private Response handleAccount(String userID) {
        try {
            AccountDataBean account = tradeService.getAccountData(userID);
            AccountProfileDataBean profile = tradeService.getAccountProfileData(userID);
            
            StringBuilder html = new StringBuilder();
            html.append(getHeader(userID));
            html.append("<h2>Account Details for ").append(userID).append("</h2>");
            html.append("<table border='1' cellpadding='5'>");
            html.append("<tr><td>Account ID:</td><td>").append(account.getAccountID()).append("</td></tr>");
            html.append("<tr><td>Balance:</td><td>$").append(account.getBalance()).append("</td></tr>");
            html.append("<tr><td>Open Balance:</td><td>$").append(account.getOpenBalance()).append("</td></tr>");
            html.append("<tr><td>Creation Date:</td><td>").append(account.getCreationDate()).append("</td></tr>");
            html.append("<tr><td>Last Login:</td><td>").append(account.getLastLogin()).append("</td></tr>");
            html.append("<tr><td>Login Count:</td><td>").append(account.getLoginCount()).append("</td></tr>");
            html.append("<tr><td>Full Name:</td><td>").append(profile.getFullName()).append("</td></tr>");
            html.append("<tr><td>Email:</td><td>").append(profile.getEmail()).append("</td></tr>");
            html.append("<tr><td>Address:</td><td>").append(profile.getAddress()).append("</td></tr>");
            html.append("</table>");
            html.append(getFooter());
            return Response.ok(html.toString()).build();
        } catch (Exception e) {
            Log.error(e, "Error getting account for user: " + userID);
            return Response.ok(buildWelcomePage("Error: " + e.getMessage())).build();
        }
    }

    private Response handleQuotes(String symbols) {
        StringBuilder html = new StringBuilder();
        String user = currentUser.get();
        html.append(getHeader(user));
        html.append("<h2>Stock Quotes</h2>");
        
        if (symbols == null || symbols.isEmpty()) {
            symbols = "s:0,s:1,s:2,s:3,s:4";
        }
        
        html.append("<table border='1' cellpadding='5'>");
        html.append("<tr><th>Symbol</th><th>Company</th><th>Price</th><th>Change</th><th>Volume</th>");
        if (user != null) {
            html.append("<th>Action</th>");
        }
        html.append("</tr>");
        
        try {
            String[] symbolArray = symbols.split(",");
            for (String symbol : symbolArray) {
                symbol = symbol.trim();
                QuoteDataBean quote = tradeService.getQuote(symbol);
                if (quote != null) {
                    html.append("<tr>");
                    html.append("<td>").append(quote.getSymbol()).append("</td>");
                    html.append("<td>").append(quote.getCompanyName()).append("</td>");
                    html.append("<td>$").append(quote.getPrice()).append("</td>");
                    html.append("<td>").append(quote.getChange()).append("</td>");
                    html.append("<td>").append(quote.getVolume()).append("</td>");
                    if (user != null) {
                        html.append("<td>");
                        html.append("<form method='post' action='/rest/app' style='display:inline'>");
                        html.append("<input type='hidden' name='action' value='buy'/>");
                        html.append("<input type='hidden' name='symbol' value='").append(symbol).append("'/>");
                        html.append("<input type='text' name='quantity' value='100' size='5'/>");
                        html.append("<input type='submit' value='Buy'/>");
                        html.append("</form>");
                        html.append("</td>");
                    }
                    html.append("</tr>");
                }
            }
        } catch (Exception e) {
            html.append("<tr><td colspan='6'>Error: ").append(e.getMessage()).append("</td></tr>");
        }
        html.append("</table>");
        
        html.append("<h3>Get More Quotes</h3>");
        html.append("<form method='post' action='/rest/app'>");
        html.append("<input type='hidden' name='action' value='quotes'/>");
        html.append("Symbols (comma-separated): <input type='text' name='symbols' value='").append(symbols).append("'/>");
        html.append("<input type='submit' value='Get Quotes'/>");
        html.append("</form>");
        
        html.append(getFooter());
        return Response.ok(html.toString()).build();
    }

    private Response handleBuy(String userID, String symbol, String quantity) {
        if (userID == null) {
            return Response.ok(buildWelcomePage("Please log in first.")).build();
        }
        
        try {
            double qty = Double.parseDouble(quantity);
            OrderDataBean order = tradeService.buy(userID, symbol, qty, 0);
            
            StringBuilder html = new StringBuilder();
            html.append(getHeader(userID));
            html.append("<h2>Order Confirmation</h2>");
            html.append("<p>Buy order placed successfully!</p>");
            html.append("<table border='1' cellpadding='5'>");
            html.append("<tr><td>Order ID:</td><td>").append(order.getOrderID()).append("</td></tr>");
            html.append("<tr><td>Symbol:</td><td>").append(symbol).append("</td></tr>");
            html.append("<tr><td>Quantity:</td><td>").append(quantity).append("</td></tr>");
            html.append("<tr><td>Order Type:</td><td>").append(order.getOrderType()).append("</td></tr>");
            html.append("<tr><td>Order Status:</td><td>").append(order.getOrderStatus()).append("</td></tr>");
            html.append("<tr><td>Price:</td><td>$").append(order.getPrice()).append("</td></tr>");
            html.append("</table>");
            html.append("<p><a href='/rest/app?action=portfolio'>View Portfolio</a></p>");
            html.append(getFooter());
            return Response.ok(html.toString()).build();
        } catch (Exception e) {
            Log.error(e, "Error buying stock for user: " + userID);
            return Response.ok(buildWelcomePage("Buy error: " + e.getMessage())).build();
        }
    }

    private Response handleSell(String userID, String holdingID) {
        if (userID == null) {
            return Response.ok(buildWelcomePage("Please log in first.")).build();
        }
        
        try {
            int hid = Integer.parseInt(holdingID);
            OrderDataBean order = tradeService.sell(userID, hid, 0);
            
            StringBuilder html = new StringBuilder();
            html.append(getHeader(userID));
            html.append("<h2>Order Confirmation</h2>");
            html.append("<p>Sell order placed successfully!</p>");
            html.append("<table border='1' cellpadding='5'>");
            html.append("<tr><td>Order ID:</td><td>").append(order.getOrderID()).append("</td></tr>");
            html.append("<tr><td>Order Type:</td><td>").append(order.getOrderType()).append("</td></tr>");
            html.append("<tr><td>Order Status:</td><td>").append(order.getOrderStatus()).append("</td></tr>");
            html.append("<tr><td>Quantity:</td><td>").append(order.getQuantity()).append("</td></tr>");
            html.append("<tr><td>Price:</td><td>$").append(order.getPrice()).append("</td></tr>");
            html.append("</table>");
            html.append("<p><a href='/rest/app?action=portfolio'>View Portfolio</a></p>");
            html.append(getFooter());
            return Response.ok(html.toString()).build();
        } catch (Exception e) {
            Log.error(e, "Error selling stock for user: " + userID);
            return Response.ok(buildWelcomePage("Sell error: " + e.getMessage())).build();
        }
    }

    private String buildWelcomePage(String message) {
        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html>");
        html.append("<html><head><title>DayTrader - Welcome</title>");
        html.append("<link rel='stylesheet' href='/style.css' type='text/css'/>");
        html.append("</head><body>");
        html.append("<h1>DayTrader - Welcome</h1>");
        
        if (message != null && !message.isEmpty()) {
            html.append("<p style='color:red;'><b>").append(message).append("</b></p>");
        }
        
        html.append("<h3>Login</h3>");
        html.append("<form method='post' action='/rest/app'>");
        html.append("<input type='hidden' name='action' value='login'/>");
        html.append("<table>");
        html.append("<tr><td>Username:</td><td><input type='text' name='uid' value='uid:0'/></td></tr>");
        html.append("<tr><td>Password:</td><td><input type='password' name='passwd' value='xxx'/></td></tr>");
        html.append("<tr><td colspan='2'><input type='submit' value='Login'/></td></tr>");
        html.append("</table>");
        html.append("</form>");
        
        html.append("<h3>View Quotes (No Login Required)</h3>");
        html.append("<form method='post' action='/rest/app'>");
        html.append("<input type='hidden' name='action' value='quotes'/>");
        html.append("Symbols: <input type='text' name='symbols' value='s:0,s:1,s:2'/>");
        html.append("<input type='submit' value='Get Quotes'/>");
        html.append("</form>");
        
        html.append("</body></html>");
        return html.toString();
    }

    private String getHeader(String userID) {
        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html>");
        html.append("<html><head><title>DayTrader</title>");
        html.append("<link rel='stylesheet' href='/style.css' type='text/css'/>");
        html.append("</head><body>");
        html.append("<div style='background:#003366; color:white; padding:10px;'>");
        html.append("<img src='/images/dayTraderLogo.gif' alt='DayTrader'/>");
        if (userID != null) {
            html.append(" | Logged in as: <b>").append(userID).append("</b>");
            html.append(" | <a href='/rest/app?action=home' style='color:white;'>Home</a>");
            html.append(" | <a href='/rest/app?action=portfolio' style='color:white;'>Portfolio</a>");
            html.append(" | <a href='/rest/app?action=account' style='color:white;'>Account</a>");
            html.append(" | <a href='/rest/app?action=quotes' style='color:white;'>Quotes</a>");
            html.append(" | <a href='/rest/app?action=logout' style='color:white;'>Logout</a>");
        }
        html.append("</div>");
        return html.toString();
    }

    private String getFooter() {
        return "<hr/><p><small>DayTrader - Quarkus Edition</small></p></body></html>";
    }
}
