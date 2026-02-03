/**
 * (C) Copyright IBM Corporation 2015.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.ibm.websphere.samples.daytrader.util;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Random;

/**
 * TradeConfig is a JavaBean holding all configuration and runtime parameters
 * for the Trade application.
 * 
 * MIGRATION NOTE: In Quarkus, configuration should ideally be externalized to
 * application.properties and injected via @ConfigProperty. This class is
 * simplified for migration purposes.
 */
public class TradeConfig {

    /* Trade Runtime Mode parameters */
    // Support original Jakarta runtime modes for compatibility
    private static String[] runTimeModeNames = { "Direct (JPA)", "Direct (JDBC)", "Full EJB3", "Session to Direct" };
    public static final int DIRECT_JPA = 0;
    public static final int DIRECT_JDBC = 1;
    public static final int DIRECT = DIRECT_JDBC;  // Alias for TradeDirect
    public static final int EJB3 = 2;
    public static final int SESSION_TO_DIRECT = 3;
    private static int runTimeMode = DIRECT_JPA;

    private static String[] orderProcessingModeNames = { "Sync", "Async", "Async 2-Phase" };
    public static final int SYNCH = 0;
    public static final int ASYNCH = 1;
    public static final int ASYNCH_2PHASE = 2;  // Two-phase commit mode
    private static int orderProcessingMode = SYNCH;
    
    // Quote price change publishing flag
    private static boolean publishQuotePriceChange = true;

    /* Trade Database Scaling parameters */
    private static int MAX_USERS = 200;
    private static int MAX_QUOTES = 400;

    /* Trade Config Miscellaneous items */
    public static int KEYBLOCKSIZE = 1000;
    public static int QUOTES_PER_PAGE = 10;
    public static boolean RND_USER = true;
    private static int MAX_HOLDINGS = 10;
    private static int count = 0;
    private static Object userID_count_semaphore = new Object();
    private static int userID_count = 0;
    private static String hostName = null;
    private static Random r0 = new Random(System.currentTimeMillis());
    private static Random randomNumberGenerator = r0;
    public static final String newUserPrefix = "ru:";
    public static final int verifyPercent = 5;
    private static boolean updateQuotePrices = true;
    private static int primIterations = 1;
    private static boolean longRun = true;
    private static boolean displayOrderAlerts = true;
    private static int marketSummaryInterval = 20;
    private static boolean trace = false;
    private static boolean actionTrace = false;
    private static int listQuotePriceChangeFrequency = 100;

    // Penny stock handling
    public static BigDecimal PENNY_STOCK_PRICE;
    public static BigDecimal PENNY_STOCK_RECOVERY_MIRACLE_MULTIPLIER;
    public static BigDecimal MAXIMUM_STOCK_PRICE;
    public static BigDecimal MAXIMUM_STOCK_SPLIT_MULTIPLIER;
    
    static {
        PENNY_STOCK_PRICE = new BigDecimal("0.01").setScale(2, RoundingMode.HALF_UP);
        PENNY_STOCK_RECOVERY_MIRACLE_MULTIPLIER = new BigDecimal("600.0").setScale(2, RoundingMode.HALF_UP);
        MAXIMUM_STOCK_PRICE = new BigDecimal("400").setScale(2, RoundingMode.HALF_UP);
        MAXIMUM_STOCK_SPLIT_MULTIPLIER = new BigDecimal("0.5").setScale(2, RoundingMode.HALF_UP);
    }

    /* Trade Scenario actions */
    public static final int HOME_OP = 0;
    public static final int QUOTE_OP = 1;
    public static final int LOGIN_OP = 2;
    public static final int LOGOUT_OP = 3;
    public static final int REGISTER_OP = 4;
    public static final int ACCOUNT_OP = 5;
    public static final int PORTFOLIO_OP = 6;
    public static final int BUY_OP = 7;
    public static final int SELL_OP = 8;
    public static final int UPDATEACCOUNT_OP = 9;

