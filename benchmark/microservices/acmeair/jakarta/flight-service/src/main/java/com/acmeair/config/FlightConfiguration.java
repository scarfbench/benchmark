/*
Copyright 2017- IBM Corp. All Rights Reserved.

SPDX-License-Identifier: Apache-2.0
*/

package com.acmeair.config;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import com.acmeair.service.FlightService;

@Path("/config")
public class FlightConfiguration {

	Logger logger = Logger.getLogger(FlightConfiguration.class.getName());

	@Inject
	FlightService flightService;

	public FlightConfiguration() {
	}

	@GET
	@Path("/countFlights")
	@Produces(MediaType.APPLICATION_JSON)
	public String countFlights() {
		try {
			String count = flightService.countFlights().toString();
			return count;
		} catch (Exception e) {
			e.printStackTrace();
			return "-1";
		}
	}

	@GET
	@Path("/countFlightSegments")
	@Produces(MediaType.APPLICATION_JSON)
	public String countFlightSegments() {
		try {
			String count = flightService.countFlightSegments().toString();
			return count;
		} catch (Exception e) {
			e.printStackTrace();
			return "-1";
		}
	}

    @GET
    @Path("/countAirports")
    @Produces(MediaType.APPLICATION_JSON)
	public String countAirports() {
		try {
			String count = flightService.countAirports().toString();
			return count;
		} catch (Exception e) {
			e.printStackTrace();
			return "-1";
		}
	}

    @GET
    @Path("/activeDataService")
    @Produces(MediaType.APPLICATION_JSON)
	public String getActiveDataServiceInfo() {
		try {
			logger.fine("Get active Data Service info");
			return flightService.getServiceType();
		} catch (Exception e) {
			e.printStackTrace();
			return "Unknown";
		}
	}

    @GET
    @Path("/runtime")
    @Produces(MediaType.APPLICATION_JSON)
	public List<Map<String, String>> getRuntimeInfo() {
		List<Map<String, String>> list = new ArrayList<>();
		Map<String, String> map = new HashMap<String, String>();
		map.put("name", "Runtime");
		map.put("description", "Java");
		list.add(map);
		map.clear();

		map.put("name", "Version");
		map.put("description", System.getProperty("java.version"));
		list.add(map);
		map.clear();

		map.put("name", "Vendor");
		map.put("description", System.getProperty("java.vendor"));
		list.add(map);

		return list;
	}

	class ServiceData {
		public String name = "";
		public String description = "";
	}
}
