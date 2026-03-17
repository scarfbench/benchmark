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

import org.springframework.stereotype.Component;
import org.springframework.http.ResponseEntity;

@Component
public class JobClient {
    private final JobServiceClient jobService;

    public JobClient(JobServiceClient jobService) {
        this.jobService = jobService;
    }

    public boolean submit(int jobID, String token) {
        ResponseEntity<String> resp = jobService.processJob(jobID, token);
        return resp.getStatusCode().is2xxSuccessful();
    }
}