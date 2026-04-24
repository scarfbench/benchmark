/*
Copyright 2019- IBM Corp. All Rights Reserved.

SPDX-License-Identifier: Apache-2.0
*/

package com.acmeair.securityutils;

import java.io.IOException;
import java.util.Map;

import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.ext.Provider;

import com.acmeair.web.AuthServiceRest;

@Provider
public class CookieInterceptor implements ContainerResponseFilter {
	@Override
	public void filter(ContainerRequestContext request, ContainerResponseContext response) throws IOException {

      if(!request.getUriInfo().getPath().equals("/login")) {
          return;
      }
      if(response.getStatus() != 200) {
          return;
      }

      @SuppressWarnings("unchecked")
      Map<String, String> modelMap = (Map<String, String>)response.getEntity();
      String token = (String) modelMap.getOrDefault("token", "");
      String login = (String) modelMap.getOrDefault("login", "");

      MultivaluedMap<String, Object> resHeaders = response.getHeaders();
      resHeaders.add("Set-Cookie", AuthServiceRest.JWT_COOKIE_NAME + "=" + token + "; Path=/");
      resHeaders.add("Set-Cookie", AuthServiceRest.USER_COOKIE_NAME + "=" + login + "; Path=/");

      // Rewrite response entity here
      response.setEntity("logged in", null, MediaType.TEXT_PLAIN_TYPE);
	}
}
