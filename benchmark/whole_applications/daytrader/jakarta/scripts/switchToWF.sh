#!/bin/bash
cd "$(dirname "$0")"

transform () {
  sed -i.bak "s#//@Resource(name = \"java#@Resource(name = \"java#" $1
  sed -i.bak "s#//@Resource(lookup = \"java#@Resource(lookup = \"java#" $1
  sed -i.bak "s#@Resource(name = \"jm#//@Resource(name = \"jm#" $1
  sed -i.bak "s#@Resource(lookup = \"jm#//@Resource(lookup = \"jm#" $1
  sed -i.bak "s#@Resource(lookup = \"jd#//@Resource(lookup = \"jd#" $1
  rm $1.bak
}

transform "../src/main/java/com/ibm/websphere/samples/daytrader/impl/ejb3/TradeSLSBBean.java"
transform "../src/main/java/com/ibm/websphere/samples/daytrader/impl/direct/TradeDirect.java"
transform "../src/main/java/com/ibm/websphere/samples/daytrader/impl/direct/TradeDirectDBUtils.java"


