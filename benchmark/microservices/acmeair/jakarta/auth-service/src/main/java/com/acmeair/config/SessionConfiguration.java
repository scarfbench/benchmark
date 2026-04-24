/*
Copyright 2017- IBM Corp. All Rights Reserved.

SPDX-License-Identifier: Apache-2.0
*/

package com.acmeair.config;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;


@Path("/config")
public class SessionConfiguration {

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
}
