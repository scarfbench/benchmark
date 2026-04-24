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

import com.ibm.sample.daytrader.core.api.TradeServices;
import com.ibm.sample.daytrader.core.direct.CookieUtils;
import com.ibm.sample.daytrader.entities.*;
import com.ibm.sample.daytrader.entities.*;
import com.ibm.sample.daytrader.utils.Log;
import com.ibm.sample.daytrader.utils.TradeConfig;

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.ArrayList;
import java.math.BigDecimal;



/**
 * TradeServletAction provides servlet specific client side access to each of
 * the Trade brokerage user operations. These include login, logout, buy, sell,
 * getQuote, etc. TradeServletAction manages a web interface to Trade handling
 * HttpRequests/HttpResponse objects and forwarding results to the appropriate
 * JSP page for the web interface. TradeServletAction invokes
 * {@link TradeAction} methods to actually perform each trading operation.
 * 
 */

public class TradeServletAction {

    private TradeServices tAction = null;

    public TradeServletAction() {
        if (TradeConfig.getAccessMode() == TradeConfig.STANDARD) {
            tAction = new TradeAction();
        } else if (TradeConfig.getAccessMode() == TradeConfig.WEBSERVICES) {             
            //Disable SOAP
            //tAction = (TradeServices) new TradeWebSoapProxy();
        } else {
            throw new IllegalArgumentException(
                "TradeServletAction - Uknown TradeConfig accessMode=" +
                TradeConfig.getAccessMode());
        }
    }

    /**
     * Display User Profile information such as address, email, etc. for the
     * given Trader Dispatch to the Trade Account JSP for display
     * 
     * @param userID
     *            The User to display profile info
     * @param ctx
     *            the servlet context
     * @param req
     *            the HttpRequest object
     * @param resp
     *            the HttpResponse object
     * @param results
     *            A short description of the results/success of this web request
     *            provided on the web page
     * @exception javax.servlet.ServletException
     *                If a servlet specific exception is encountered
     * @exception javax.io.IOException
     *                If an exception occurs while writing results back to the
     *                user
     * 
     */
    void doAccount(ServletContext ctx, HttpServletRequest req,
            HttpServletResponse resp, String userID, String results)
            throws javax.servlet.ServletException, java.io.IOException 
    {  
        try 
        {
            AccountDataBean accountData = tAction.getAccountData(userID);
            AccountProfileDataBean accountProfileData = tAction.getAccountProfileData(userID);
            ArrayList orderDataBeans = (TradeConfig.getLongRun() ? new ArrayList() : (ArrayList) tAction.getOrders(userID));
            
        	Log.trace("TradeServletAction#doAccount() - account was successful");
            
            req.setAttribute("accountData", accountData);
            req.setAttribute("accountProfileData", accountProfileData);
            req.setAttribute("orderDataBeans", orderDataBeans);
            req.setAttribute("results", results);
            
            requestDispatch(ctx, req, resp, userID, TradeConfig.getPage(TradeConfig.ACCOUNT_PAGE));
        } 
        catch (java.lang.IllegalArgumentException e) 
        { 
        	// this is a user error so will forward to another page rather than throw a 500
            req.setAttribute("results", results + "could not find account for userID = " + userID);
            requestDispatch(ctx, req, resp, userID, TradeConfig.getPage(TradeConfig.HOME_PAGE));
            // log the exception with an error level of 3 which means, handled
            // exception but would invalidate a automation run
            Log.error("TradeServletAction.doAccount(...)",
                      "illegal argument, information should be in exception string",
                      e);
        } 
        catch (Exception e) 
        {
            // log the exception with error page
            throw new ServletException("TradeServletAction.doAccount(...)" + " exception user =" + userID, e);
        }

    }