    private static int[][] scenarioMixes = {
        { 20, 40, 0, 4, 2, 10, 12, 4, 4, 4 }, // STANDARD
        { 20, 40, 0, 4, 2, 7, 7, 7, 7, 6 },   // High Volume
    };
    private static char[] actions = { 'h', 'q', 'l', 'o', 'r', 'a', 'p', 'b', 's', 'u' };
    private static int sellDeficit = 0;
    private static int scenarioCount = 0;

    // Card deck for user selection
    private static ArrayList<Integer> deck = null;
    private static int card = 0;

    private static final BigDecimal orderFee = new BigDecimal("24.95");
    private static final BigDecimal cashFee = new BigDecimal("0.0");
    private static final BigDecimal ONE = new BigDecimal(1.0);

    // Hostname
    private static String getHostname() {
        try {
            if (hostName == null) {
                hostName = java.net.InetAddress.getLocalHost().getHostName();
                try {
                    hostName = hostName.substring(0, hostName.indexOf('.'));
                } catch (Exception e) {
                    // ignore
                }
            }
        } catch (Exception e) {
            Log.error("Exception getting local host name using 'localhost' - ", e);
            hostName = "localhost";
        }
        return hostName;
    }

    public static String[] getRunTimeModeNames() {
        return runTimeModeNames;
    }

    public static char getScenarioAction(boolean newUser) {
        int r = rndInt(100);
        int i = 0;
        int sum = scenarioMixes[0][i];
        while (sum <= r) {
            i++;
            sum += scenarioMixes[0][i];
        }
        incrementScenarioCount();

        if ((!newUser) && (actions[i] == 'b')) {
            synchronized (TradeConfig.class) {
                if (sellDeficit > 0) {
                    sellDeficit--;
                    return 's';
                }
            }
        }
        return actions[i];
    }

    public static String getUserID() {
        return RND_USER ? rndUserID() : nextUserID();
    }

    public static BigDecimal getOrderFee(String orderType) {
        if ("BUY".equalsIgnoreCase(orderType) || "SELL".equalsIgnoreCase(orderType)) {
            return orderFee;
        }
        return cashFee;
    }

    public static synchronized void incrementSellDeficit() {
        sellDeficit++;
    }

    public static String nextUserID() {
        String userID;
        synchronized (userID_count_semaphore) {
            userID = "uid:" + userID_count;
            userID_count++;
            if (userID_count % MAX_USERS == 0) {
                userID_count = 0;
            }
        }
        return userID;
    }

    public static double random() {
        return randomNumberGenerator.nextDouble();
    }

    public static String rndAddress() {
        return rndInt(1000) + " Oak St.";
    }

    public static String rndBalance() {
        return "1000000";
    }

    public static String rndCreditCard() {
        return rndInt(100) + "-" + rndInt(1000) + "-" + rndInt(1000) + "-" + rndInt(1000);
    }

    public static String rndEmail(String userID) {
        return userID.replace(":", "") + "@" + rndInt(100) + ".com";
    }

    public static String rndFullName() {
        return "first:" + rndInt(1000) + " last:" + rndInt(5000);
    }

    public static int rndInt(int i) {
        return (int) (random() * i);
    }

    public static float rndFloat(int i) {
        return (float) (random() * i);
    }

    public static BigDecimal rndBigDecimal(float f) {
        return new BigDecimal(random() * f).setScale(2, RoundingMode.HALF_UP);
    }

    public static boolean rndBoolean() {
        return randomNumberGenerator.nextBoolean();
    }

    public static synchronized String rndNewUserID() {
        return newUserPrefix + getHostname() + System.currentTimeMillis() + count++;
    }

    public static float rndPrice() {
        return rndInt(200) + 1.0f;
    }

    public static BigDecimal getRandomPriceChangeFactor() {
        double percentGain = rndFloat(1) * 0.1;
        if (random() < .5) {
            percentGain *= -1;
        }
        percentGain += 1;

        BigDecimal percentGainBD = new BigDecimal(percentGain).setScale(2, RoundingMode.HALF_UP);
        if (percentGainBD.doubleValue() <= 0.0) {
            percentGainBD = ONE;
        }
        return percentGainBD;
    }

