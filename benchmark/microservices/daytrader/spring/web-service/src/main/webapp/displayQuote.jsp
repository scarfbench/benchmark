<!--
 Licensed to the Apache Software Foundation (ASF) under one or more
  contributor license agreements.  See the NOTICE file distributed with
  this work for additional information regarding copyright ownership.
  The ASF licenses this file to You under the Apache License, Version 2.0
  (the "License"); you may not use this file except in compliance with
  the License.  You may obtain a copy of the License at

      http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
-->
<%@ page
    import="java.math.BigDecimal, com.ibm.sample.daytrader.core.direct.*, com.ibm.sample.daytrader.entities.*, com.ibm.sample.daytrader.utils.*,com.ibm.sample.daytrader.core.api.*, com.ibm.sample.daytrader.web.TradeAction"
    session="true" isThreadSafe="true" isErrorPage="false"%>
<%@ page import="com.ibm.sample.daytrader.core.api.TradeServices" %>
<%@ page import="com.ibm.sample.daytrader.core.direct.FinancialUtils" %>
<%@ page import="com.ibm.sample.daytrader.utils.Log" %>
<%@ page import="com.ibm.sample.daytrader.entities.QuoteDataBean" %>
<% 
    String symbol = request.getParameter("symbol");
    TradeServices tAction=null;
    tAction = new TradeAction();
    try { 
        QuoteDataBean quoteData = tAction.getQuote(symbol);

 %>
<TR align="center" bgcolor="#fafcb6">
    <TD><%= FinancialUtils.printQuoteLink(quoteData.getSymbol()) %></TD>
    <TD><%= quoteData.getCompanyName()%></TD>
    <TD><%= quoteData.getVolume()%></TD>
    <TD><%= quoteData.getLow() + " - " + quoteData.getHigh()%></TD>
    <TD nowrap><%= quoteData.getOpen()%></TD>
    <TD>$ <%= quoteData.getPrice()%></TD>
    <TD><%= FinancialUtils.printGainHTML(new BigDecimal(quoteData.getChange())) %>
    <%= FinancialUtils.printGainPercentHTML( FinancialUtils.computeGainPercent(quoteData.getPrice(), quoteData.getOpen())) %></TD>
    <TD>
    <FORM action=""><INPUT type="submit" name="action" value="buy"><INPUT
        type="hidden" name="symbol" value="<%= quoteData.getSymbol()%>"><INPUT
        size="4" type="text" name="quantity" value="100"></FORM>
    </TD>
</TR>

<%
    }
    catch (Exception e)
    {
        Log.error("displayQuote.jsp  exception", e);
    }
%>
