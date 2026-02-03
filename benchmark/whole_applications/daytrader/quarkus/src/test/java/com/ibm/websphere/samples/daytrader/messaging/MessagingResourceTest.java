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
package com.ibm.websphere.samples.daytrader.messaging;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.*;

/**
 * Tests for the messaging functionality.
 * These tests verify that the JMS-to-Reactive-Messaging migration works correctly.
 * 
 * MIGRATION NOTES:
 * These tests replace manual testing of:
 * - PingServlet2MDBQueue -> /rest/messaging/ping/broker
 * - PingServlet2MDBTopic -> /rest/messaging/ping/streamer
 */
@QuarkusTest
public class MessagingResourceTest {

    @Test
    public void testPingBroker() {
        given()
            .contentType(ContentType.JSON)
            .queryParam("message", "Test ping to broker")
        .when()
            .post("/rest/messaging/ping/broker")
        .then()
            .statusCode(200)
            .body("status", is("sent"))
            .body("destination", is("trade-broker-queue"))
            .body("message", is("Test ping to broker"));
    }

    @Test
    public void testPingBrokerWithDefaultMessage() {
        given()
            .contentType(ContentType.JSON)
        .when()
            .post("/rest/messaging/ping/broker")
        .then()
            .statusCode(200)
            .body("status", is("sent"))
            .body("destination", is("trade-broker-queue"))
            .body("message", containsString("Ping from MessagingResource"));
    }

    @Test
    public void testPingStreamer() {
        given()
            .contentType(ContentType.JSON)
            .queryParam("message", "Test ping to streamer")
        .when()
            .post("/rest/messaging/ping/streamer")
        .then()
            .statusCode(200)
            .body("status", is("sent"))
            .body("destination", is("trade-streamer-topic"))
            .body("message", is("Test ping to streamer"));
    }

    @Test
    public void testGetStats() {
        // First send some messages to generate stats
        given()
            .contentType(ContentType.JSON)
        .when()
            .post("/rest/messaging/ping/broker");

        given()
            .contentType(ContentType.JSON)
        .when()
            .post("/rest/messaging/ping/streamer");

        // Wait a bit for async processing
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Then check stats
        given()
            .contentType(ContentType.JSON)
        .when()
            .get("/rest/messaging/stats")
        .then()
            .statusCode(200)
            .body("statistics", notNullValue())
            .body("timestamp", notNullValue());
    }

    @Test
    public void testResetStats() {
        given()
            .contentType(ContentType.JSON)
        .when()
            .post("/rest/messaging/stats/reset")
        .then()
            .statusCode(200)
            .body("status", is("reset"));
    }
}
