/*
 * Copyright (c), Eclipse Foundation, Inc. and its licensors.
 *
 * All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v1.0, which is available at
 * https://www.eclipse.org/org/documents/edl-v10.php
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */
package jakarta.tutorial.concurrency.jobs.client;

import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Response;

import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

/**
 * REST client interface for JobService using Quarkus REST Client
 */
@RegisterRestClient(configKey = "job-service")
@Path("/webapi/JobService")
public interface JobServiceClient {

    @POST
    @Path("/process")
    Response processJob(@QueryParam("jobID") int jobID,
            @HeaderParam("X-REST-API-Key") String apiKey);
}