    /**
     * Update User Profile information such as address, email, etc. for the
     * given Trader Dispatch to the Trade Account JSP for display If any in put
     * is incorrect revert back to the account page w/ an appropriate message
     * 
     * @param userID
     *            The User to upddate profile info
     * @param password
     *            The new User password
     * @param cpassword
     *            Confirm password
     * @param fullname
     *            The new User fullname info
     * @param address
     *            The new User address info
     * @param cc
     *            The new User credit card info
     * @param email
     *            The new User email info
     * @param ctx
     *            the servlet context
     * @param req
     *            the HttpRequest object
     * @param resp
     *            the HttpResponse object
     * @exception javax.servlet.ServletException
     *                If a servlet specific exception is encountered
     * @exception javax.io.IOException
     *                If an exception occurs while writing results back to the
     *                user
     * 
     */
    void doAccountUpdate(ServletContext ctx, HttpServletRequest req,
            HttpServletResponse resp, String userID, String password,
            String cpassword, String fullName, String address,
            String creditcard, String email)
            throws javax.servlet.ServletException, java.io.IOException {
        
    	String results = "";
     	
        // First verify input data
        boolean doUpdate = true;
        if (password.equals(cpassword) == false) 
        {
            results = "Update profile error: passwords do not match";
            doUpdate = false;
        } 
        else if (password.length() <= 0 || fullName.length() <= 0 
        		|| address.length() <= 0 || creditcard.length() <= 0
                || email.length() <= 0) 
        {
            results = "Update profile error: please fill in all profile information fields";
            doUpdate = false;
        }
        AccountProfileDataBean accountProfileData = new AccountProfileDataBean(
                userID, password, fullName, address, email, creditcard);
        try 
        {
            if (doUpdate) 
            {
                accountProfileData = tAction.updateAccountProfile(accountProfileData);
                results = "Account profile update successful";
            	Log.trace("TradeServletAction#doAccountUpdate() - account update was successful");
            }

        } 
        catch (java.lang.IllegalArgumentException e) 
        { 
        	// this is a user error so I will forward them to another page rather than throw a 500
            req.setAttribute(
                            "results",
                            results
                                    + "invalid argument, check userID is correct, and the database is populated"
                                    + userID);
            Log.error(
            		e,
                    "TradeServletAction.doAccount(...)",
                    "illegal argument, information should be in exception string",
                    "treating this as a user error and forwarding on to a new page");
        } 
        catch (Exception e) 
        {
            // log the exception with error page
            throw new ServletException(
                    "TradeServletAction.doAccountUpdate(...)"
                            + " exception user =" + userID, e);
        }
        doAccount(ctx, req, resp, userID, results);
    }

    /**
     * Buy a new holding of shares for the given trader Dispatch to the Trade
     * Portfolio JSP for display
     * 
     * @param userID
     *            The User buying shares
     * @param symbol
     *            The stock to purchase
     * @param amount
     *            The quantity of shares to purchase
     * @param ctx
     *            the servlet context
     * @param req
     *            the HttpRequest object
     * @param resp
     *            the HttpResponse object
     * @exception javax.servlet.ServletException
     *                If a servlet specific exception is encountered
     * @exception javax.io.IOException
     *                If an exception occurs while writing results back to the
     *                user
     * 
     */
    void doBuy(ServletContext ctx, HttpServletRequest req,
            HttpServletResponse resp, String userID, String symbol,
            String quantity) throws ServletException, IOException {

        String results = "";

        try 
        {
            OrderDataBean orderData = tAction.buy(userID, symbol, new Double(
                    quantity).doubleValue(), TradeConfig.orderProcessingMode);

        	Log.trace("TradeServletAction#doBuy() - buy was successful");
        	
            req.setAttribute("orderData", orderData);
            req.setAttribute("results", results);
           	
        } 
        catch (java.lang.IllegalArgumentException e) 
        { 
        	// this is a user error so forward them to another page rather than throw a 500
            req.setAttribute("results", results + "illegal argument:");
            requestDispatch(ctx, req, resp, userID, TradeConfig
                    .getPage(TradeConfig.HOME_PAGE));
            // log the exception with an error level of 3 which means, handled
            // exception but would invalidate a automation run
            Log.error(e, "TradeServletAction.doBuy(...)",
                    "illegal argument. userID = " + userID, "symbol = "
                            + symbol);
        } 
        catch (Exception e) 
        {
            // log the exception with error page
            throw new ServletException("TradeServletAction.buy(...)"
                    + " exception buying stock " + symbol + " for user "
                    + userID, e);
        }
        requestDispatch(ctx, req, resp, userID, TradeConfig.getPage(TradeConfig.ORDER_PAGE));
    }

