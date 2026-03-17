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

import java.util.ArrayList;
import java.util.List;

// MIGRATION: javax.* -> jakarta.*
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

import com.ibm.websphere.samples.daytrader.entities.QuoteDataBean;
import com.ibm.websphere.samples.daytrader.interfaces.TradeServices;

/**
 * MIGRATION NOTES:
 * 
 * 1. Package changed: javax.ws.rs -> jakarta.ws.rs
 * 2. Simplified dependency injection - no longer using Instance<> with qualifiers
 * 3. @ApplicationPath is not needed in Quarkus - configure in application.properties
 */
@Path("quotes")
@RequestScoped
public class QuoteResource {

    @Inject
    TradeServices tradeService;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{symbols}")
    public List<QuoteDataBean> quotesGet(@PathParam("symbols") String symbols) {
        return getQuotes(symbols);
    }

    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_JSON)
    public List<QuoteDataBean> quotesPost(@FormParam("symbols") String symbols) {
        return getQuotes(symbols);
    }

    private List<QuoteDataBean> getQuotes(String symbols) {
        ArrayList<QuoteDataBean> quoteDataBeans = new ArrayList<>();

        try {
            String[] symbolsSplit = symbols.split(",");
            for (String symbol : symbolsSplit) {
                QuoteDataBean quoteData = tradeService.getQuote(symbol.trim());
                if (quoteData != null) {
                    quoteDataBeans.add(quoteData);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return quoteDataBeans;
    }
}
