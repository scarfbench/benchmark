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

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Logger;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.MongoClientURI;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoDatabase;

public class ConnectionManager implements MongoConstants {

	private static AtomicReference<ConnectionManager> connectionManager = new AtomicReference<ConnectionManager>();

	private static final Logger logger = Logger.getLogger(ConnectionManager.class.getName());

	protected MongoClient mongoClient;
	protected MongoDatabase db;

	/**
	 * init mongo connection manager.
	 */
	public static ConnectionManager getConnectionManager() {
		if (connectionManager.get() == null) {
			synchronized (connectionManager) {
				if (connectionManager.get() == null) {
					connectionManager.set(new ConnectionManager());
				}
			}
		}
		return connectionManager.get();
	}

	private ConnectionManager() {

		// Set default client options, and then check if there is a properties file.
		String hostname = "localhost";
		int port = 27017;
		String dbname = "acmeair";
		String username = null;
		String password = null;

		Properties prop = new Properties();
		String acmeairProps = System.getenv("ACMEAIR_PROPERTIES");
		try {
			if (acmeairProps != null) {
				prop.load(new FileInputStream(acmeairProps));
			} else {
				prop.load(ConnectionManager.class.getResourceAsStream("/config.properties"));
				acmeairProps = "OK";
			}
		} catch (IOException ex) {
			logger.info("Properties file does not exist" + ex.getMessage());
			acmeairProps = null;
		}

		ServerAddress dbAddress = null;
		MongoClientOptions.Builder options = new MongoClientOptions.Builder();
		if (acmeairProps != null) {
			try {
				logger.info("Reading mongo.properties file");
				if (System.getenv("MONGO_HOST") != null) {
					hostname = System.getenv("MONGO_HOST");
				} else if (prop.containsKey("hostname")) {
					hostname = prop.getProperty("hostname");
				}
				if (System.getenv("MONGO_PORT") != null) {
					port = Integer.parseInt(System.getenv("MONGO_PORT"));
				} else if (prop.containsKey("port")) {
					port = Integer.parseInt(prop.getProperty("port"));
				}
				if (System.getenv("MONGO_DBNAME") != null) {
					dbname = System.getenv("MONGO_DBNAME");
				} else if (prop.containsKey("dbname")) {
					dbname = prop.getProperty("dbname");
				}
				if (prop.containsKey("username")) {
					username = prop.getProperty("username");
				}
				if (prop.containsKey("password")) {
					password = prop.getProperty("password");
				}
				if (prop.containsKey("connectionsPerHost")) {
					options.connectionsPerHost(Integer.parseInt(prop.getProperty("connectionsPerHost")));
				}
				if (prop.containsKey("minConnectionsPerHost")) {
					options.minConnectionsPerHost(Integer.parseInt(prop.getProperty("minConnectionsPerHost")));
				}
				if (prop.containsKey("maxWaitTime")) {
					options.maxWaitTime(Integer.parseInt(prop.getProperty("maxWaitTime")));
				}
				if (prop.containsKey("connectTimeout")) {
					options.connectTimeout(Integer.parseInt(prop.getProperty("connectTimeout")));
				}
				if (prop.containsKey("socketTimeout")) {
					options.socketTimeout(Integer.parseInt(prop.getProperty("socketTimeout")));
				}
				if (prop.containsKey("socketKeepAlive")) {
					options.socketKeepAlive(Boolean.parseBoolean(prop.getProperty("socketKeepAlive")));
				}
				if (prop.containsKey("sslEnabled")) {
					options.sslEnabled(Boolean.parseBoolean(prop.getProperty("sslEnabled")));
				}
				if (prop.containsKey("threadsAllowedToBlockForConnectionMultiplier")) {
					options.threadsAllowedToBlockForConnectionMultiplier(
							Integer.parseInt(prop.getProperty("threadsAllowedToBlockForConnectionMultiplier")));
				}

			} catch (Exception ioe) {
				logger.severe("Exception when trying to read from the " + "mongo.properties file" + ioe.getMessage());
			}
		}

		MongoClientOptions builtOptions = options.build();

		try {
			// Check if VCAP_SERVICES exist, and if it does, look up the url from the
			// credentials.
			String vcapJsonString = System.getenv("VCAP_SERVICES");
			if (vcapJsonString != null) {
				logger.info("Reading VCAP_SERVICES");

				ObjectMapper mapper = new ObjectMapper();
				JsonNode vcapServices = mapper.readTree(vcapJsonString);
				JsonNode mongoServiceArray = null;

				for (Entry<String, JsonNode> field : (Iterable<Entry<String, JsonNode>>) () -> vcapServices.fields()) {
					if (field.getKey().startsWith("mongo")) {
						mongoServiceArray = field.getValue();
						logger.info("Service Type : MongoLAB - " + field.getValue());
						break;
					}
					if (field.getKey().startsWith("user-provided")) {
						mongoServiceArray = field.getValue();
						logger.info("Service Type : MongoDB by Compost - " + field.getValue());
						break;
					}
				}

				if (mongoServiceArray == null) {
					logger.info("VCAP_SERVICES existed, but a MongoLAB or MongoDB by COMPOST "
							+ "service was not definied. Trying DB resource");
					// VCAP_SERVICES don't exist, so use the DB resource
					dbAddress = new ServerAddress(hostname, port);

					// If username & password exists, connect DB with username & password
					if ((username == null) || (password == null)) {
						mongoClient = new MongoClient(dbAddress, builtOptions);
					} else {
						List<MongoCredential> credentials = new ArrayList<>();
						credentials.add(MongoCredential.createCredential(username, dbname, password.toCharArray()));
						mongoClient = new MongoClient(dbAddress, credentials, builtOptions);
					}
				} else {
					JsonNode mongoService = mongoServiceArray.get(0);
					JsonNode credentials = mongoService.get("credentials");
					String url = credentials.get("url").asText();
					logger.fine("service url = " + url);
					MongoClientURI mongoUri = new MongoClientURI(url, options);
					mongoClient = new MongoClient(mongoUri);
					dbname = mongoUri.getDatabase();

				}
			} else {

				// VCAP_SERVICES don't exist, so use the DB resource
				dbAddress = new ServerAddress(hostname, port);

				// If username & password exists, connect DB with username & password
				if ((username == null) || (password == null)) {
					mongoClient = new MongoClient(dbAddress, builtOptions);
				} else {
					List<MongoCredential> credentials = new ArrayList<>();
					credentials.add(MongoCredential.createCredential(username, dbname, password.toCharArray()));
					mongoClient = new MongoClient(dbAddress, credentials, builtOptions);
				}
			}

			db = mongoClient.getDatabase(dbname);
			logger.info("#### Mongo DB Server " + mongoClient.getAddress().getHost() + " ####");
			logger.info("#### Mongo DB Port " + mongoClient.getAddress().getPort() + " ####");
			logger.info("#### Mongo DB is created with DB name " + dbname + " ####");
			logger.info("#### MongoClient Options ####");
			logger.info("maxConnectionsPerHost : " + builtOptions.getConnectionsPerHost());
			logger.info("minConnectionsPerHost : " + builtOptions.getMinConnectionsPerHost());
			logger.info("maxWaitTime : " + builtOptions.getMaxWaitTime());
			logger.info("connectTimeout : " + builtOptions.getConnectTimeout());
			logger.info("socketTimeout : " + builtOptions.getSocketTimeout());
			logger.info("socketKeepAlive : " + builtOptions.isSocketKeepAlive());
			logger.info("sslEnabled : " + builtOptions.isSslEnabled());
			logger.info("threadsAllowedToBlockForConnectionMultiplier : "
					+ builtOptions.getThreadsAllowedToBlockForConnectionMultiplier());
			logger.info("Complete List : " + builtOptions.toString());

		} catch (Exception e) {
			logger.severe("Caught Exception : " + e.getMessage());
		}

	}

	public MongoDatabase getDb() {
		return db;
	}
}