    public static float rndQuantity() {
        return rndInt(200) + 1.0f;
    }

    public static String rndSymbol() {
        return "s:" + rndInt(MAX_QUOTES - 1);
    }

    public static String rndSymbols() {
        StringBuilder symbols = new StringBuilder();
        int num_symbols = rndInt(QUOTES_PER_PAGE);

        for (int i = 0; i <= num_symbols; i++) {
            symbols.append("s:").append(rndInt(MAX_QUOTES - 1));
            if (i < num_symbols) {
                symbols.append(",");
            }
        }
        return symbols.toString();
    }

    public static String rndUserID() {
        return getNextUserIDFromDeck();
    }

    private static synchronized String getNextUserIDFromDeck() {
        int numUsers = getMAX_USERS();
        if (deck == null) {
            deck = new ArrayList<>(numUsers);
            for (int i = 0; i < numUsers; i++) {
                deck.add(i);
            }
            java.util.Collections.shuffle(deck, r0);
        }
        if (card >= numUsers) {
            card = 0;
        }
        return "uid:" + deck.get(card++);
    }

    // Getters and Setters
    public static String[] getOrderProcessingModeNames() {
        return orderProcessingModeNames;
    }

    public static int[][] getScenarioMixes() {
        return scenarioMixes;
    }

    public static int getMAX_USERS() {
        return MAX_USERS;
    }

    public static void setMAX_USERS(int maxUsers) {
        MAX_USERS = maxUsers;
        deck = null;
    }

    public static int getMAX_QUOTES() {
        return MAX_QUOTES;
    }

    public static void setMAX_QUOTES(int maxQuotes) {
        MAX_QUOTES = maxQuotes;
    }

    public static int getMAX_HOLDINGS() {
        return MAX_HOLDINGS;
    }

    public static void setMAX_HOLDINGS(int maxHoldings) {
        MAX_HOLDINGS = maxHoldings;
    }

    public static int getScenarioCount() {
        return scenarioCount;
    }

    public static void setScenarioCount(int count) {
        scenarioCount = count;
    }

    public static synchronized void incrementScenarioCount() {
        scenarioCount++;
    }

    public static boolean getUpdateQuotePrices() {
        return updateQuotePrices;
    }

    public static void setUpdateQuotePrices(boolean update) {
        updateQuotePrices = update;
    }

    public static boolean getPublishQuotePriceChange() {
        return publishQuotePriceChange;
    }

    public static void setPublishQuotePriceChange(boolean publish) {
        publishQuotePriceChange = publish;
    }

    public static int getPrimIterations() {
        return primIterations;
    }

    public static void setPrimIterations(int iter) {
        primIterations = iter;
    }

    public static boolean getLongRun() {
        return longRun;
    }

    public static void setLongRun(boolean run) {
        longRun = run;
    }

    public static void setMarketSummaryInterval(int seconds) {
        marketSummaryInterval = seconds;
    }

    public static int getMarketSummaryInterval() {
        return marketSummaryInterval;
    }

    public static void setRunTimeMode(int value) {
        runTimeMode = value;
    }

    public static int getRunTimeMode() {
        return runTimeMode;
    }

    public static void setOrderProcessingMode(int value) {
        orderProcessingMode = value;
    }

    public static int getOrderProcessingMode() {
        return orderProcessingMode;
    }

    public static void setDisplayOrderAlerts(boolean value) {
        displayOrderAlerts = value;
    }

    public static boolean getDisplayOrderAlerts() {
        return displayOrderAlerts;
    }

    public static boolean getTrace() {
        return trace;
    }

    public static void setTrace(boolean value) {
        trace = value;
    }

    public static boolean getActionTrace() {
        return actionTrace;
    }

    public static void setActionTrace(boolean value) {
        actionTrace = value;
    }

    public static int getListQuotePriceChangeFrequency() {
        return listQuotePriceChangeFrequency;
    }

    public static void setListQuotePriceChangeFrequency(int value) {
        listQuotePriceChangeFrequency = value;
    }
}
