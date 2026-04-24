/*
Copyright 2017- IBM Corp. All Rights Reserved.

SPDX-License-Identifier: Apache-2.0
*/

package com.acmeair.config;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;


import com.acmeair.loader.FlightLoader;

@Path("/loader")
public class FlightLoaderRest {

    @Inject
	private FlightLoader loader;

    @GET
    @Path("/load")
    @Produces(MediaType.TEXT_PLAIN)
	public String loadDb() {
		String response = loader.loadFlightDb();
		return response;
	}
}
