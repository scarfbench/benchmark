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

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import org.eclipse.microprofile.reactive.messaging.Incoming;

import com.ibm.websphere.samples.daytrader.interfaces.TradeServices;
import com.ibm.websphere.samples.daytrader.messaging.OrderMessage;
import com.ibm.websphere.samples.daytrader.util.Log;
import com.ibm.websphere.samples.daytrader.util.MDBStats;
import com.ibm.websphere.samples.daytrader.util.TimerStat;

/**
 * Message-Driven Bean for processing trade orders.
 * 
 * MIGRATION NOTES - Original Jakarta EE annotations:
 * --------------------------------------------------
 * @TransactionAttribute(TransactionAttributeType.REQUIRED)
 * @TransactionManagement(TransactionManagementType.CONTAINER)
 * @MessageDriven(activationConfig = { 
 *     @ActivationConfigProperty(propertyName = "acknowledgeMode", propertyValue = "Auto-acknowledge"),
 *     @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Queue"),
 *     @ActivationConfigProperty(propertyName = "destination", propertyValue = "jms/TradeBrokerQueue"),
 *     @ActivationConfigProperty(propertyName = "subscriptionDurability", propertyValue = "NonDurable") 
 * })
 * public class DTBroker3MDB implements MessageListener { ... }
 * 
 * Quarkus Migration:
 * ------------------
 * @ApplicationScoped replaces @MessageDriven
 * @Incoming("trade-broker-queue") replaces JMS destination config
 * @Transactional replaces @TransactionAttribute
 * OrderMessage POJO replaces JMS TextMessage
 */
@ApplicationScoped
public class DTBroker3MDB {
  
  private final MDBStats mdbStats;
  private int statInterval = 10000;

  // MIGRATION: @Resource MessageDrivenContext mdc -> Exception propagation for rollback
  // In Quarkus, throwing an exception triggers rollback instead of mdc.setRollbackOnly()

  @Inject
  TradeServices trade;

  public DTBroker3MDB() {
    if (statInterval <= 0) {
      statInterval = 10000;
    }
    mdbStats = MDBStats.getInstance();
  }   

  @PostConstruct
  void init() {
    Log.trace("DTBroker3MDB:init()");
  } 
  
  /**
   * Process incoming order messages.
   * 
   * MIGRATION: This replaces the JMS MessageListener.onMessage(Message) method.
   * - JMS TextMessage -> OrderMessage POJO
   * - message.getStringProperty("command") -> message.getCommand()
   * - message.getIntProperty("orderID") -> message.getOrderID()
   * - message.getBooleanProperty("twoPhase") -> message.isTwoPhase()
   * - message.getLongProperty("publishTime") -> message.getPublishTime()
   */
  @Incoming("trade-broker-queue")
  @Transactional
  public void onMessage(OrderMessage message) {
    try {

      Log.trace("TradeBroker:onMessage -- received message -->" + message.getText() + "command-->"
          + message.getCommand() + "<--");

      // MIGRATION: JMS redelivery check removed - Quarkus handles redelivery differently
      // if (message.getJMSRedelivered()) { ... }

      String command = message.getCommand();
      if (command == null) {
        Log.debug("DTBroker3MDB:onMessage -- received message with null command. Message-->" + message);
        return;
      }
      if (command.equalsIgnoreCase("neworder")) {
        /* Get the Order ID and complete the Order */
        Integer orderID = message.getOrderID();
        boolean twoPhase = message.isTwoPhase();
        // MIGRATION: 'direct' property removed - not used in Quarkus version
        long publishTime = message.getPublishTime();
        long receiveTime = System.currentTimeMillis();

        try {
          Log.trace("DTBroker3MDB:onMessage - completing order " + orderID + " twoPhase=" + twoPhase);

          trade.completeOrder(orderID, twoPhase);

          TimerStat currentStats = mdbStats.addTiming("DTBroker3MDB:neworder", publishTime, receiveTime);

          if ((currentStats.getCount() % statInterval) == 0) {
            Log.log(" DTBroker3MDB: processed " + statInterval + " stock trading orders." +
                " Total NewOrders process = " + currentStats.getCount() +
                "Time (in seconds):" +
                " min: " +currentStats.getMinSecs()+
                " max: " +currentStats.getMaxSecs()+
                " avg: " +currentStats.getAvgSecs());
          }
        } catch (Exception e) {
          Log.error("DTBroker3MDB:onMessage Exception completing order: " + orderID + "\n", e);
          // MIGRATION: mdc.setRollbackOnly() -> throw RuntimeException
          throw new RuntimeException("Failed to complete order: " + orderID, e);
        }
      } else if (command.equalsIgnoreCase("ping")) {

        Log.trace("DTBroker3MDB:onMessage  received test command -- message: " + message.getText());

        long publishTime = message.getPublishTime();
        long receiveTime = System.currentTimeMillis();

        TimerStat currentStats = mdbStats.addTiming("DTBroker3MDB:ping", publishTime, receiveTime);

        if ((currentStats.getCount() % statInterval) == 0) {
          Log.log(" DTBroker3MDB: received " + statInterval + " ping messages." +
              " Total ping message count = " + currentStats.getCount() +
              " Time (in seconds):" +
              " min: " +currentStats.getMinSecs()+
              " max: " +currentStats.getMaxSecs()+
              " avg: " +currentStats.getAvgSecs());
        }
      } else {
        Log.error("DTBroker3MDB:onMessage - unknown message request command-->" + command + "<-- message=" + message.getText());
      }
    } catch (Throwable t) {
      // JMS onMessage should handle all exceptions
      Log.error("DTBroker3MDB: Exception", t);
      // MIGRATION: mdc.setRollbackOnly() -> throw RuntimeException  
      throw new RuntimeException("DTBroker3MDB processing failed", t);
    }
  }
}
