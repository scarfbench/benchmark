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

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.acmeair.service.FlightService;

@Path("/config")
public class FlightConfiguration {

	Logger logger = Logger.getLogger(FlightConfiguration.class.getName());

	@Inject
	FlightService flightService;

	public FlightConfiguration() {
	}

	/**
	 * Get the number of flights in the db.
	 */
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

	/**
	 * Get the number of flight segments in the db.
	 */
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

	/**
	 * Get the number of airports in the db.
	 */
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

	/**
	 * Get the database type.
	 */
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

	/**
	 * Get the runtime info.
	 */
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
