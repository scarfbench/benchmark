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
package com.ibm.sample.daytrader.gateway.utils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Random;


/**
 * TradeConfig is a JavaBean holding all configuration and runtime parameters for the Trade application
 * TradeConfig sets runtime parameters such as the RunTimeMode (EJB3, DIRECT, SESSION3, JDBC, JPA)
 *
 */

public class TradeConfig {

    public static String[] orderProcessingModeNames =
            { "Synchronous", "Asynchronous_2-Phase" };
    public static final int SYNCH = 0;
    public static final int ASYNCH_2PHASE = 1;
    public static int orderProcessingMode = SYNCH;

    /* Trade Database Scaling parameters*/
    private static int MAX_USERS = 200;
    private static int MAX_QUOTES = 400;
    private static int MAX_HOLDINGS = 10;
    private static int count = 0;
    private static String hostName = null;

    /* Trade Config Miscellaneous itmes */
    public static int QUOTES_PER_PAGE = 10;

    private static Random r0 = new Random(System.currentTimeMillis());
    //private static Random r1 = new Random(RND_SEED);
    private static Random randomNumberGenerator = r0;
    public static final String newUserPrefix = "ru:";
    public static final int verifyPercent = 5;

    private static boolean publishQuotePriceChange = false;

    /**
     *   -1 means every operation
     *    0 means never perform a market summary
     *  > 0 means number of seconds between summaries.  These will be
     *      synchronized so only one transaction in this period will create a summary and
     *      will cache its results.
     */
    private static int  marketSummaryInterval = 20;

    /*
     * Penny stocks is a problem where the random price change factor gets a stock
     * down to $.01.  In this case trade jumpstarts the price back to $6.00 to
     * keep the math interesting.
     */
    public static BigDecimal PENNY_STOCK_PRICE;
    public static BigDecimal PENNY_STOCK_RECOVERY_MIRACLE_MULTIPLIER;
    static {
        PENNY_STOCK_PRICE = new BigDecimal(0.01);
        PENNY_STOCK_PRICE =
                PENNY_STOCK_PRICE.setScale(2, BigDecimal.ROUND_HALF_UP);
        PENNY_STOCK_RECOVERY_MIRACLE_MULTIPLIER = new BigDecimal(600.0);
        PENNY_STOCK_RECOVERY_MIRACLE_MULTIPLIER.setScale(
                2,
                BigDecimal.ROUND_HALF_UP);
    }

    /* CJB (DAYTRADER-25) - Also need to impose a ceiling on the quote price to ensure
     * prevent account and holding balances from exceeding the databases decimal precision.
     * At some point, this maximum value can be used to trigger a stock split.
     */

    public static BigDecimal MAXIMUM_STOCK_PRICE;
    public static BigDecimal MAXIMUM_STOCK_SPLIT_MULTIPLIER;
    static {
        MAXIMUM_STOCK_PRICE = new BigDecimal(400);
        MAXIMUM_STOCK_PRICE.setScale(2, BigDecimal.ROUND_HALF_UP);
        MAXIMUM_STOCK_SPLIT_MULTIPLIER = new BigDecimal(0.5);
        MAXIMUM_STOCK_SPLIT_MULTIPLIER.setScale(2, BigDecimal.ROUND_HALF_UP);
    }

    // FUTURE:
    // If a "trade2.properties" property file is supplied, reset the default values
    // to match those specified in the file. This provides a persistent runtime
    // property mechanism during server startup

    /**
     * Return the hostname for this system
     * Creation date: (2/16/2000 9:02:25 PM)
     */

    private static String getHostname() {
        try {
            if (hostName == null) {
                hostName = java.net.InetAddress.getLocalHost().getHostName();
                //Strip of fully qualifed domain if necessary
                try {
                    hostName = hostName.substring(0, hostName.indexOf('.'));
                } catch (Exception e) {
                }
            }
        } catch (Exception e) {
            Log.error(
                    "Exception getting local host name using 'localhost' - ",
                    e);
            hostName = "localhost";
        }
        return hostName;
    }

    private static final BigDecimal orderFee = new BigDecimal("24.95");
    private static final BigDecimal cashFee = new BigDecimal("0.0");
    public static BigDecimal getOrderFee(String orderType) {
        if ((orderType.compareToIgnoreCase("BUY") == 0)
                || (orderType.compareToIgnoreCase("SELL") == 0))
            return orderFee;

        return cashFee;

    }

    public static double random() {
        return randomNumberGenerator.nextDouble();
    }

    public static String rndAddress() {
        return rndInt(1000) + " Oak St.";
    }
    public static String rndBalance() {
        //Give all new users a cool mill in which to trade
        return "1000000";
    }
    public static String rndCreditCard() {
        return rndInt(100)
                + "-"
                + rndInt(1000)
                + "-"
                + rndInt(1000)
                + "-"
                + rndInt(1000);
    }
    public static String rndEmail(String userID) {
        return userID + "@" + rndInt(100) + ".com";
    }
    public static String rndFullName() {
        return "first:" + rndInt(1000) + " last:" + rndInt(5000);
    }
    public static int rndInt(int i) {
        return (new Float(random() * i)).intValue();
    }
    public static float rndFloat(int i) {
        return (new Float(random() * i)).floatValue();
    }
    public static BigDecimal rndBigDecimal(float f) {
        return (new BigDecimal(random() * f)).setScale(
                2,
                BigDecimal.ROUND_HALF_UP);
    }

