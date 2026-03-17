package spring.examples.tutorial.helloservice;

import jakarta.tutorial.helloservice.ejb.SayHello;
import jakarta.tutorial.helloservice.ejb.SayHelloResponse;
import org.springframework.ws.server.endpoint.annotation.Endpoint;
import org.springframework.ws.server.endpoint.annotation.PayloadRoot;
import org.springframework.ws.server.endpoint.annotation.RequestPayload;
import org.springframework.ws.server.endpoint.annotation.ResponsePayload;

@Endpoint
public class HelloServiceBean {

    private static final String NAMESPACE_URI = "http://ejb.helloservice.tutorial.jakarta/";
    private final String message = "Hello, ";

    @PayloadRoot(namespace = NAMESPACE_URI, localPart = "sayHello")
    @ResponsePayload
    public SayHelloResponse sayHello(@RequestPayload SayHello request) {
        SayHelloResponse response = new SayHelloResponse();
        response.setReturn(message + request.getArg0() + ".");
        return response;
    }

}
