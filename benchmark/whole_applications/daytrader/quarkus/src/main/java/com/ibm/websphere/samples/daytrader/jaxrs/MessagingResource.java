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
package com.ibm.websphere.samples.daytrader.jaxrs;

import java.util.HashMap;
import java.util.Map;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import com.ibm.websphere.samples.daytrader.messaging.MessageProducerService;
import com.ibm.websphere.samples.daytrader.util.MDBStats;
import com.ibm.websphere.samples.daytrader.util.TimerStat;

/**
 * REST Resource for testing and monitoring messaging functionality.
 * 
 * MIGRATION NOTES:
 * 
 * This replaces PingServlet2MDBQueue and PingServlet2MDBTopic servlets
 * which were used to test JMS messaging in the original application.
 * 
 * Original endpoints:
 *   /ejb3/PingServlet2MDBQueue - Send test message to TradeBrokerQueue
 *   /ejb3/PingServlet2MDBTopic - Send test message to TradeStreamerTopic
 * 
 * Quarkus endpoints:
 *   /rest/messaging/ping/broker - Send ping to broker queue
 *   /rest/messaging/ping/streamer - Send ping to streamer topic
 *   /rest/messaging/stats - Get messaging statistics
 */
@Path("/messaging")
@Produces(MediaType.APPLICATION_JSON)
public class MessagingResource {

    @Inject
    MessageProducerService messageProducer;

    // Use singleton pattern - original MDBStats is not CDI-managed
    private final MDBStats mdbStats = MDBStats.getInstance();

    /**
     * Send a ping message to the trade broker queue.
     * Replaces PingServlet2MDBQueue functionality.
     */
    @POST
    @Path("/ping/broker")
    public Response pingBroker(@QueryParam("message") String message) {
        if (message == null || message.isEmpty()) {
            message = "Ping from MessagingResource at " + System.currentTimeMillis();
        }
        
        messageProducer.sendBrokerPing(message);
        
        Map<String, Object> response = new HashMap<>();
        response.put("status", "sent");
        response.put("destination", "trade-broker-queue");
        response.put("message", message);
        response.put("timestamp", System.currentTimeMillis());
        
        return Response.ok(response).build();
    }

    /**
     * Send a ping message to the trade streamer topic.
     * Replaces PingServlet2MDBTopic functionality.
     */
    @POST
    @Path("/ping/streamer")
    public Response pingStreamer(@QueryParam("message") String message) {
        if (message == null || message.isEmpty()) {
            message = "Ping from MessagingResource at " + System.currentTimeMillis();
        }
        
        messageProducer.sendStreamerPing(message);
        
        Map<String, Object> response = new HashMap<>();
        response.put("status", "sent");
        response.put("destination", "trade-streamer-topic");
        response.put("message", message);
        response.put("timestamp", System.currentTimeMillis());
        
        return Response.ok(response).build();
    }

    /**
     * Get messaging statistics.
     * Shows statistics for processed messages, timing information, etc.
     */
    @GET
    @Path("/stats")
    public Response getStats() {
        Map<String, Object> response = new HashMap<>();
        
        // MDBStats extends HashMap<String, TimerStat> so iterate directly
        Map<String, Map<String, Object>> formattedStats = new HashMap<>();
        
        for (Map.Entry<String, TimerStat> entry : mdbStats.entrySet()) {
            TimerStat stat = entry.getValue();
            Map<String, Object> statMap = new HashMap<>();
            statMap.put("count", stat.getCount());
            statMap.put("minSeconds", stat.getMinSecs());
            statMap.put("maxSeconds", stat.getMaxSecs());
            statMap.put("avgSeconds", stat.getAvgSecs());
            formattedStats.put(entry.getKey(), statMap);
        }
        
        response.put("statistics", formattedStats);
        response.put("timestamp", System.currentTimeMillis());
        
        return Response.ok(response).build();
    }

    /**
     * Reset messaging statistics.
     */
    @POST
    @Path("/stats/reset")
    public Response resetStats() {
        mdbStats.reset();
        
        Map<String, Object> response = new HashMap<>();
        response.put("status", "reset");
        response.put("timestamp", System.currentTimeMillis());
        
        return Response.ok(response).build();
    }
}
