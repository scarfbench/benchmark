/*
Copyright 2013- IBM Corp. All Rights Reserved.

SPDX-License-Identifier: Apache-2.0
*/

package com.acmeair.service;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class KeyGenerator {

  public Object generate() {
    return java.util.UUID.randomUUID().toString();
  }
}
