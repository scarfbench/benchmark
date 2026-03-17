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
package jakarta.tutorial.concurrency.jobs.service;

import java.util.UUID;
import java.util.concurrent.RejectedExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.microprofile.context.ManagedExecutor;
import jakarta.inject.Inject;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Response;

@ApplicationScoped
@Path("/JobService")
public class JobService {

    private static final Logger logger = Logger.getLogger(JobService.class.getCanonicalName());
    private static final String API_TOKEN_HEADER = "X-REST-API-Key";

    @Inject @High
    ManagedExecutor highPrioExecutor;

    @Inject @Low
    ManagedExecutor lowPrioExecutor;

    @Inject
    TokenStore tokenStore;

    @GET
    @Path("/token")
    public Response getToken() {
        final String token = "123X5-" + UUID.randomUUID();
        tokenStore.put(token);
        return Response.status(200).entity(token).build();
    }

    @POST
    @Path("/process")
    public Response process(@HeaderParam(API_TOKEN_HEADER) String token,
                            @QueryParam("jobID") int jobID) {
        try {
            if (token != null && tokenStore.isValid(token)) {
                logger.info("Token accepted. Execution with high priority.");
                highPrioExecutor.execute(new JobTask("HIGH-" + jobID));
            } else {
                logger.log(Level.INFO, "Invalid or missing token! {0}", token);
                lowPrioExecutor.execute(new JobTask("LOW-" + jobID));
            }
        } catch (RejectedExecutionException ree) {
            return Response.status(Response.Status.SERVICE_UNAVAILABLE)
                    .entity("Job " + jobID + " NOT submitted. " + ree.getMessage()).build();
        }
        return Response.ok("Job " + jobID + " successfully submitted.").build();
    }

    static class JobTask implements Runnable {
        private static final Logger logger = Logger.getLogger(JobTask.class.getName());
        private final String jobID;
        private final int JOB_EXECUTION_TIME = 10_000;

        JobTask(String id) { this.jobID = id; }

        @Override public void run() {
            try {
                logger.log(Level.INFO, "Task started {0}", jobID);
                Thread.sleep(JOB_EXECUTION_TIME);
                logger.log(Level.INFO, "Task finished {0}", jobID);
            } catch (InterruptedException ex) {
                logger.log(Level.SEVERE, null, ex);
                Thread.currentThread().interrupt();
            }
        }
    }
}