    /**
     * Create the Trade Home page with personalized information such as the
     * traders account balance Dispatch to the Trade Home JSP for display
     * 
     * @param ctx
     *            the servlet context
     * @param req
     *            the HttpRequest object
     * @param resp
     *            the HttpResponse object
     * @param results
     *            A short description of the results/success of this web request
     *            provided on the web page
     * @exception javax.servlet.ServletException
     *                If a servlet specific exception is encountered
     * @exception javax.io.IOException
     *                If an exception occurs while writing results back to the
     *                user
     * 
     */
    void doHome(ServletContext ctx, HttpServletRequest req,
            HttpServletResponse resp, String userID, String results)
            throws javax.servlet.ServletException, java.io.IOException {
                      	      
        try {
            AccountDataBean accountData = tAction.getAccountData(userID);
            Collection<HoldingDataBean> holdingDataBeans = tAction.getHoldings(userID);
            
        	Log.trace("TradeServletAction#doHome() - home was successful");

            // Edge Caching:
            // Getting the MarketSummary has been moved to the JSP
            // MarketSummary.jsp. This makes the MarketSummary a
            // standalone "fragment", and thus is a candidate for
            // Edge caching.
            // marketSummaryData = tAction.getMarketSummary();

            req.setAttribute("accountData", accountData);
            req.setAttribute("holdingDataBeans", holdingDataBeans);
            // See Edge Caching above
            // req.setAttribute("marketSummaryData", marketSummaryData);
            req.setAttribute("results", results);
           	
        } catch (java.lang.IllegalArgumentException e) { 
            // this is a user error so I will
            // forward them to another page rather than throw a 500
            req.setAttribute("results", results + "check userID = " + userID
                + " and that the database is populated");
            requestDispatch(ctx, req, resp, userID, TradeConfig.getPage(TradeConfig.HOME_PAGE));
            // log the exception with an error level of 3 which means, handled
            // exception but would invalidate a automation run
            Log.error("TradeServletAction.doHome(...)"
                + "illegal argument, information should be in exception string"
                + "treating this as a user error and forwarding on to a new page",
                e);
        } catch (Exception e) {
            boolean javaee = false;
            // since we will not have the EJB Spec for non-JavaEE containers,
            // lets try to handle the expected exception logic, where for
            // JavaEE we should catch a javax.ejb.FinderException while
            // for non-JavaEE we should catch a RuntimeException
            
            if (!javaee && (e instanceof RuntimeException)) {   // non-JavaEE container
                // this is a user error so I will
                // forward them to another page rather than throw a 500
                req.setAttribute("results", results
                    + "\nCould not find account for + " + userID);
                // requestDispatch(ctx, req, resp,
                // TradeConfig.getPage(TradeConfig.HOME_PAGE));
                // log the exception with an error level of 3 which means, handled
                // exception but would invalidate a automation run
                Log.error("TradeServletAction.doHome(...)"
                    + "Error finding account for user "
                    + userID
                    + "treating this as a user error and forwarding on to a new page",
                    e);                
            } else {
                // log the exception with error page
                throw new ServletException("TradeServletAction.doHome(...)"
                    + " exception user =" + userID, e);
            }
        }

        requestDispatch(ctx, req, resp, userID, TradeConfig.getPage(TradeConfig.HOME_PAGE));
    }

