/*******************************************************************************
* Copyright (c) 2017 IBM Corp.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*******************************************************************************/

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
