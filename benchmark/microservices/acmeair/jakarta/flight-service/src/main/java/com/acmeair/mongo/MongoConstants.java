/*
Copyright 2017- IBM Corp. All Rights Reserved.

SPDX-License-Identifier: Apache-2.0
*/

package com.acmeair.mongo;

import com.acmeair.AcmeAirConstants;

public interface MongoConstants extends AcmeAirConstants {

  public static final String JNDI_NAME = "mongo/acmeairMongodb";
  public static final String KEY = "mongo";
  public static final String KEY_DESCRIPTION = "mongoDB implementation";

  public static final String HOSTNAME = "mongohostname";
  public static final String PORT = "mongoport";
  public static final String DATABASE = "mongodatabase";

}
