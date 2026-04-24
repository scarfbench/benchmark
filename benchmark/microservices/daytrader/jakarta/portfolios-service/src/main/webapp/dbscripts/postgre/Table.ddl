--    Licensed to the Apache Software Foundation (ASF) under one or more
--    contributor license agreements.  See the NOTICE file distributed with
--    this work for additional information regarding copyright ownership.
--    The ASF licenses this file to You under the Apache License, Version 2.0
--    (the "License"); you may not use this file except in compliance with
--    the License.  You may obtain a copy of the License at
--
--       http://www.apache.org/licenses/LICENSE-2.0
--
--    Unless required by applicable law or agreed to in writing, software
--    distributed under the License is distributed on an "AS IS" BASIS,
--    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
--    See the License for the specific language governing permissions and
--    limitations under the License.

-- Each SQL statement in this file should terminate with a semicolon (;)
-- Lines starting with the pound character (#) are considered as comments

-- If you first time initialize postgre db, the drop statements should be commented out as following.
-- DROP TABLE holdingejb;
-- DROP TABLE accountprofileejb;
-- DROP TABLE quoteejb;
-- DROP TABLE keygenejb;
-- DROP TABLE accountejb;
-- DROP TABLE orderejb;

CREATE TABLE holdingejb
  (PURCHASEPRICE DECIMAL(14, 2),
   HOLDINGID INTEGER NOT NULL,
   QUANTITY DOUBLE PRECISION NOT NULL,
   PURCHASEDATE TIMESTAMP,
   ACCOUNT_ACCOUNTID INTEGER,
   QUOTE_SYMBOL VARCHAR(255));

ALTER TABLE holdingejb 
    ADD CONSTRAINT PK_HOLDINGEJB PRIMARY KEY (HOLDINGID);

CREATE TABLE accountprofileejb
  (ADDRESS VARCHAR(255),
   PASSWD VARCHAR(255),
   USERID VARCHAR(255) NOT NULL,
   EMAIL VARCHAR(255),
   CREDITCARD VARCHAR(255),
   FULLNAME VARCHAR(255));

ALTER TABLE accountprofileejb 
    ADD CONSTRAINT PK_ACCOUNTPROFILE2 PRIMARY KEY (USERID);

CREATE TABLE quoteejb
  (LOW DECIMAL(14, 2),
   OPEN1 DECIMAL(14, 2),
   VOLUME DOUBLE PRECISION NOT NULL,
   PRICE DECIMAL(14, 2),
   HIGH DECIMAL(14, 2),
   COMPANYNAME VARCHAR(255),
   SYMBOL VARCHAR(255) NOT NULL,
   CHANGE1 DOUBLE PRECISION NOT NULL);

ALTER TABLE quoteejb 
    ADD CONSTRAINT PK_QUOTEEJB PRIMARY KEY (SYMBOL);

CREATE TABLE keygenejb
  (KEYVAL INTEGER NOT NULL,
   KEYNAME VARCHAR(255) NOT NULL);

ALTER TABLE keygenejb 
    ADD CONSTRAINT PK_KEYGENEJB PRIMARY KEY (KEYNAME);

CREATE TABLE accountejb
  (CREATIONDATE TIMESTAMP,
   OPENBALANCE DECIMAL(14, 2),
   LOGOUTCOUNT INTEGER NOT NULL,
   BALANCE DECIMAL(14, 2),
   ACCOUNTID INTEGER NOT NULL,
   LASTLOGIN TIMESTAMP,
   LOGINCOUNT INTEGER NOT NULL,
   PROFILE_USERID VARCHAR(255));

ALTER TABLE accountejb 
    ADD CONSTRAINT PK_ACCOUNTEJB PRIMARY KEY (ACCOUNTID);

CREATE TABLE orderejb
  (ORDERFEE DECIMAL(14, 2),
   COMPLETIONDATE TIMESTAMP,
   ORDERTYPE VARCHAR(255),
   ORDERSTATUS VARCHAR(255),
   PRICE DECIMAL(14, 2),
   QUANTITY DOUBLE PRECISION NOT NULL,
   OPENDATE TIMESTAMP,
   ORDERID INTEGER NOT NULL,
   ACCOUNT_ACCOUNTID INTEGER,
   QUOTE_SYMBOL VARCHAR(255),
   HOLDING_HOLDINGID INTEGER);

ALTER TABLE orderejb 
    ADD CONSTRAINT PK_ORDEREJB PRIMARY KEY (ORDERID);

CREATE INDEX ACCOUNT_USERID ON accountejb(PROFILE_USERID);
CREATE INDEX HOLDING_ACCOUNTID ON holdingejb(ACCOUNT_ACCOUNTID);
CREATE INDEX ORDER_ACCOUNTID ON orderejb(ACCOUNT_ACCOUNTID);
CREATE INDEX ORDER_HOLDINGID ON orderejb(HOLDING_HOLDINGID);
CREATE INDEX CLOSED_ORDERS ON orderejb(ACCOUNT_ACCOUNTID,ORDERSTATUS);
