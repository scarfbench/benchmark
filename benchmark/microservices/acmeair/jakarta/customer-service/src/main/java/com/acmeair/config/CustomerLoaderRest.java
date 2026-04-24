/*
Copyright 2017- IBM Corp. All Rights Reserved.

SPDX-License-Identifier: Apache-2.0
*/

package com.acmeair.config;

import jakarta.inject.Inject;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;

import com.acmeair.loader.CustomerLoader;

@Path("/loader")
public class CustomerLoaderRest {

    @Inject
	private CustomerLoader loader;

    @GET
	@Path("/query")
    @Produces(MediaType.TEXT_PLAIN)
	public String queryLoader() {
		String response = loader.queryLoader();
		return response;
	}

    @GET
    @Path("/load")
    @Produces(MediaType.TEXT_PLAIN)
	public String loadDb(@DefaultValue("-1") @QueryParam("numCustomers") long numCustomers) {
		String response = loader.loadCustomerDb(numCustomers);
		return response;
	}
}