    public static boolean rndBoolean() {
        return randomNumberGenerator.nextBoolean();
    }

    /**
     * Returns a new Trade user
     * Creation date: (2/16/2000 8:50:35 PM)
     */
    public synchronized static String rndNewUserID() {

        return newUserPrefix
                + getHostname()
                + System.currentTimeMillis()
                + count++;
    }

    public static float rndPrice() {
        return ((new Integer(rndInt(200))).floatValue()) + 1.0f;
    }
    private final static BigDecimal ONE = new BigDecimal(1.0);
    public static BigDecimal getRandomPriceChangeFactor() {
        // CJB (DAYTRADER-25) - Vary change factor between 1.2 and 0.8
        double percentGain = rndFloat(1) * 0.2;
        if (random() < .5)
            percentGain *= -1;
        percentGain += 1;

        // change factor is between +/- 20%
        BigDecimal percentGainBD =
                (new BigDecimal(percentGain)).setScale(2, BigDecimal.ROUND_HALF_UP);
        if (percentGainBD.doubleValue() <= 0.0)
            percentGainBD = ONE;

        return percentGainBD;
    }

    public static float rndQuantity() {
        return ((new Integer(rndInt(200))).floatValue()) + 1.0f;
    }

    public static String rndSymbol() {
        return "s:" + rndInt(MAX_QUOTES - 1);
    }
    public static String rndSymbols() {

        String symbols = "";
        int num_symbols = rndInt(QUOTES_PER_PAGE);

        for (int i = 0; i <= num_symbols; i++) {
            symbols += "s:" + rndInt(MAX_QUOTES - 1);
            if (i < num_symbols)
                symbols += ",";
        }
        return symbols;
    }

    public static String rndUserID() {
        String nextUser = getNextUserIDFromDeck();
        if (Log.doTrace())
            Log.trace("TradeConfig:rndUserID -- new trader = " + nextUser);

        return nextUser;
    }

    private static synchronized String getNextUserIDFromDeck() {
        int numUsers = getMAX_USERS();
        if (deck == null) {
            deck = new ArrayList(numUsers);
            for (int i = 0; i < numUsers; i++)
                deck.add(i, new Integer(i));
            java.util.Collections.shuffle(deck, r0);
        }
        if (card >= numUsers)
            card = 0;
        return "uid:" + deck.get(card++);
    }

    //Trade implements a card deck approach to selecting
    // users for trading with tradescenarioservlet
    private static ArrayList deck = null;
    private static int card = 0;

    /**
     * Gets the orderProcessingModeNames
     * @return Returns a String[]
     */
    public static String[] getOrderProcessingModeNames() {
        return orderProcessingModeNames;
    }


    /**
     * Gets the mAX_USERS.
     * @return Returns a int
     */
    public static int getMAX_USERS() {
        return MAX_USERS;
    }

    /**
     * Sets the mAX_USERS.
     * @param mAX_USERS The mAX_USERS to set
     */
    public static void setMAX_USERS(int mAX_USERS) {
        MAX_USERS = mAX_USERS;
        deck = null; // reset the card deck for selecting users
    }

    /**
     * Gets the mAX_QUOTES.
     * @return Returns a int
     */
    public static int getMAX_QUOTES() {
        return MAX_QUOTES;
    }

    /**
     * Sets the mAX_QUOTES.
     * @param mAX_QUOTES The mAX_QUOTES to set
     */
    public static void setMAX_QUOTES(int mAX_QUOTES) {
        MAX_QUOTES = mAX_QUOTES;
    }

    /**
     * Gets the mAX_HOLDINGS.
     * @return Returns a int
     */
    public static int getMAX_HOLDINGS() {
        return MAX_HOLDINGS;
    }

    /**
     * Sets the mAX_HOLDINGS.
     * @param mAX_HOLDINGS The mAX_HOLDINGS to set
     */
    public static void setMAX_HOLDINGS(int mAX_HOLDINGS) {
        MAX_HOLDINGS = mAX_HOLDINGS;
    }

    public static void setPublishQuotePriceChange(boolean publishQuotePriceChange) {
        TradeConfig.publishQuotePriceChange = publishQuotePriceChange;
    }

    public static boolean getPublishQuotePriceChange() {
        return publishQuotePriceChange;
    }

    public static void setMarketSummaryInterval(int seconds) {
        TradeConfig.marketSummaryInterval = seconds;
    }

    public static  int getMarketSummaryInterval() {
        return TradeConfig.marketSummaryInterval;
    }

}
