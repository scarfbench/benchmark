/*
Copyright 2017- IBM Corp. All Rights Reserved.

SPDX-License-Identifier: Apache-2.0
*/

package com.acmeair.config;

import javax.inject.Inject;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

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
