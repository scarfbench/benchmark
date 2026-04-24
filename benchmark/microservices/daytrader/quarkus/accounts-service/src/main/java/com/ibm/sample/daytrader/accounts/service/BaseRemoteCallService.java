package com.ibm.sample.daytrader.accounts.service;

import jakarta.ws.rs.*;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.Response;

public class BaseRemoteCallService {

    public static String invokeEndpoint(String url, String method, String body) throws Exception {
        return invokeEndpoint(url, method, body, -1);
    }

    public static String invokeEndpoint(String url, String method, String body, int connTimeOut) throws Exception {
        Response response = sendRequest(url, method, body, connTimeOut);
        int responseCode = response.getStatus();

        switch (responseCode) {
            case 400: throw new BadRequestException("Malformed message from : " + url);
            case 401: throw new NotAuthorizedException("Authentication failure from : " + url);
            case 403: throw new ForbiddenException("Not permitted to access from : " + url);
            case 404: throw new NotFoundException("Couldn't find resource from : " + url);
            case 405: throw new NotAllowedException("HTTP method not supported from : " + url);
            case 406: throw new NotAcceptableException("Client accepts media type not supported from : " + url);
            case 415: throw new NotSupportedException("Client produces media type not supported from : " + url);
            case 500: throw new InternalServerErrorException("General server error from : " + url);
            case 503: throw new NotAuthorizedException("Server is temporarily unavailable or busy from : " + url);
            default:
                if (responseCode >= 300 && responseCode <= 399)
                    throw new RedirectionException("A request redirection from : " + url, responseCode, null);
                if (responseCode >= 400 && responseCode <= 499)
                    throw new ClientErrorException("A client request error from : " + url, responseCode);
                if (responseCode >= 500 && responseCode <= 599)
                    throw new ServerErrorException("A server error from : " + url, responseCode);
        }

        String responseEntity = response.readEntity(String.class);
        response.close();
        return responseEntity;
    }

    public static Response sendRequest(String url, String method, String body, int connTimeOut) {
        Client client = ClientBuilder.newClient();
        if (connTimeOut > 0) {
            client.property("jersey.config.client.connectTimeout", connTimeOut);
        }
        WebTarget target = client.target(url);
        Response response = target.request().method(method, Entity.json(body));
        return response;
    }
}
