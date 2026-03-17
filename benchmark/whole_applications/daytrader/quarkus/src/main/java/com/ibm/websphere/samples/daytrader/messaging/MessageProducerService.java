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

import java.math.BigDecimal;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;

import com.ibm.websphere.samples.daytrader.util.Log;

/**
 * MIGRATION NOTES:
 * 
 * Original (Jakarta EE - JMS Producer Pattern):
 * ---------------------------------------------
 * @Resource(name = "jms/QueueConnectionFactory")
 * private ConnectionFactory queueConnectionFactory;
 * 
 * @Resource(name = "jms/TradeBrokerQueue")
 * private Queue tradeBrokerQueue;
 * 
 * // Usage:
 * JMSContext context = queueConnectionFactory.createContext();
 * TextMessage message = context.createTextMessage();
 * message.setStringProperty("command", "neworder");
 * message.setIntProperty("orderID", orderID);
 * message.setBooleanProperty("twoPhase", twoPhase);
 * message.setLongProperty("publishTime", System.currentTimeMillis());
 * message.setText("Order message...");
 * context.createProducer().send(tradeBrokerQueue, message);
 * 
 * Quarkus (SmallRye Reactive Messaging - Emitter Pattern):
 * --------------------------------------------------------
 * @Inject
 * @Channel("trade-broker-queue")
 * Emitter<OrderMessage> brokerEmitter;
 * 
 * // Usage:
 * OrderMessage message = OrderMessage.newOrder(orderID, twoPhase);
 * brokerEmitter.send(message);
 * 
 * Key Differences:
 * 1. No ConnectionFactory/JMSContext - just @Channel + Emitter
 * 2. No manual message creation - just create POJO
 * 3. No explicit property setting - POJO fields are the properties
 * 4. Cleaner, more type-safe code
 * 5. Built-in backpressure handling
 */
@ApplicationScoped
public class MessageProducerService {

    @Inject
    @Channel("trade-broker-queue")
    @io.smallrye.reactive.messaging.annotations.Broadcast
    Emitter<OrderMessage> brokerEmitter;

    @Inject
    @Channel("trade-streamer-topic")
    @io.smallrye.reactive.messaging.annotations.Broadcast
    Emitter<QuoteUpdateMessage> streamerEmitter;

    /**
     * Queue a new order for asynchronous processing.
     * This replaces the JMS Queue send in the original DTBroker pattern.
     * 
     * @param orderID the order ID to process
     * @param twoPhase whether to use two-phase commit
     */
    public void queueOrderForProcessing(Integer orderID, boolean twoPhase) {
        OrderMessage message = OrderMessage.newOrder(orderID, twoPhase);
        Log.trace("MessageProducerService:queueOrderForProcessing - sending order " + orderID);
        brokerEmitter.send(message);
    }

    /**
     * Send a ping message to the trade broker queue.
     * Used for testing and performance measurement.
     * 
     * @param text the ping message text
     */
    public void sendBrokerPing(String text) {
        OrderMessage message = OrderMessage.ping(text);
        Log.trace("MessageProducerService:sendBrokerPing - sending ping");
        brokerEmitter.send(message);
    }

    /**
     * Publish a quote update to the streamer topic.
     * This replaces the JMS Topic publish in the original DTStreamer pattern.
     * 
     * @param symbol the stock symbol
     * @param newPrice the new price
     * @param oldPrice the old price
     */
    public void publishQuoteUpdate(String symbol, BigDecimal newPrice, BigDecimal oldPrice) {
        QuoteUpdateMessage message = QuoteUpdateMessage.quoteUpdate(symbol, newPrice, oldPrice);
        Log.trace("MessageProducerService:publishQuoteUpdate - publishing update for " + symbol);
        streamerEmitter.send(message);
    }

    /**
     * Publish a complete quote price change to the streamer topic.
     * This replaces the full JMS Topic publish in the original TradeDirect pattern.
     */
    public void publishQuotePriceChange(String symbol, String company, BigDecimal price,
            BigDecimal oldPrice, BigDecimal open, BigDecimal low, BigDecimal high,
            double volume, BigDecimal changeFactor, double sharesTraded) {
        QuoteUpdateMessage message = QuoteUpdateMessage.quoteUpdate(symbol, price, oldPrice);
        message.setCompany(company);
        message.setOpen(open);
        message.setLow(low);
        message.setHigh(high);
        message.setVolume(volume);
        message.setChangeFactor(changeFactor);
        message.setSharesTraded(sharesTraded);
        Log.trace("MessageProducerService:publishQuotePriceChange - publishing update for " + symbol);
        streamerEmitter.send(message);
    }

    /**
     * Send a ping message to the streamer topic.
     * Used for testing and performance measurement.
     * 
     * @param text the ping message text
     */
    public void sendStreamerPing(String text) {
        QuoteUpdateMessage message = QuoteUpdateMessage.ping(text);
        Log.trace("MessageProducerService:sendStreamerPing - sending ping");
        streamerEmitter.send(message);
    }
}
