/**
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.ibm.sample.daytrader.portfolios;

import org.apache.catalina.Context;
import org.apache.catalina.startup.Tomcat;

import org.apache.tomcat.util.descriptor.web.ContextResource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.embedded.tomcat.TomcatWebServer;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.Bean;

import javax.sql.DataSource;


@ServletComponentScan(basePackages={"com.ibm.sample.daytrader.portfolios"})
@SpringBootApplication
public class PortfoliosApplication extends SpringBootServletInitializer {

	//  Configure database environment
	private static String driverClassName;
	private static String url;
	private static String username;
	private static String password;
	@Value("${database.driver}")
	public void setDriverClassName(String dbDriver) {
		driverClassName = dbDriver;
	}
	@Value("${database.url}")
	public void setUrl(String dbUrl) {
		url = dbUrl;
	}
	@Value("${database.username}")
	public void setUsername(String dbUname) {
		username = dbUname;
	}
	@Value("${database.password}")
	public void setPassword(String pass) {
		password = pass;
	}

	@Override
	protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
		return application.sources(PortfoliosApplication.class);
	}

	public static void main(String[] args) throws Exception {
		SpringApplication.run(PortfoliosApplication.class, args);
	}

	@Bean
	public TomcatServletWebServerFactory tomcatFactory()
	{
		return new TomcatServletWebServerFactory()
		{
			@Override
			protected TomcatWebServer getTomcatWebServer(Tomcat tomcat)
			{
				tomcat.enableNaming();
				return super.getTomcatWebServer(tomcat);
			}

			@Override
			protected void postProcessContext(Context context)
			{
				//
				// Portfolios Data Source
				//
				ContextResource portfoliosDataSource = new ContextResource();
				portfoliosDataSource.setName("jdbc/PortfoliosDataSource");
				portfoliosDataSource.setAuth("Container");
				portfoliosDataSource.setType(DataSource.class.getName());
				// Set Database Properties
				portfoliosDataSource.setProperty("driverClassName", driverClassName);
				portfoliosDataSource.setProperty("url", url);
				portfoliosDataSource.setProperty("username", username);
				portfoliosDataSource.setProperty("password", password);
				portfoliosDataSource.setProperty("maxActive", "100");
				portfoliosDataSource.setProperty("maxIdle", "30");
				portfoliosDataSource.setProperty("maxWait", "10000");
				context.getNamingResources().addResource(portfoliosDataSource);
			}
		};
	}
}

