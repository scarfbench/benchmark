package org.example.realworldapi.integration;

import jakarta.ws.rs.core.MediaType;
import org.apache.http.HttpStatus;
import org.example.realworldapi.AbstractIT;
import org.example.realworldapi.application.web.model.request.LoginRequest;
import org.example.realworldapi.application.web.model.request.LoginRequestWrapper;
import org.example.realworldapi.application.web.model.request.NewUserRequest;
import org.example.realworldapi.application.web.model.request.NewUserRequestWrapper;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.example.realworldapi.constants.TestConstants.API_PREFIX;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.hasSize;

public class UsersResourceIT extends AbstractIT {

    private final String USERS_RESOURCE_PATH = API_PREFIX + "/users";
    private final String LOGIN_PATH = USERS_RESOURCE_PATH + "/login";

    @Test
    public void givenAValidUser_whenCallingRegisterUserEndpoint_thenReturnAnUserWithTokenFieldAndCode201() {

        NewUserRequest newUserRequest = new NewUserRequest();
        newUserRequest.setUsername("user");
        newUserRequest.setEmail("user@mail.com");
        newUserRequest.setPassword("user123");

        NewUserRequestWrapper newUserRequestWrapper = new NewUserRequestWrapper(newUserRequest);

        given().contentType(MediaType.APPLICATION_JSON).body(newUserRequestWrapper).when().post(USERS_RESOURCE_PATH).then().statusCode(HttpStatus.SC_CREATED).body("user.id", Matchers.nullValue(), "user.password", Matchers.nullValue(), "user.username", Matchers.notNullValue(), "user.email", Matchers.notNullValue(), "user.token", Matchers.notNullValue(), "user.bio", Matchers.nullValue(), "user.image", Matchers.nullValue());
    }

    @Test
    public void givenAPersistedUser_whenCallingRegisterUserEndpointWithExistingEmail_thenReturnCode409() {

        String userPassword = "123";

        final var user = createUserEntity("user1", "user1@mail.com", "bio", "image", userPassword);

        NewUserRequest newUserRequest = new NewUserRequest();
        newUserRequest.setUsername("user2");
        newUserRequest.setEmail(user.getEmail());
        newUserRequest.setPassword("user123");

        NewUserRequestWrapper newUserRequestWrapper = new NewUserRequestWrapper(newUserRequest);

        given().contentType(MediaType.APPLICATION_JSON).body(newUserRequestWrapper).when().post(USERS_RESOURCE_PATH).then().statusCode(HttpStatus.SC_CONFLICT).body("errors.body", hasItems("email already exists"));
    }

    @Test
    public void givenAPersistedUser_whenCallingRegisterUserEndpointWithExistingUsername_thenReturnCode409() {

        String userPassword = "123";

        final var user = createUserEntity("user1", "user1@mail.com", "bio", "image", userPassword);

        NewUserRequest newUserRequest = new NewUserRequest();
        newUserRequest.setUsername(user.getUsername());
        newUserRequest.setEmail("user2@mail.com");
        newUserRequest.setPassword("user123");

        NewUserRequestWrapper newUserRequestWrapper = new NewUserRequestWrapper(newUserRequest);


        given().contentType(MediaType.APPLICATION_JSON).body(newUserRequestWrapper).when().post(USERS_RESOURCE_PATH).then().statusCode(HttpStatus.SC_CONFLICT).body("errors.body", hasItems("username already exists"));
    }

    @Test
    public void givenAnInvalidUser_thenReturnErrorsWith422Code() {

        NewUserRequest newUserRequest = new NewUserRequest();
        NewUserRequestWrapper newUserRequestWrapper = new NewUserRequestWrapper(newUserRequest);

        given().contentType(MediaType.APPLICATION_JSON).body(newUserRequestWrapper).when().post(USERS_RESOURCE_PATH).then().statusCode(HttpStatus.SC_UNPROCESSABLE_ENTITY).body("errors.body", hasSize(3), "errors.body", hasItems("username must be not blank", "email must be not blank", "password must be not blank"));
    }

    @Test
//    @DefaultLocale(language = "en", country = "EN")
    public void givenAnInvalidEmail_thenReturnErrorsWith422Code() {

        NewUserRequest newUserRequest = new NewUserRequest();
        newUserRequest.setUsername("username");
        newUserRequest.setEmail("email");
        newUserRequest.setPassword("user123");

        NewUserRequestWrapper newUserRequestWrapper = new NewUserRequestWrapper(newUserRequest);

        given().contentType(MediaType.APPLICATION_JSON).body(newUserRequestWrapper).when().post(USERS_RESOURCE_PATH).then().statusCode(HttpStatus.SC_UNPROCESSABLE_ENTITY).body("errors.body", hasSize(1), "errors.body", hasItems("must be a well-formed email address"));
    }

    @Test
    public void givenAInvalidLogin_whenExecuteLoginEndpoint_shouldReturnErrorsWith422Code() {

        LoginRequest loginRequest = new LoginRequest();
        LoginRequestWrapper loginRequestWrapper = new LoginRequestWrapper(loginRequest);

        given().contentType(MediaType.APPLICATION_JSON).body(loginRequestWrapper).when().post(LOGIN_PATH).then().statusCode(HttpStatus.SC_UNPROCESSABLE_ENTITY).body("errors.body", hasItems("email must be not blank", "password must be not blank"));
    }

    @Test
    public void givenAInvalidLoginEmail_whenExecuteLoginEndpoint_shouldReturnUnauthorized() {

        String userPassword = "123";

        final var user = createUserEntity("user1", "user1@mail.com", "bio", "image", userPassword);

        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("user2@mail.com");
        loginRequest.setPassword(userPassword);

        LoginRequestWrapper loginRequestWrapper = new LoginRequestWrapper(loginRequest);

        given().contentType(MediaType.APPLICATION_JSON).body(loginRequestWrapper).when().post(LOGIN_PATH).then().statusCode(HttpStatus.SC_UNAUTHORIZED).body("errors.body", hasItems("Unauthorized"));
    }

    @Test
    public void givenAInvalidLoginPassword_whenExecuteLoginEndpoint_shouldReturnUnauthorized() {

        final var user = createUserEntity("user1", "user1@mail.com", "123", "bio", "image");

        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail(user.getEmail());
        loginRequest.setPassword("145");

        LoginRequestWrapper loginRequestWrapper = new LoginRequestWrapper(loginRequest);

        given().contentType(MediaType.APPLICATION_JSON).body(loginRequestWrapper).when().post(LOGIN_PATH).then().statusCode(HttpStatus.SC_UNAUTHORIZED).body("errors.body", hasItems("Unauthorized"));
    }
}
