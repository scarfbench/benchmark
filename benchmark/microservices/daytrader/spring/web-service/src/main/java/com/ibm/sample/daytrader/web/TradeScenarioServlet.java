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


import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.annotation.WebServlet;
import javax.servlet.annotation.WebInitParam;

import com.ibm.sample.daytrader.core.direct.CookieUtils;
import com.ibm.sample.daytrader.entities.HoldingDataBean;
import com.ibm.sample.daytrader.utils.Log;
import com.ibm.sample.daytrader.utils.TradeConfig;

import java.util.Collection;
import java.util.Iterator;
import java.io.IOException;

/**
 * TradeScenarioServlet emulates a population of web users by generating a specific Trade operation 
 * for a randomly chosen user on each access to the URL. Test this servlet by clicking Trade Scenario 
 * and hit "Reload" on your browser to step through a Trade Scenario. To benchmark using this URL aim 
 * your favorite web load generator (such as AKStress) at the Trade Scenario URL and fire away.
 */
@WebServlet(value = "/scenario", initParams = {
        @WebInitParam(name = "runTimeMode", value = "Web JPA"),
        @WebInitParam(name = "orderProcessingMode", value = "Synchronous"),
        @WebInitParam(name = "accessMode", value = "Standard"),
        @WebInitParam(name = "workloadMix", value = "Standard"),
        @WebInitParam(name = "WebInterface", value = "JSP"),
        @WebInitParam(name = "maxUsers", value = "200"),
        @WebInitParam(name = "maxQuotes", value = "400"),
        @WebInitParam(name = "primIterations", value = "1"),
        @WebInitParam(name = "cachingType", value = "No Caching")})
public class TradeScenarioServlet extends HttpServlet {

   /**
    * Servlet initialization method.
    */
    public void init(ServletConfig config) throws ServletException
    {
        super.init(config);
        java.util.Enumeration en = config.getInitParameterNames();
        while ( en.hasMoreElements() )
        {
            String parm = (String) en.nextElement();
            String value = config.getInitParameter(parm);
            TradeConfig.setConfigParam(parm, value);
        }
    }
    
   /**
    * Returns a string that contains information about TradeScenarioServlet
    *
    * @return The servlet information
    */
    public java.lang.String getServletInfo()
    {
        return "TradeScenarioServlet emulates a population of web users";
    }    



   /**
    * Process incoming HTTP GET requests
    *
    * @param request Object that encapsulates the request to the servlet
    * @param response Object that encapsulates the response from the servlet
    */
    public void doGet(javax.servlet.http.HttpServletRequest request, javax.servlet.http.HttpServletResponse response)
        throws ServletException, IOException
    {
        performTask(request,response);
    }

   /**
    * Process incoming HTTP POST requests
    *
    * @param request Object that encapsulates the request to the servlet
    * @param response Object that encapsulates the response from the servlet
    */
    public void doPost(javax.servlet.http.HttpServletRequest request, javax.servlet.http.HttpServletResponse response)
        throws ServletException, IOException
    {
        performTask(request,response);
    }    

