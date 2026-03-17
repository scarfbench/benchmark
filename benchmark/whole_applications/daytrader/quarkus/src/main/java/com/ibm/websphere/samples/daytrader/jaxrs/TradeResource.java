/**
 * (C) Copyright IBM Corporation 2019.
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
package com.ibm.websphere.samples.daytrader.jaxrs;

import java.math.BigDecimal;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.FormParam;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import com.ibm.websphere.samples.daytrader.beans.MarketSummaryDataBean;
import com.ibm.websphere.samples.daytrader.entities.AccountDataBean;
import com.ibm.websphere.samples.daytrader.entities.AccountProfileDataBean;
import com.ibm.websphere.samples.daytrader.entities.OrderDataBean;
import com.ibm.websphere.samples.daytrader.interfaces.TradeServices;
import com.ibm.websphere.samples.daytrader.util.TradeConfig;

/**
 * Main Trade REST API resource.
 * Provides REST endpoints for core trading operations.
 */
@Path("trade")
@RequestScoped
@Produces(MediaType.APPLICATION_JSON)
public class TradeResource {

    @Inject
    TradeServices tradeService;

    @GET
    @Path("/market")
    public Response getMarketSummary() {
        try {
            MarketSummaryDataBean summary = tradeService.getMarketSummary();
            return Response.ok(summary).build();
        } catch (Exception e) {
            return Response.serverError().entity(e.getMessage()).build();
        }
    }

    @POST
    @Path("/login")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response login(@FormParam("userID") String userID, 
                         @FormParam("password") String password) {
        try {
            AccountDataBean account = tradeService.login(userID, password);
            return Response.ok(account).build();
        } catch (Exception e) {
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity(e.getMessage()).build();
        }
    }

    @POST
    @Path("/logout/{userID}")
    public Response logout(@PathParam("userID") String userID) {
        try {
            tradeService.logout(userID);
            return Response.ok().build();
        } catch (Exception e) {
            return Response.serverError().entity(e.getMessage()).build();
        }
    }

    @GET
    @Path("/account/{userID}")
    public Response getAccount(@PathParam("userID") String userID) {
        try {
            AccountDataBean account = tradeService.getAccountData(userID);
            if (account == null) {
                return Response.status(Response.Status.NOT_FOUND).build();
            }
            return Response.ok(account).build();
        } catch (Exception e) {
            return Response.serverError().entity(e.getMessage()).build();
        }
    }

    @GET
    @Path("/account/{userID}/profile")
    public Response getAccountProfile(@PathParam("userID") String userID) {
        try {
            AccountProfileDataBean profile = tradeService.getAccountProfileData(userID);
            if (profile == null) {
                return Response.status(Response.Status.NOT_FOUND).build();
            }
            return Response.ok(profile).build();
        } catch (Exception e) {
            return Response.serverError().entity(e.getMessage()).build();
        }
    }

    @GET
    @Path("/account/{userID}/holdings")
    public Response getHoldings(@PathParam("userID") String userID) {
        try {
            return Response.ok(tradeService.getHoldings(userID)).build();
        } catch (Exception e) {
            return Response.serverError().entity(e.getMessage()).build();
        }
    }

    @GET
    @Path("/account/{userID}/orders")
    public Response getOrders(@PathParam("userID") String userID) {
        try {
            return Response.ok(tradeService.getOrders(userID)).build();
        } catch (Exception e) {
            return Response.serverError().entity(e.getMessage()).build();
        }
    }

    @POST
    @Path("/buy")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response buy(@FormParam("userID") String userID,
                       @FormParam("symbol") String symbol,
                       @FormParam("quantity") double quantity) {
        try {
            OrderDataBean order = tradeService.buy(userID, symbol, quantity, 
                    TradeConfig.getOrderProcessingMode());
            return Response.ok(order).build();
        } catch (Exception e) {
            return Response.serverError().entity(e.getMessage()).build();
        }
    }

    @POST
    @Path("/sell")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response sell(@FormParam("userID") String userID,
                        @FormParam("holdingID") Integer holdingID) {
        try {
            OrderDataBean order = tradeService.sell(userID, holdingID, 
                    TradeConfig.getOrderProcessingMode());
            return Response.ok(order).build();
        } catch (Exception e) {
            return Response.serverError().entity(e.getMessage()).build();
        }
    }

    @POST
    @Path("/register")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response register(@FormParam("userID") String userID,
                            @FormParam("password") String password,
                            @FormParam("fullname") String fullname,
                            @FormParam("address") String address,
                            @FormParam("email") String email,
                            @FormParam("creditcard") String creditcard,
                            @FormParam("openBalance") String openBalance) {
        try {
            BigDecimal balance = new BigDecimal(openBalance);
            AccountDataBean account = tradeService.register(userID, password, fullname,
                    address, email, creditcard, balance);
            return Response.ok(account).build();
        } catch (Exception e) {
            return Response.serverError().entity(e.getMessage()).build();
        }
    }
}
