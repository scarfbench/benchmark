package com.ibm.sample.daytrader.gateway.service;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ibm.sample.daytrader.gateway.core.beans.RunStatsDataBean;
import com.ibm.sample.daytrader.gateway.entities.AccountDataBean;
import com.ibm.sample.daytrader.gateway.entities.AccountProfileDataBean;
import com.ibm.sample.daytrader.gateway.utils.Log;

import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import java.math.BigDecimal;

@ApplicationScoped
public class AccountsRemoteCallService extends BaseRemoteCallService {
    protected static ObjectMapper mapper;
    static { mapper = new ObjectMapper(); mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false); }

    @ConfigProperty(name = "daytrader.services.account", defaultValue = "http://localhost:1443")
    String accountsServiceRoute;

    public boolean tradeBuildDB(int limit, int offset) throws Exception {
        String url = accountsServiceRoute + "/admin/tradeBuildDB?limit="+limit+"&offset="+offset;
        Log.debug("AccountsRemoteCallService.tradeBuildDB() - " + url);
        String responseEntity = invokeEndpoint(url, "POST", "");
        return mapper.readValue(responseEntity, Boolean.class);
    }

    public RunStatsDataBean resetTrade(boolean deleteAll) throws Exception {
        String url = accountsServiceRoute + "/admin/resetTrade?deleteAll=" + deleteAll;
        Log.debug("AccountsRemoteCallService.resetTrade() - " + url);
        String responseEntity = invokeEndpoint(url, "GET", null);
        return mapper.readValue(responseEntity, RunStatsDataBean.class);
    }

    public boolean recreateDBTables() throws Exception {
        String url = accountsServiceRoute + "/admin/recreateDBTables";
        Log.debug("AccountsRemoteCallService.recreateDBTables() - " + url);
        String responseEntity = invokeEndpoint(url, "POST", "");
        return mapper.readValue(responseEntity, Boolean.class);
    }

    public AccountDataBean getAccountData(String userID) throws Exception {
        String url = accountsServiceRoute + "/accounts/" + userID;
        Log.debug("AccountsRemoteCallService.getAccountData() - " + url);
        String responseEntity = invokeEndpoint(url, "GET", null);
        return mapper.readValue(responseEntity, AccountDataBean.class);
    }

    public AccountProfileDataBean getAccountProfileData(String userID) throws Exception {
        String url = accountsServiceRoute + "/accounts/" + userID + "/profiles";
        Log.debug("AccountsRemoteCallService.getAccountProfileData() - " + url);
        String responseEntity = invokeEndpoint(url, "GET", null);
        return mapper.readValue(responseEntity, AccountProfileDataBean.class);
    }

    public AccountProfileDataBean updateAccountProfile(AccountProfileDataBean profileData) throws Exception {
        String url = accountsServiceRoute + "/accounts/" + profileData.getUserID() + "/profiles";
        Log.debug("AccountsRemoteCallService.updateAccountProfile() - " + url);
        String profileDataInString = mapper.writeValueAsString(profileData);
        String responseEntity = invokeEndpoint(url, "PUT", profileDataInString);
        return mapper.readValue(responseEntity, AccountProfileDataBean.class);
    }

    public AccountDataBean login(String userID, String password) throws Exception {
        String url = accountsServiceRoute + "/login/" + userID;
        Log.debug("AccountsRemoteCallService.login() - " + url);
        String responseEntity = invokeEndpoint(url, "PATCH", password);
        return mapper.readValue(responseEntity, AccountDataBean.class);
    }

    public void logout(String userID) throws Exception {
        String url = accountsServiceRoute + "/logout/" + userID;
        Log.debug("AccountsRemoteCallService.logout() - " + url);
        String responseEntity = invokeEndpoint(url, "PATCH", "");
        mapper.readValue(responseEntity, Boolean.class);
    }

    public AccountDataBean register(String userID, String password, String fullname,
            String address, String email, String creditCard, BigDecimal openBalance) throws Exception {
        String url = accountsServiceRoute + "/accounts";
        Log.debug("AccountsRemoteCallService.register() - " + url);
        AccountDataBean accountData = new AccountDataBean();
        accountData.setProfileID(userID);
        accountData.setOpenBalance(openBalance);
        accountData.setProfile(new AccountProfileDataBean());
        accountData.getProfile().setUserID(userID);
        accountData.getProfile().setPassword(password);
        accountData.getProfile().setFullName(fullname);
        accountData.getProfile().setAddress(address);
        accountData.getProfile().setEmail(email);
        accountData.getProfile().setCreditCard(creditCard);
        String accountDataInString = mapper.writeValueAsString(accountData);
        String responseEntity = invokeEndpoint(url, "POST", accountDataInString);
        return mapper.readValue(responseEntity, AccountDataBean.class);
    }
}
