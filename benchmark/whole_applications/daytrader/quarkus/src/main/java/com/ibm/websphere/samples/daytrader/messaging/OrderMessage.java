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

/**
 * MIGRATION NOTES:
 * 
 * Original: JMS TextMessage with properties (command, orderID, twoPhase, direct, publishTime)
 * Quarkus: Plain Java object (POJO) used with Reactive Messaging
 * 
 * In Jakarta EE, JMS messages were heavyweight with:
 * - message.setStringProperty("command", "neworder")
 * - message.setIntProperty("orderID", orderID)
 * - message.setBooleanProperty("twoPhase", twoPhase)
 * - message.setText(...)
 * 
 * In Quarkus, we use simple POJOs that are serialized automatically.
 * This is cleaner, type-safe, and doesn't require JMS infrastructure.
 */
public class OrderMessage {
    
    public static final String COMMAND_NEW_ORDER = "neworder";
    public static final String COMMAND_PING = "ping";
    
    private String command;
    private Integer orderID;
    private boolean twoPhase;
    private boolean direct;
    private long publishTime;
    private String text;
    
    // Default constructor for serialization
    public OrderMessage() {
    }
    
    // Factory method for new order messages
    public static OrderMessage newOrder(Integer orderID, boolean twoPhase) {
        OrderMessage msg = new OrderMessage();
        msg.command = COMMAND_NEW_ORDER;
        msg.orderID = orderID;
        msg.twoPhase = twoPhase;
        msg.direct = false;
        msg.publishTime = System.currentTimeMillis();
        msg.text = "Order message for orderID=" + orderID;
        return msg;
    }
    
    // Factory method for ping messages
    public static OrderMessage ping(String text) {
        OrderMessage msg = new OrderMessage();
        msg.command = COMMAND_PING;
        msg.publishTime = System.currentTimeMillis();
        msg.text = text;
        return msg;
    }
    
    // Getters and setters
    public String getCommand() {
        return command;
    }
    
    public void setCommand(String command) {
        this.command = command;
    }
    
    public Integer getOrderID() {
        return orderID;
    }
    
    public void setOrderID(Integer orderID) {
        this.orderID = orderID;
    }
    
    public boolean isTwoPhase() {
        return twoPhase;
    }
    
    public void setTwoPhase(boolean twoPhase) {
        this.twoPhase = twoPhase;
    }
    
    public boolean isDirect() {
        return direct;
    }
    
    public void setDirect(boolean direct) {
        this.direct = direct;
    }
    
    public long getPublishTime() {
        return publishTime;
    }
    
    public void setPublishTime(long publishTime) {
        this.publishTime = publishTime;
    }
    
    public String getText() {
        return text;
    }
    
    public void setText(String text) {
        this.text = text;
    }
    
    @Override
    public String toString() {
        return "OrderMessage{" +
                "command='" + command + '\'' +
                ", orderID=" + orderID +
                ", twoPhase=" + twoPhase +
                ", publishTime=" + publishTime +
                '}';
    }
}
