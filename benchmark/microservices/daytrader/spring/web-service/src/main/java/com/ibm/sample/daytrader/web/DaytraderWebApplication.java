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

package com.ibm.sample.daytrader.web;

import java.util.Arrays;

import org.apache.catalina.Context;
import org.apache.catalina.startup.Tomcat;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.context.annotation.Bean;

import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.embedded.tomcat.TomcatWebServer;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;


@ServletComponentScan(basePackages={"com.ibm.sample.daytrader"})
@SpringBootApplication
public class DaytraderWebApplication extends SpringBootServletInitializer {
	
	// Updated by on 2018-02-22
	//	- Each microservice has their own private database (datasource)

	@Override
	protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
		return application.sources(DaytraderWebApplication.class);
	}

	public static void main(String[] args) throws Exception {
		SpringApplication.run(DaytraderWebApplication.class, args);
	}

	@Bean
	public TomcatServletWebServerFactory tomcatFactory()
	{

		TomcatServletWebServerFactory factory = new TomcatServletWebServerFactory()
		{
			@Override
			protected TomcatWebServer getTomcatWebServer(Tomcat tomcat)
			{
				tomcat.enableNaming();
				return super.getTomcatWebServer(tomcat);
			}

			@Override
			protected void postProcessContext(Context context) {}
		};

		factory.setTomcatContextCustomizers(Arrays.asList(context -> {
			// Set the cookie properties to make sure the browser will send them over the
			// in-secure connection (ie. http) between the browser and the kubectl proxy.
			context.setUseHttpOnly(true);
			context.setSessionCookiePath("/");
		}));
		return factory;
	}
}

