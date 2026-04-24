/*
Copyright 2017- IBM Corp. All Rights Reserved.

SPDX-License-Identifier: Apache-2.0
*/

package com.acmeair.config;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

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