   /** 
    * Main service method for TradeScenarioServlet
    *
    * @param request Object that encapsulates the request to the servlet
    * @param response Object that encapsulates the response from the servlet
    */    
    public void performTask(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException 
    {
    	
        // Scenario generator for Trade2
        char action = ' ';
        String userID = null;

        // String to create full dispatch path to TradeAppServlet w/ request Parameters
        String dispPath = null; // Dispatch Path to TradeAppServlet

        resp.setContentType("text/html");
        
    	String scenarioAction = (String) req.getParameter("action");

		ServletContext ctx = null;
		HttpSession session = null;
		try
		{
			ctx = getServletConfig().getServletContext();
			// These operations require the user to be logged in. Verify that  
			// the user is logged in, and if not then change the operation to 
			// a login
	        userID = CookieUtils.getCookieValue(req.getCookies(),"uidBean");	        
			Log.debug("TradeScenarioServlet#performTask(" + action + ") - getCookie(uidBean): " + userID );
		}
		catch (Exception e)
		{
			Log.error(
				"trade_client.TradeScenarioServlet.service(...): performing " + scenarioAction +
				"error getting ServletContext,HttpSession, or UserID from session" +
				"will make scenarioAction a login and try to recover from there", e);
			userID = null;
			action = 'l';
		}

		if (userID == null)
		{
			action = 'l'; // change to login
			TradeConfig.incrementScenarioCount();
		}
		else if (action == ' ') 
		{	
			//action is not specified perform a random operation according to current mix
			// Tell getScenarioAction if we are an original user or a registered user 
			// -- sellDeficits should only be compensated for with original users.
			action = TradeConfig.getScenarioAction(userID.startsWith(TradeConfig.newUserPrefix));
		}	
		
		Log.debug("TradeScenarioServlet#performTask(" + action + ")"); 
		Log.debug("TradeScenarioServlet#performTask(" + action + ")"); 
		Log.debug("TradeScenarioServlet#performTask(" + action + ")"); 
		Log.debug("TradeScenarioServlet#performTask(" + action + ")"); 
		Log.debug("TradeScenarioServlet#performTask(" + action + ")"); 
		
        switch (action)
            {
                case 'q' : //quote 
                    dispPath = tasPathPrefix + "quotes&inScenario=true&symbols=" + TradeConfig.rndSymbols();
                    ctx.getRequestDispatcher(dispPath).include(req, resp);
                    break;
                case 'a' : //account
                    dispPath = tasPathPrefix + "account&inScenario=true";
                    ctx.getRequestDispatcher(dispPath).include(req, resp);
                    break;
                case 'u' : //update account profile
                    dispPath = tasPathPrefix + "account&inScenario=true";
                    ctx.getRequestDispatcher(dispPath).include(req, resp);

                    String fullName = "rnd" + System.currentTimeMillis();
                    String address = "rndAddress";
                    String   password = "xxx";
                    String email = "rndEmail";
                    String creditcard = "rndCC";
                    dispPath = tasPathPrefix + "update_profile&inScenario=true&fullname=" + fullName + 
                        "&password=" + password + "&cpassword=" + password +                     
                        "&address=" + address +    "&email=" + email + 
                        "&creditcard=" +  creditcard;
                    ctx.getRequestDispatcher(dispPath).include(req, resp);
                    break;
                case 'h' : //home
                    dispPath = tasPathPrefix + "home&inScenario=true";
                    ctx.getRequestDispatcher(dispPath).include(req, resp);
                    break;
                case 'l' : //login
                	
                    userID = TradeConfig.getUserID();
                    String password2 = "xxx";
                    dispPath = tasPathPrefix + "login&inScenario=true&uid=" + userID + "&passwd=" + password2;                   
                    ctx.getRequestDispatcher(dispPath).include(req, resp);                 
            		
                    // Although you are forwarding the login to another page it has a different url. The 
                    // request/response that it uses to login are not the same as those in this method. 
                    // So you still need to invalidate the session and cookies in this servlet.
                    
                	// First step is invalidate old session; if it exists
           			HttpSession oldSession = req.getSession(false);
           			if (oldSession != null) 
           			{
           				oldSession.invalidate();
           			}   
       				/*
       				 * Invalidate all cookies by, for each cookie received,
       				 * overwriting value and instructing browser to delete 
       				 * it; except for the userId cookie; which we will set
       				 * to the newly logged in user. 
       				 */
       				Cookie[] cookies = req.getCookies();
       				if (cookies != null && cookies.length > 0) 
       				{
       					for (Cookie cookie : cookies) 
       					{
   							cookie.setValue("-");
   							cookie.setMaxAge(0);
   							resp.addCookie(cookie);
       					}
       				}
                
                	// Now you can generate the new session               
                	session = req.getSession(true);
                	session.setMaxInactiveInterval(5*60);
                	
   	            	// And add the new UserID cookie
   					resp.addCookie(CookieUtils.newCookie("uidBean",userID));
   					Log.debug("TradeScenarioServlet#login() - addCookie(uidBean," + userID + ")" ); 	
            		
                    break;
                case 'o' : //logout
                    dispPath = tasPathPrefix + "logout&inScenario=true";
                    ctx.getRequestDispatcher(dispPath).include(req, resp);
             
                    // Although you are forwarding the logout to another page it has a different url. The
                    // request/response that it uses are not the same as these. So you need to invalidate 
                    // these session and cookies.
                    
                	// Now invalidate old session; if it exists
           			oldSession = req.getSession(false);
           			if (oldSession != null) 
           			{
           				oldSession.invalidate();
           			}
           			
       				/*
       				 * Invalidate all cookies by, for each cookie received,
       				 * overwriting value and instructing browser to deletes 
       				 * it
       				 */
       				cookies = req.getCookies();
       				if (cookies != null && cookies.length > 0) 
       				{
       					for (Cookie cookie : cookies) 
       					{
     						cookie.setValue("-");
       						cookie.setMaxAge(0);
       						resp.addCookie(cookie);
       					}
       				}
                    break;
                case 'p' : //portfolio
                    dispPath = tasPathPrefix + "portfolio&inScenario=true";
                    ctx.getRequestDispatcher(dispPath).include(req, resp);
                    break;
                case 'r' : //register
                    userID = TradeConfig.rndNewUserID();
                    String passwd = "yyy";
                    fullName = TradeConfig.rndFullName();
                    creditcard = TradeConfig.rndCreditCard();
                    String money = TradeConfig.rndBalance();
                    email = TradeConfig.rndEmail(userID);
                    String smail = TradeConfig.rndAddress();
                    dispPath = tasPathPrefix + "register&inScenario=true&Full Name=" + fullName + "&snail mail=" + smail +
                        "&email=" + email + "&user id=" + userID + "&passwd=" + passwd + 
                        "&confirm passwd=" + passwd + "&money=" + money + 
                        "&Credit Card Number=" + creditcard;
                    ctx.getRequestDispatcher(dispPath).include(req, resp);                    
                    
                    // Although you are forwarding the register to another page it has a different url. Thus,
                    // the request/response that it uses are not the same as this one. So you still need to
                    // invalidate the oldSession, cookies, and then you can generate the new session.
                    
                	// First step is invalidate old session; if it exists
           			oldSession = req.getSession(false);
           			if (oldSession != null) 
           			{
           				oldSession.invalidate();
           			}
           		            
       				/*
       				 * Invalidate all cookies by, for each cookie received,
       				 * overwriting value and instructing browser to delete 
       				 * it; except for the userId cookie; which we will set
       				 * to the newly logged in user. 
       				 */
       				cookies = req.getCookies();
       				if (cookies != null && cookies.length > 0) 
       				{
       					for (Cookie cookie : cookies) 
       					{
   							cookie.setValue("-");
   							cookie.setMaxAge(0);
   							resp.addCookie(cookie);
       					}
       				}
                
                	// Now you can generate the new session               
                	session = req.getSession(true);
                	session.setMaxInactiveInterval(5*60);
                	
   	            	// And add the new UserID cookie
   					resp.addCookie(CookieUtils.newCookie("uidBean",userID));
   					Log.debug("TradeScenarioServlet#register() - addCookie(uidBean," + userID + ")" ); 	
       				
                    break;
                case 's' : //sell
                    dispPath = tasPathPrefix + "portfolioNoEdge&inScenario=true";
                    ctx.getRequestDispatcher(dispPath).include(req, resp);

                    Collection holdings = (Collection) req.getAttribute("holdingDataBeans");
                    int numHoldings = holdings.size();
                    if (numHoldings > 0)
                    {
                        //sell first available security out of holding 
                        
                        Iterator it = holdings.iterator();
                        boolean foundHoldingToSell = false;
                        while (it.hasNext()) 
                        {
                            HoldingDataBean holdingData = (HoldingDataBean) it.next();
                            if ( !(holdingData.getPurchaseDate().equals(new java.util.Date(0)))  )
                            {
                                Integer holdingID = holdingData.getHoldingID();

                                dispPath = tasPathPrefix + "sell&inScenario=true&holdingID="+holdingID;
                                ctx.getRequestDispatcher(dispPath).include(req, resp);
                                foundHoldingToSell = true;
                                break;    
                            }
                        }
                        if (foundHoldingToSell) break;
                        if (Log.doTrace())
                            Log.trace("TradeScenario: No holding to sell -switch to buy -- userID = " + userID + "  Collection count = " + numHoldings);        

                    }
                    // At this point: A TradeScenario Sell was requested with No Stocks in Portfolio
                    // This can happen when a new registered user happens to request a sell before a buy
                    // In this case, fall through and perform a buy instead

                    /* Trade 2.037: Added sell_deficit counter to maintain correct buy/sell mix.
                     * When a users portfolio is reduced to 0 holdings, a buy is requested instead of a sell.
                     * This throws off the buy/sell mix by 1. This results in unwanted holding table growth
                     * To fix this we increment a sell deficit counter to maintain the correct ratio in getScenarioAction
                     * The 'z' action from getScenario denotes that this is a sell action that was switched from a buy
                     * to reduce a sellDeficit
                     */
                    if (userID.startsWith(TradeConfig.newUserPrefix) == false)
                    {
                        TradeConfig.incrementSellDeficit();
                    }
                case 'b' : //buy
                    String symbol = TradeConfig.rndSymbol();
                    String amount = TradeConfig.rndQuantity() + "";

                    dispPath = tasPathPrefix + "quotes&inScenario=true&symbols=" + symbol;
                    ctx.getRequestDispatcher(dispPath).include(req, resp);

                    dispPath = tasPathPrefix + "buy&inScenario=true&quantity=" + amount + "&symbol=" + symbol;
                    ctx.getRequestDispatcher(dispPath).include(req, resp);
                    break;
            } //end of switch statement 
        
    }

    // URL Path Prefix for dispatching to TradeAppServlet
    private final static String tasPathPrefix = "/app?action=";

}
