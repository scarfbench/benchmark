package com.ibm.websphere.samples.daytrader.messaging;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.RestAssured;
import jakarta.inject.Inject;

import org.junit.jupiter.api.Test;

import com.ibm.websphere.samples.daytrader.util.MDBStats;
import com.ibm.websphere.samples.daytrader.util.TimerStat;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for JMS to Reactive Messaging migration.
 * 
 * These tests verify that:
 * 1. Messages can be sent via MessageProducerService
 * 2. Messages are received by DTBroker3MDB and DTStreamer3MDB
 * 3. MDBStats tracks message processing statistics
 */
@QuarkusTest
public class MessagingTest {

    @Inject
    MessageProducerService messageProducer;

    @Test
    public void testBrokerPing() throws Exception {
        // Reset stats
        MDBStats.getInstance().reset();
        
        // Send ping message
        messageProducer.sendBrokerPing("Test ping from MessagingTest");
        
        // Give time for async processing
        Thread.sleep(500);
        
        // Verify stats were recorded (using original MDB class name)
        TimerStat stats = MDBStats.getInstance().get("DTBroker3MDB:ping");
        assertNotNull(stats, "Ping stats should be recorded");
        assertTrue(stats.getCount() >= 1, "At least one ping should be processed");
    }

    @Test
    public void testStreamerPing() throws Exception {
        // Reset stats
        MDBStats.getInstance().reset();
        
        // Send ping message to streamer
        messageProducer.sendStreamerPing("Test ping to streamer");
        
        // Give time for async processing
        Thread.sleep(500);
        
        // Verify stats were recorded (using original MDB class name)
        TimerStat stats = MDBStats.getInstance().get("DTStreamer3MDB:ping");
        assertNotNull(stats, "Streamer ping stats should be recorded");
        assertTrue(stats.getCount() >= 1, "At least one streamer ping should be processed");
    }

    @Test
    public void testQuoteUpdate() throws Exception {
        // Reset stats
        MDBStats.getInstance().reset();
        
        // Publish quote update
        messageProducer.publishQuoteUpdate("s:0", 
            new java.math.BigDecimal("105.50"), 
            new java.math.BigDecimal("100.00"));
        
        // Give time for async processing
        Thread.sleep(500);
        
        // Verify stats were recorded (using original MDB class name)
        TimerStat stats = MDBStats.getInstance().get("DTStreamer3MDB:updateQuote");
        assertNotNull(stats, "Quote update stats should be recorded");
        assertTrue(stats.getCount() >= 1, "At least one quote update should be processed");
    }

    @Test
    public void testAsyncOrderProcessing() throws Exception {
        // First create an order via REST API
        // The TradeAppResource uses POST to /rest/app with action parameter
        String loginResponse = RestAssured.given()
            .formParam("action", "login")
            .formParam("uid", "uid:0")
            .formParam("passwd", "xxx")
            .when()
            .post("/rest/app")
            .then()
            .statusCode(200)
            .extract().body().asString();

        // Buy a stock (this creates an order)
        String buyResponse = RestAssured.given()
            .formParam("action", "buy")
            .formParam("symbol", "s:0")
            .formParam("quantity", "10")
            .when()
            .post("/rest/app")
            .then()
            .statusCode(200)
            .extract().body().asString();

        assertTrue(buyResponse.contains("Order") || buyResponse.contains("order"), 
            "Buy should create an order");
    }
}
