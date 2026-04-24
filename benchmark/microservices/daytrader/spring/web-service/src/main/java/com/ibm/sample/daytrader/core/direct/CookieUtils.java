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

package com.ibm.sample.daytrader.core.direct;

import javax.servlet.*;
import javax.servlet.http.*;

public class CookieUtils {
		  
	/**
	 * Encode a cookie as per RFC 2109.  The resulting string 
	 * can be used as the value for a <code>Set-Cookie</code> 
	 * header.
	 *
	 * @param cookie The cookie to encode.
	 * @return A string following RFC 2109.
	 */
	public static String encodeCookie(Cookie cookie) 
	{
	    StringBuffer buf = new StringBuffer( cookie.getName() );
	    buf.append("=");
	    buf.append(cookie.getValue());

	    if (cookie.getComment() != null) {
	        buf.append("; Comment=\"");
	        buf.append(cookie.getComment());
	        buf.append("\"");
	    }

	    if (cookie.getDomain() != null) {
	        buf.append("; Domain=\"");
	        buf.append(cookie.getDomain());
	        buf.append("\"");
	    }

	    long age = cookie.getMaxAge();
	    if (cookie.getMaxAge() >= 0) {
	        buf.append("; Max-Age=\"");
	        buf.append(cookie.getMaxAge());
	        buf.append("\"");
	    }

	    if (cookie.getPath() != null) {
	        buf.append("; Path=\"");
	        buf.append(cookie.getPath());
	        buf.append("\"");
	    }

	    if (cookie.getSecure()) {
	        buf.append("; Secure");
	    }

	    if (cookie.getVersion() > 0) {
	        buf.append("; Version=\"");
	        buf.append(cookie.getVersion());
	        buf.append("\"");
	    }

	    return (buf.toString());
	}
	
	/*
	 * @param cookies The cookies
	 * @param name The cookie's name.
	 * @return The cookie's value.
	 */
	public static String getCookieValue( Cookie[] cookies, String name ) 
	{
		String value = null;
		if (cookies != null)
		{
			for(Cookie cookie : cookies)
			{	
				if (name.equals(cookie.getName()))
				{   
					value = cookie.getValue();
					break;
				}
    		}
    	}
		return value;
    }
	
	/*
	 * Set the cookie properties to make sure the browser will send them over the
     * in-secure connection (ie. http) between the browser and the kubectl proxy. 
	 */
	public static Cookie newCookie( String name, String value ) 
	{
    	Cookie cookie = new Cookie(name,value);
    	cookie.setHttpOnly(true);
    	cookie.setPath("/");
    	cookie.setSecure(false);
    	cookie.setMaxAge(5*60); // set expiry to 5 mins    	
    	return cookie;
	}

}
