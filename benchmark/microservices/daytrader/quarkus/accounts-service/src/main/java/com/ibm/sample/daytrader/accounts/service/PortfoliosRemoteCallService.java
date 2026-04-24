package com.ibm.sample.daytrader.accounts.service;

import com.ibm.sample.daytrader.accounts.entities.AccountDataBean;
import com.ibm.sample.daytrader.accounts.utils.Log;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@ApplicationScoped
public class PortfoliosRemoteCallService extends BaseRemoteCallService {

    protected static ObjectMapper mapper;
    static {
        mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    @ConfigProperty(name = "daytrader.services.portfolios", defaultValue = "http://localhost:3443")
    String portfoliosServiceRoute;

    public AccountDataBean getAccountData(String userID) throws Exception {
        String url = portfoliosServiceRoute + "/portfolios/" + userID;
        Log.debug("PortfoliosRemoteCallService.getAccountData() - " + url);
        String responseEntity = invokeEndpoint(url, "GET", null);
        return mapper.readValue(responseEntity, AccountDataBean.class);
    }

    public AccountDataBean register(AccountDataBean accountData) throws Exception {
        String url = portfoliosServiceRoute + "/portfolios";
        Log.debug("PortfoliosRemoteCallService.register() - " + url);
        String accountDataInString = mapper.writeValueAsString(accountData);
        String responseEntity = invokeEndpoint(url, "POST", accountDataInString);
        return mapper.readValue(responseEntity, AccountDataBean.class);
    }
}
