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

/**
 * MIGRATION NOTES:
 * 
 * Original: JMS TextMessage published to jms/TradeStreamerTopic
 * Properties: command, symbol, price, oldPrice, publishTime
 * 
 * Quarkus: Simple POJO with same information, used with Reactive Messaging.
 * 
 * The DTStreamer3MDB consumed topic messages for:
 * - "updateQuote" command: Log price changes
 * - "ping" command: Health check / performance testing
 */
public class QuoteUpdateMessage {
    
    public static final String COMMAND_UPDATE_QUOTE = "updateQuote";
    public static final String COMMAND_PING = "ping";
    
    private String command;
    private String symbol;
    private BigDecimal price;
    private BigDecimal oldPrice;
    private long publishTime;
    private String text;
    
    // Additional fields for full quote update (matching original JMS message properties)
    private String company;
    private BigDecimal open;
    private BigDecimal low;
    private BigDecimal high;
    private double volume;
    private BigDecimal changeFactor;
    private double sharesTraded;
    
    // Default constructor for serialization
    public QuoteUpdateMessage() {
    }
    
    // Factory method for quote update
    public static QuoteUpdateMessage quoteUpdate(String symbol, BigDecimal newPrice, BigDecimal oldPrice) {
        QuoteUpdateMessage msg = new QuoteUpdateMessage();
        msg.command = COMMAND_UPDATE_QUOTE;
        msg.symbol = symbol;
        msg.price = newPrice;
        msg.oldPrice = oldPrice;
        msg.publishTime = System.currentTimeMillis();
        msg.text = "Quote update for " + symbol + ": " + oldPrice + " -> " + newPrice;
        return msg;
    }
    
    // Factory method for ping
    public static QuoteUpdateMessage ping(String text) {
        QuoteUpdateMessage msg = new QuoteUpdateMessage();
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
    
    public String getSymbol() {
        return symbol;
    }
    
    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }
    
    public BigDecimal getPrice() {
        return price;
    }
    
    public void setPrice(BigDecimal price) {
        this.price = price;
    }
    
    public BigDecimal getOldPrice() {
        return oldPrice;
    }
    
    public void setOldPrice(BigDecimal oldPrice) {
        this.oldPrice = oldPrice;
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
    
    // Additional getters/setters for full quote update fields
    public String getCompany() {
        return company;
    }
    
    public void setCompany(String company) {
        this.company = company;
    }
    
    public BigDecimal getOpen() {
        return open;
    }
    
    public void setOpen(BigDecimal open) {
        this.open = open;
    }
    
    public BigDecimal getLow() {
        return low;
    }
    
    public void setLow(BigDecimal low) {
        this.low = low;
    }
    
    public BigDecimal getHigh() {
        return high;
    }
    
    public void setHigh(BigDecimal high) {
        this.high = high;
    }
    
    public double getVolume() {
        return volume;
    }
    
    public void setVolume(double volume) {
        this.volume = volume;
    }
    
    public BigDecimal getChangeFactor() {
        return changeFactor;
    }
    
    public void setChangeFactor(BigDecimal changeFactor) {
        this.changeFactor = changeFactor;
    }
    
    public double getSharesTraded() {
        return sharesTraded;
    }
    
    public void setSharesTraded(double sharesTraded) {
        this.sharesTraded = sharesTraded;
    }
    
    @Override
    public String toString() {
        return "QuoteUpdateMessage{" +
                "command='" + command + '\'' +
                ", symbol='" + symbol + '\'' +
                ", price=" + price +
                ", oldPrice=" + oldPrice +
                ", publishTime=" + publishTime +
                '}';
    }
}