    /**
     * Login a Trade User. Dispatch to the Trade Home JSP for display
     * 
     * @param userID
     *            The User to login
     * @param passwd
     *            The password supplied by the trader used to authenticate
     * @param ctx
     *            the servlet context
     * @param req
     *            the HttpRequest object
     * @param resp
     *            the HttpResponse object
     * @param results
     *            A short description of the results/success of this web request
     *            provided on the web page
     * @exception javax.servlet.ServletException
     *                If a servlet specific exception is encountered
     * @exception javax.io.IOException
     *                If an exception occurs while writing results back to the
     *                user
     * 
     */
    void doLogin(ServletContext ctx, HttpServletRequest req,
            HttpServletResponse resp, String userID, String passwd)
            throws javax.servlet.ServletException, java.io.IOException {

        String results = "";
        try {
            // Got a valid userID and passwd, attempt login
            AccountDataBean accountData = tAction.login(userID, passwd);
            
            if (accountData != null) 
            { 	
            	// Login was successful
            	Log.trace("TradeServletAction#doLogin() - login was successful");

            	// Invalidate the old session; if there is one
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
            	HttpSession session = req.getSession(true);
            	session.setMaxInactiveInterval(5*60);
            	
            	// And add the new UserID cookie
				resp.addCookie(CookieUtils.newCookie("uidBean",userID));
				Log.debug("TradeServletAction#login() - addCookie(uidBean," + userID + ")" );   
               	
                results = "Ready to Trade";            
                doHome(ctx, req, resp, userID, results);
                return;
            }
            else
            {
            	// this is a user error wo will forward them to another page rather than throw a 500
               	req.setAttribute("results", results + "\nCould not find account for user=" + userID);
                // log the exception with an error level of 3 which means,
                // handled exception but would invalidate a automation run
                Log.debug(
                       "TradeServletAction.doLogin(...)" +
                       "Error finding account for user " + userID + 
                       "user entered a bad username or the database is not populated");
            }
        } 
        catch (java.lang.IllegalArgumentException e) 
        { 
        	// this is a user error so forward them to another page rather than throw a 500
            req.setAttribute("results", results + "illegal argument:" + e.getMessage());
            // log the exception with an error level of 3 which means, handled
            // exception but would invalidate a automation run
            Log.error(
            		e,
                    "TradeServletAction.doLogin(...)",
                    "illegal argument, information should be in exception string",
                    "treating this as a user error and forwarding on to a new page");
        } 
        catch (Exception e) 
        {
            // log the exception with error page
            throw new ServletException("TradeServletAction.doLogin(...)"
                    + "Exception logging in user=" + userID + " with password="
                    + passwd + "; " + e.getMessage(), e);
        }
        
        // If you get here then login failed due to user error so forward them to another page
        requestDispatch(ctx, req, resp, null, TradeConfig.getPage(TradeConfig.WELCOME_PAGE));

    }

