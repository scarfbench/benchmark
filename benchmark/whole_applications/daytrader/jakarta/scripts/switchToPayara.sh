#!/bin/bash
cd "$(dirname "$0")"

transform () {
  sed -i.bak "s#//@ActivationConfigProperty(propertyName = \"destination\", propertyValue = \"T#@ActivationConfigProperty(propertyName = \"destination\", propertyValue = \"T#" $1
  sed -i.bak "s#@ActivationConfigProperty(propertyName = \"destination\", propertyValue = \"j#//@ActivationConfigProperty(propertyName = \"destination\", propertyValue = \"j#" $1
  rm $1.bak
}

transform "../src/main/java/com/ibm/websphere/samples/daytrader/mdb/DTBroker3MDB.java"
transform "../src/main/java/com/ibm/websphere/samples/daytrader/mdb/DTStreamer3MDB.java"

mv ../src/main/java/com/ibm/websphere/samples/daytrader/web/prims/ejb3/PingServlet2MDBQueue.java ../src/main/java/com/ibm/websphere/samples/daytrader/web/prims/ejb3/PingServlet2MDBQueue.java_bak
mv ../src/main/java/com/ibm/websphere/samples/daytrader/web/prims/ejb3/PingServlet2MDBTopic.java ../src/main/java/com/ibm/websphere/samples/daytrader/web/prims/ejb3/PingServlet2MDBTopic.java_bak


