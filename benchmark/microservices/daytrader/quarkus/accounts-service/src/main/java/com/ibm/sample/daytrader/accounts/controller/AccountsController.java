package com.ibm.sample.daytrader.accounts.controller;

import com.ibm.sample.daytrader.accounts.beans.RunStatsDataBean;
import com.ibm.sample.daytrader.accounts.service.AccountsService;
import com.ibm.sample.daytrader.accounts.utils.Log;
import com.ibm.sample.daytrader.accounts.entities.AccountDataBean;
import com.ibm.sample.daytrader.accounts.entities.AccountProfileDataBean;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.math.BigDecimal;
import java.util.Map;

@Path("/")
@ApplicationScoped
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AccountsController {

    @Inject
    AccountsService accountsService;

    @POST
    @Path("/accounts/test")
    public Response testAPIRequest(Map<String, String> valMap) {
        return Response.ok(valMap.get("key")).build();
    }

    @POST
    @Path("/accounts")
    public Response register(AccountDataBean accountData) {
        Log.traceEnter("AccountsController.register(" + accountData.getProfileID() + ")");
        String userID = accountData.getProfileID();
        String password = accountData.getProfile().getPassword();
        String fullname = accountData.getProfile().getFullName();
        String address = accountData.getProfile().getAddress();
        String email = accountData.getProfile().getEmail();
        String creditCard = accountData.getProfile().getCreditCard();
        BigDecimal openBalance = accountData.getOpenBalance();

        try {
            accountData = accountsService.register(userID, password, fullname, address, email, creditCard, openBalance);
            Log.traceExit("AccountsController.register()");
            return Response.status(Response.Status.CREATED)
                    .header("Cache-Control", "no-cache")
                    .entity(accountData).build();
        } catch (Throwable t) {
            Log.error("AccountsController.register()", t);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PUT
    @Path("/accounts/{userId}/profiles")
    public Response updateAccountProfile(@PathParam("userId") String userId, AccountProfileDataBean profileData) {
        Log.traceEnter("AccountsController.updateAccountProfile()");
        try {
            profileData = accountsService.updateAccountProfile(profileData);
            if (profileData != null) {
                Log.traceExit("AccountsController.updateAccountProfile()");
                return Response.ok(profileData).header("Cache-Control", "no-cache").build();
            } else {
                Log.traceExit("AccountsController.updateAccountProfile()");
                return Response.status(Response.Status.NO_CONTENT).header("Cache-Control", "no-cache").build();
            }
        } catch (Throwable t) {
            Log.error("AccountsController.updateAccountProfile()", t);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GET
    @Path("/accounts/{userId}/profiles")
    public Response getAccountProfileData(@PathParam("userId") String userId) {
        Log.traceEnter("AccountsController.getAccountProfileData()");
        AccountProfileDataBean profileData = null;
        try {
            profileData = accountsService.getAccountProfileData(userId);
            if (profileData != null) {
                Log.traceExit("AccountsController.getAccountProfileData()");
                return Response.ok(profileData).header("Cache-Control", "no-cache").build();
            } else {
                Log.traceExit("AccountsController.getAccountProfileData()");
                return Response.status(Response.Status.NO_CONTENT).header("Cache-Control", "no-cache").build();
            }
        } catch (Throwable t) {
            Log.error("AccountsController.getAccountProfileData()", t);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GET
    @Path("/accounts/{userId}")
    public Response getAccountData(@PathParam("userId") String userId) {
        Log.traceEnter("AccountsController.getAccountData()");
        AccountDataBean accountData = null;
        try {
            accountData = accountsService.getAccountData(userId);
            if (accountData != null) {
                Log.traceExit("AccountsController.getAccountData()");
                return Response.ok(accountData).header("Cache-Control", "no-cache").build();
            } else {
                Log.traceExit("AccountsController.getAccountData()");
                return Response.status(Response.Status.NO_CONTENT).header("Cache-Control", "no-cache").build();
            }
        } catch (Throwable t) {
            Log.error("AccountsController.getAccountData()", t);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PATCH
    @Path("/login/{userId}")
    public Response login(@PathParam("userId") String userId, String password) {
        Log.traceEnter("AccountsController.login()");
        AccountDataBean accountData = null;
        try {
            accountData = accountsService.login(userId, password);
            Log.traceExit("AccountsController.login()");
            return Response.ok(accountData).header("Cache-Control", "no-cache").build();
        } catch (jakarta.ws.rs.NotAuthorizedException nae) {
            Log.error("AccountsController.login()", nae);
            return Response.status(Response.Status.UNAUTHORIZED).build();
        } catch (Throwable t) {
            Log.error("AccountsController.login()", t);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PATCH
    @Path("/logout/{userId}")
    public Response logout(@PathParam("userId") String userId) {
        Log.traceEnter("AccountsController.logout()");
        try {
            accountsService.logout(userId);
            Log.traceExit("AccountsController.logout()");
            return Response.ok(true).build();
        } catch (Throwable t) {
            Log.error("AccountsController.logout()", t);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }

    @POST
    @Path("/admin/tradeBuildDB")
    public Response tradeBuildDB(@QueryParam("limit") Integer limit, @QueryParam("offset") Integer offset) {
        Log.traceEnter("AccountsController.tradeBuildDB()");
        Boolean result = false;
        try {
            result = accountsService.tradeBuildDB(limit.intValue(), offset.intValue());
            Log.traceExit("AccountsController.tradeBuildDB()");
            return Response.status(Response.Status.CREATED)
                    .header("Cache-Control", "no-cache")
                    .entity(result).build();
        } catch (Throwable t) {
            Log.error("AccountsController.tradeBuildDB()", t);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }

    @POST
    @Path("/admin/recreateDBTables")
    public Response recreateDBTables() {
        Log.traceEnter("AccountsController.recreateDBTables()");
        Boolean result = false;
        try {
            result = accountsService.recreateDBTables();
            Log.traceExit("AccountsController.recreateDBTables()");
            return Response.status(Response.Status.CREATED)
                    .header("Cache-Control", "no-cache")
                    .entity(result).build();
        } catch (Throwable t) {
            Log.error("AccountsController.recreateDBTables()", t);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GET
    @Path("/admin/resetTrade")
    public Response resetTrade(@QueryParam("deleteAll") Boolean deleteAll) {
        Log.traceEnter("AccountsController.resetTrade()");
        RunStatsDataBean runStatsData = null;
        try {
            runStatsData = accountsService.resetTrade(deleteAll);
            if (runStatsData != null) {
                Log.traceExit("AccountsController.resetTrade()");
                return Response.ok(runStatsData).header("Cache-Control", "no-cache").build();
            } else {
                Log.traceExit("AccountsController.resetTrade()");
                return Response.status(Response.Status.NO_CONTENT).header("Cache-Control", "no-cache").build();
            }
        } catch (Throwable t) {
            Log.error("AccountsController.resetTrade()", t);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }
}