    /**
     * Logout a Trade User Dispatch to the Trade Welcome JSP for display
     * 
     * @param userID
     *            The User to logout
     * @param ctx
     *            the servlet context
     * @param req
     *            the HttpRequest object
     * @param resp
     *            the HttpResponse object
     * @param results
     *            A short description of the results/success of this web request
     *            provided on the web page
     * @exception javax.servlet.ServletException
     *                If a servlet specific exception is encountered
     * @exception javax.io.IOException
     *                If an exception occurs while writing results back to the
     *                user
     * 
     */
    void doLogout(ServletContext ctx, HttpServletRequest req,
            HttpServletResponse resp, String userID) throws ServletException,
            IOException {
        String results = "";

        try 
        {
            tAction.logout(userID);   
        	// If no exception then logout was successful
        	Log.trace("TradeServletAction#doLogout() - logout was successful");
        	           
        	// Invalidate the old session if there is one
   			HttpSession oldSession = req.getSession(false);
   			if (oldSession != null) 
   			{
   				oldSession.invalidate();
   			}
   		            
			/*
			 * Invalidate all cookies by, for each cookie received,
			 * overwriting value and instructing browser to deletes 
			 * it
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

        } 
        catch (java.lang.IllegalArgumentException e) 
        { 
        	// this is a user error so forward them to another page, at the end of the page.
            req.setAttribute("results", results + "illegal argument:" + e.getMessage());

            // log the exception with an error level of 3 which means, handled
            // exception but would invalidate a automation run
            Log.error(
                e,
                "TradeServletAction.doLogout(...)",
                "illegal argument, information should be in exception string",
                "treating this as a user error and forwarding on to a new page");
        } 
        catch (Exception e) 
        {
            // log the exception and foward to a error page
            Log.error(e, "TradeServletAction.doLogout(...):",
                    "Error logging out" + userID, "fowarding to an error page");
            // set the status_code to 500
            throw new ServletException("TradeServletAction.doLogout(...)"
                    + "exception logging out user " + userID, e);
        }
        
        requestDispatch(ctx, req, resp, null, TradeConfig.getPage(TradeConfig.WELCOME_PAGE));
    }

    /**
     * Retrieve the current portfolio of stock holdings for the given trader
     * Dispatch to the Trade Portfolio JSP for display
     * 
     * @param userID
     *            The User requesting to view their portfolio
     * @param ctx
     *            the servlet context
     * @param req
     *            the HttpRequest object
     * @param resp
     *            the HttpResponse object
     * @param results
     *            A short description of the results/success of this web request
     *            provided on the web page
     * @exception javax.servlet.ServletException
     *                If a servlet specific exception is encountered
     * @exception javax.io.IOException
     *                If an exception occurs while writing results back to the
     *                user
     * 
     */
    void doPortfolio(ServletContext ctx, HttpServletRequest req,
            HttpServletResponse resp, String userID, String results)
            throws ServletException, IOException {
       	
        try {
            // Get the holdings for this user

            Collection quoteDataBeans = new ArrayList();
            Collection holdingDataBeans = tAction.getHoldings(userID);

            // Walk through the collection of user
            // holdings and creating a list of quotes
            if (holdingDataBeans.size() > 0) {

                Iterator it = holdingDataBeans.iterator();
                while (it.hasNext()) {
                    HoldingDataBean holdingData = (HoldingDataBean) it.next();
                    QuoteDataBean quoteData = tAction.getQuote(holdingData
                            .getQuoteID());
                    quoteDataBeans.add(quoteData);
                }
            } else {
                results = results + ".  Your portfolio is empty.";
            }
            
        	Log.trace("TradeServletAction#doPortfolio() - portfolio was successful");
            
            req.setAttribute("results", results);
            req.setAttribute("holdingDataBeans", holdingDataBeans);
            req.setAttribute("quoteDataBeans", quoteDataBeans);
            
            requestDispatch(ctx, req, resp, userID, TradeConfig.getPage(TradeConfig.PORTFOLIO_PAGE));
        } 
        catch (java.lang.IllegalArgumentException e) 
        { 
        	// this is a user error so forward them to another page rather than throw a 500
            req.setAttribute("results", results + "illegal argument:" + e.getMessage());
            requestDispatch(ctx, req, resp, userID, TradeConfig.getPage(TradeConfig.PORTFOLIO_PAGE));
            // log the exception with an error level of 3 which means, handled
            // exception but would invalidate a automation run
            Log
                    .error(
                            e,
                            "TradeServletAction.doPortfolio(...)",
                            "illegal argument, information should be in exception string",
                            "user error");
        } 
        catch (Exception e) 
        {
            // log the exception with error page
            throw new ServletException("TradeServletAction.doPortfolio(...)" + " exception user =" + userID, e);
        }
    }

    /**
     * Retrieve the current Quote for the given stock symbol Dispatch to the
     * Trade Quote JSP for display
     * 
     * @param userID
     *            The stock symbol used to get the current quote
     * @param ctx
     *            the servlet context
     * @param req
     *            the HttpRequest object
     * @param resp
     *            the HttpResponse object
     * @exception javax.servlet.ServletException
     *                If a servlet specific exception is encountered
     * @exception javax.io.IOException
     *                If an exception occurs while writing results back to the
     *                user
     * 
     */
    void doQuotes(ServletContext ctx, HttpServletRequest req,
            HttpServletResponse resp, String userID, String symbols)
            throws ServletException, IOException {
        String results = "";

        // Edge Caching:
        // Getting Quotes has been moved to the JSP
        // Quote.jsp. This makes each Quote a
        // standalone "fragment", and thus is a candidate for
        // Edge caching.
        //            
        
        requestDispatch(ctx, req, resp, userID, TradeConfig.getPage(TradeConfig.QUOTE_PAGE));
    }

    /**
     * Register a new trader given the provided user Profile information such as
     * address, email, etc. Dispatch to the Trade Home JSP for display
     * 
     * @param userID
     *            The User to create
     * @param passwd
     *            The User password
     * @param fullname
     *            The new User fullname info
     * @param ccn
     *            The new User credit card info
     * @param money
     *            The new User opening account balance
     * @param address
     *            The new User address info
     * @param email
     *            The new User email info
     * @return The userID of the new trader
     * @param ctx
     *            the servlet context
     * @param req
     *            the HttpRequest object
     * @param resp
     *            the HttpResponse object
     * @exception javax.servlet.ServletException
     *                If a servlet specific exception is encountered
     * @exception javax.io.IOException
     *                If an exception occurs while writing results back to the
     *                user
     * 
     */
    void doRegister(ServletContext ctx, HttpServletRequest req,
            HttpServletResponse resp, String userID, String passwd,
            String cpasswd, String fullname, String ccn,
            String openBalanceString, String email, String address)
            throws ServletException, IOException 
    {
        String results = "";

        try {
            // Validate user passwords match and are at least 1 char in length
            if ((passwd.equals(cpasswd)) && (passwd.length() >= 1)) 
            {
                AccountDataBean accountData = tAction.register(userID, passwd,
                        fullname, address, email, ccn, new BigDecimal(openBalanceString));
                if (accountData == null) 
                {
                    results = "Registration operation failed;";
                    Log.trace(results);
                    req.setAttribute("results", results);
                    requestDispatch(ctx, req, resp, userID, TradeConfig.getPage(TradeConfig.REGISTER_PAGE));
                } 
                else 
                {	
                	Log.trace("TradeServletAction#doRegister() - register was successful");
                	
                    doLogin(ctx, req, resp, userID, passwd);
                    results = "Registration operation succeeded;  Account " + accountData.getAccountID() + " has been created.";
                    req.setAttribute("results", results);
                }
            } else {
                // Password validation failed
                results = "Registration operation failed, your passwords did not match";
                Log.trace(results);
                req.setAttribute("results", results);
                requestDispatch(ctx, req, resp, userID, TradeConfig.getPage(TradeConfig.REGISTER_PAGE));
            }

        } catch (Exception e) {
            // log the exception with error page
            throw new ServletException("TradeServletAction.doRegister(...)"
                    + " exception user =" + userID, e);
        }
    }

    /**
     * Sell a current holding of stock shares for the given trader. Dispatch to
     * the Trade Portfolio JSP for display
     * 
     * @param userID
     *            The User buying shares
     * @param symbol
     *            The stock to sell
     * @param indx
     *            The unique index identifying the users holding to sell
     * @param ctx
     *            the servlet context
     * @param req
     *            the HttpRequest object
     * @param resp
     *            the HttpResponse object
     * @exception javax.servlet.ServletException
     *                If a servlet specific exception is encountered
     * @exception javax.io.IOException
     *                If an exception occurs while writing results back to the
     *                user
     * 
     */
    void doSell(ServletContext ctx, HttpServletRequest req,
            HttpServletResponse resp, String userID, Integer holdingID)
            throws ServletException, IOException {
        String results = "";
     	
        try {
            OrderDataBean orderData = tAction.sell(userID, holdingID, TradeConfig.orderProcessingMode);

        	Log.trace("TradeServletAction#doSell() - sell was successful");
            
            req.setAttribute("orderData", orderData);
            req.setAttribute("results", results);
        } catch (java.lang.IllegalArgumentException e) { // this is a user
                                                            // error so I will
            // just log the exception and then later on I will redisplay the
            // portfolio page
            // because this is just a user exception
            Log
                    .error(
                            e,
                            "TradeServletAction.doSell(...)",
                            "illegal argument, information should be in exception string",
                            "user error");
        } catch (Exception e) {
            // log the exception with error page
            throw new ServletException("TradeServletAction.doSell(...)"
                    + " exception selling holding " + holdingID + " for user ="
                    + userID, e);
        }
        requestDispatch(ctx, req, resp, userID, TradeConfig.getPage(TradeConfig.ORDER_PAGE));
    }

    void doWelcome(ServletContext ctx, HttpServletRequest req,
            HttpServletResponse resp, String status) throws ServletException,
            IOException {

        req.setAttribute("results", status);
        requestDispatch(ctx, req, resp, null, TradeConfig.getPage(TradeConfig.WELCOME_PAGE));
    }

    private void requestDispatch(ServletContext ctx, HttpServletRequest req,
            HttpServletResponse resp, String userID, String page)
            throws ServletException, IOException 
    {       
        ctx.getRequestDispatcher(page).include(req, resp);
    }

}
