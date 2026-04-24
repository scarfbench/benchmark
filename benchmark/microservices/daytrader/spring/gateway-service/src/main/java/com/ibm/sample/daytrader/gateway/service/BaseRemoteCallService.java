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

package com.ibm.sample.daytrader.gateway.service;

import javax.transaction.NotSupportedException;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.ClientErrorException;
import javax.ws.rs.ForbiddenException;
import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.NotAcceptableException;
import javax.ws.rs.NotAllowedException;
import javax.ws.rs.NotAuthorizedException;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.RedirectionException;
import javax.ws.rs.ServerErrorException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.client.ClientProperties;
import org.glassfish.jersey.client.HttpUrlConnectorProvider;

//
//Don't do any logging from the base remote call service unless you send it to its own logger;
//otherwise the log messages will be written to the integration test and to the server log and
//will cause server messages to be lost. For debugging, you will need the server log messages.

public class BaseRemoteCallService {
	
    public static String invokeEndpoint(String url, String method, String body) throws Exception
    {
    	return invokeEndpoint(url, method, body, -1);
    }
    
    public static String invokeEndpoint(String url, String method, String body, int connTimeOut) throws Exception
    {       	
   		Response  response = sendRequest(url, method, body, connTimeOut);
   		int responseCode = response.getStatus();
   		
   		// switch statement 
   		switch(responseCode)
   		{
   		   	case 400 :
   	   		   	// Malformed message 
   	   			throw new BadRequestException("Malformed message from : " + url);
   		   
   		   	case 401 :
   		   		// Authentication failure
   	   			throw new NotAuthorizedException("Authentication failure from : " + url);

      	   case 403 :
      		   // Not permitted to access
  	   			throw new ForbiddenException("Not permitted to access from : " + url);
      		   
   		  	case 404 :
   		  		// Couldn't find resource
   	   			throw new NotFoundException("Couldn't find resource from : " + url);
   		  		
   		  	case 405 :
       		    // HTTP method not supported
   		  		throw new NotAllowedException("HTTP method not supported from : " + url);

   		  	case 406 :
     		      // Client media type requested not supported
   	   			throw new NotAcceptableException("Client accepts media type not supported from : " + url);
     		      
   		  	case 415:
   		  		// Client posted media type not supported
   	   			throw new NotSupportedException("Client produces media type not supported from : " + url);
     		      
       		case 500 :
       		      // General server error
   	   			throw new InternalServerErrorException("General server error from : " + url);

       		case 503 :
     		      // Server is temporarily unavailable or busy
   	   			throw new NotAuthorizedException("Server is temporarily unavailable or busy from : " + url);
       		      
       		default :
       			if ( responseCode >= 300 && responseCode <= 399 )
       			{
       				throw new RedirectionException("A request redirection from : " + url, responseCode, null);
       			}
       			if ( responseCode >= 400 && responseCode <= 499 )
       			{
       				throw new ClientErrorException("A client request error from : " + url, responseCode);
       			}
       			if ( responseCode >=500 && responseCode <= 599 )
       			{
       				throw new ServerErrorException("A server error from : " + url, responseCode);
       			}
   		}
   		
   		String responseEntity = response.readEntity(String.class);
   		response.close();
        return responseEntity;
    }

    public static Response sendRequest(String url, String method, String body, int connTimeOut) 
    {
    	// Jersey client doesn't support the Http PATCH method without this workaround
        Client client = ClientBuilder.newClient()
        		.property(HttpUrlConnectorProvider.SET_METHOD_WORKAROUND, true);
        
        if (connTimeOut > 0)
        {
        	client.property(ClientProperties.CONNECT_TIMEOUT, connTimeOut);
        }
        
        WebTarget target = client.target(url);
        Response response = target.request().method(method, Entity.json(body));
        return response;
    }
}
