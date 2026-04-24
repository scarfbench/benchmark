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

import com.acmeair.service.BookingService;

@Path("/config")
public class BookingConfiguration {

	Logger logger = Logger.getLogger(BookingConfiguration.class.getName());

	@Inject
	BookingService bookingService;

	/**
	 * Get numbers of bookings in the db.
	 */
    @GET
    @Path("/countBookings")
    @Produces(MediaType.APPLICATION_JSON)
	public String countBookings() {
		try {
			String count = bookingService.count().toString();
			return "" + count;
		} catch (Exception e) {
			e.printStackTrace();
			return "-1";
		}
	}

	/**
	 * Get active db impl.
	 */
    @GET
    @Path("/activeDataService")
    @Produces(MediaType.APPLICATION_JSON)
	public String getActiveDataServiceInfo() {
		try {
			logger.fine("Get active Data Service info");
			return bookingService.getServiceType();
		} catch (Exception e) {
			e.printStackTrace();
			return "Unknown";
		}
	}

	/**
	 * Get runtime info.
	 */
    @GET
    @Path("/runtime")
    @Produces(MediaType.APPLICATION_JSON)
	public List<Map<String, String>> getRuntimeInfo() {
		List<Map<String, String>> list = new ArrayList<>();
		Map<String, String> map = new HashMap<>();
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
}
