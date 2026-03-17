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
package com.ibm.websphere.samples.daytrader.mdb;

import jakarta.enterprise.context.ApplicationScoped;

import org.eclipse.microprofile.reactive.messaging.Incoming;

import com.ibm.websphere.samples.daytrader.messaging.QuoteUpdateMessage;
import com.ibm.websphere.samples.daytrader.util.Log;
import com.ibm.websphere.samples.daytrader.util.MDBStats;
import com.ibm.websphere.samples.daytrader.util.TimerStat;

/**
 * Message-Driven Bean for processing quote update streams.
 * 
 * MIGRATION NOTES - Original Jakarta EE annotations:
 * --------------------------------------------------
 * @TransactionAttribute(TransactionAttributeType.REQUIRED)
 * @TransactionManagement(TransactionManagementType.CONTAINER)
 * @MessageDriven(activationConfig = { 
 *     @ActivationConfigProperty(propertyName = "acknowledgeMode", propertyValue = "Auto-acknowledge"),
 *     @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Topic"),
 *     @ActivationConfigProperty(propertyName = "destination", propertyValue = "jms/TradeStreamerTopic"),
 *     @ActivationConfigProperty(propertyName = "subscriptionDurability", propertyValue = "NonDurable") 
 * })
 * public class DTStreamer3MDB implements MessageListener { ... }
 * 
 * Quarkus Migration:
 * ------------------
 * @ApplicationScoped replaces @MessageDriven
 * @Incoming("trade-streamer-topic") replaces JMS destination config
 * QuoteUpdateMessage POJO replaces JMS TextMessage
 */
@ApplicationScoped
public class DTStreamer3MDB {

  private final MDBStats mdbStats;
  private int statInterval = 10000;

  // MIGRATION: @Resource MessageDrivenContext mdc -> Exception propagation for rollback
  // In Quarkus, throwing an exception triggers rollback instead of mdc.setRollbackOnly()

  /** Creates a new instance of TradeSteamerMDB */
  public DTStreamer3MDB() {
    Log.trace("DTStreamer3MDB:DTStreamer3MDB()");
    
    if (statInterval <= 0) {
      statInterval = 10000;
    }
    mdbStats = MDBStats.getInstance();
  }

  /**
   * Process incoming quote update messages.
   * 
   * MIGRATION: This replaces the JMS MessageListener.onMessage(Message) method.
   * - JMS TextMessage -> QuoteUpdateMessage POJO
   * - message.getStringProperty("command") -> message.getCommand()
   * - message.getStringProperty("symbol") -> message.getSymbol()
   * - message.getStringProperty("price") -> message.getNewPrice()
   * - message.getStringProperty("oldPrice") -> message.getOldPrice()
   * - message.getLongProperty("publishTime") -> message.getPublishTime()
   */
  @Incoming("trade-streamer-topic")
  public void onMessage(QuoteUpdateMessage message) {

    try {
      Log.trace("DTStreamer3MDB:onMessage -- received message -->" + message.getText() + "command-->"
          + message.getCommand() + "<--");
      
      String command = message.getCommand();
      if (command == null) {
        Log.debug("DTStreamer3MDB:onMessage -- received message with null command. Message-->" + message);
        return;
      }
      if (command.equalsIgnoreCase("updateQuote")) {
        Log.trace("DTStreamer3MDB:onMessage -- received message -->" + message.getText() + "\n\t symbol = "
              + message.getSymbol() + "\n\t current price =" + message.getPrice() + "\n\t old price ="
              + message.getOldPrice());
        
        long publishTime = message.getPublishTime();
        long receiveTime = System.currentTimeMillis();

        TimerStat currentStats = mdbStats.addTiming("DTStreamer3MDB:updateQuote", publishTime, receiveTime);

        if ((currentStats.getCount() % statInterval) == 0) {
          Log.log(" DTStreamer3MDB: " + statInterval + " prices updated:" +
              " Total message count = " + currentStats.getCount() +
              " Time (in seconds):" +
              " min: " +currentStats.getMinSecs()+
              " max: " +currentStats.getMaxSecs()+
              " avg: " +currentStats.getAvgSecs() );
        }
      } else if (command.equalsIgnoreCase("ping")) {
        Log.trace("DTStreamer3MDB:onMessage  received ping command -- message: " + message.getText());
        

        long publishTime = message.getPublishTime();
        long receiveTime = System.currentTimeMillis();

        TimerStat currentStats = mdbStats.addTiming("DTStreamer3MDB:ping", publishTime, receiveTime);

        if ((currentStats.getCount() % statInterval) == 0) {
          Log.log(" DTStreamer3MDB: received " + statInterval + " ping messages." +
              " Total message count = " + currentStats.getCount() +
              " Time (in seconds):" +
              " min: " +currentStats.getMinSecs()+
              " max: " +currentStats.getMaxSecs()+
              " avg: " +currentStats.getAvgSecs());
        }
      } else {
        Log.error("DTStreamer3MDB:onMessage - unknown message request command-->" + command + "<-- message=" + message.getText());
      }
    } catch (Throwable t) {
      // JMS onMessage should handle all exceptions
      Log.error("DTStreamer3MDB: Exception", t);
      // MIGRATION: mdc.setRollbackOnly() -> throw RuntimeException
      throw new RuntimeException("DTStreamer3MDB processing failed", t);
    }
  }

}
