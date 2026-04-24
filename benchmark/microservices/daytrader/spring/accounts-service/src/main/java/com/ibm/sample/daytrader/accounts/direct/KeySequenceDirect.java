/**
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.ibm.sample.daytrader.accounts.direct;

import java.util.Collection;
import java.util.Iterator;
import java.util.HashMap;
import java.util.ListIterator;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import com.ibm.sample.daytrader.accounts.utils.KeyBlock;
import com.ibm.sample.daytrader.accounts.utils.Log;
import com.ibm.sample.daytrader.accounts.utils.TradeConfig;

public class KeySequenceDirect {

    private static HashMap keyMap = new HashMap();
    private static Connection conn = null;

	// re-factored this class so that it manages its own
    // connection instead of commiting another pending transaction
    // that may be in progress.  
    public static synchronized void initialize(Connection conn) 
    {
    	KeySequenceDirect.keyMap = new HashMap();
       	try 
    	{
    		if (KeySequenceDirect.conn != null) KeySequenceDirect.conn.close();
    	} 
    	catch (Throwable t) 
    	{
     		Log.error("Ignored exception closing a connection.", t);
    	}
    	KeySequenceDirect.conn = conn;    	
    }

    public static synchronized Integer getNextID(String keyName)
        throws Exception {
        Integer nextID = null;
        // First verify we have allocated a block of keys
        // for this key name
        // Then verify the allocated block has not been depleted
        // allocate a new block if necessary
        if (keyMap.containsKey(keyName) == false) allocNewBlock(keyName);
        Collection block = (Collection) keyMap.get(keyName);

        Iterator ids = block.iterator();
        if (ids.hasNext() == false)
            ids = allocNewBlock(keyName).iterator();
        // get and return a new unique key
        nextID = (Integer) ids.next();

        if (Log.doTrace())
            Log.trace("KeySequenceDirect:getNextID()--> Returning new PK ID for Entity type: "
                + keyName + " ID=" + nextID);
        return nextID;
    }

    private static Collection allocNewBlock(String keyName) throws Exception 
    {
    	Collection block = null;
        try 
        {	
            PreparedStatement stmt = conn.prepareStatement(getKeyForUpdateSQL);
            stmt.setString(1, keyName);
            ResultSet rs = stmt.executeQuery();
            if (!rs.next()) 
            {
                // No keys found for this name - create a new one
                PreparedStatement stmt2 = conn.prepareStatement(createKeySQL);
                // fix unique constraint exception
                int keyVal = 0;
                if (keyMap.containsKey(keyName))
                {
                    KeyBlock innerBlock = (KeyBlock) keyMap.get(keyName);
                    ListIterator ids = innerBlock.listIterator();
                    if (ids.hasPrevious() == true)
                	   keyVal = (Integer)ids.previous() + 1; 
                }
                stmt2.setString(1, keyName);
                stmt2.setInt(2, keyVal);
                stmt2.executeUpdate();
                stmt2.close();
                stmt.close();
                stmt = conn.prepareStatement(getKeyForUpdateSQL);
                stmt.setString(1, keyName);
                rs = stmt.executeQuery();
                rs.next();
            }
            int keyVal = rs.getInt("keyval");
            stmt.close();

            stmt = conn.prepareStatement(updateKeyValueSQL);
            stmt.setInt(1, keyVal + TradeConfig.KEYBLOCKSIZE);
            stmt.setString(2, keyName);
            stmt.executeUpdate();
            stmt.close();

            block = new KeyBlock(keyVal, keyVal + TradeConfig.KEYBLOCKSIZE - 1);
            keyMap.put(keyName, block);
           	conn.commit();
        } catch (Exception e) {
            conn.rollback();
            throw new Exception("Failure to allocate new block of keys for entity: " + keyName, e);
        } finally {
            // Do not release this connection. Keygen will use it until re-initialized
        }
        return block;
    }

    private static final String getKeyForUpdateSQL = "select * from keygenejb kg where kg.keyname = ?  for update";

    private static final String createKeySQL =
        "insert into keygenejb " + "( keyname, keyval ) " + "VALUES (  ?  ,  ? )";

    private static final String updateKeyValueSQL = "update keygenejb set keyval = ? " + "where keyname = ?";

}
