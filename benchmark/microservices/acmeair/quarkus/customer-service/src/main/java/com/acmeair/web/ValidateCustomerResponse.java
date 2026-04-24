/*
Copyright 2019- IBM Corp. All Rights Reserved.

SPDX-License-Identifier: Apache-2.0
*/

package com.acmeair.web;

import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection
public class ValidateCustomerResponse {
    public boolean validCustomer;
}
