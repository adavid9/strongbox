package org.carlspring.strongbox.controllers.login;

import org.carlspring.strongbox.config.IntegrationTest;
import org.carlspring.strongbox.rest.common.RestAssuredBaseTest;
import org.carlspring.strongbox.users.dto.UserDto;
import org.carlspring.strongbox.users.service.UserService;
import org.carlspring.strongbox.users.service.impl.XmlUserService.XmlUserServiceQualifier;

import javax.inject.Inject;

import io.restassured.module.mockmvc.RestAssuredMockMvc;
import org.hamcrest.CoreMatchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.parallel.Execution;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.parallel.ExecutionMode.CONCURRENT;

/**
 * @author Przemyslaw Fusik
 */
@IntegrationTest
@ExtendWith(SpringExtension.class)
@Execution(CONCURRENT)
public class LoginControllerTest
        extends RestAssuredBaseTest
{

    @Inject
    @XmlUserServiceQualifier
    private UserService userService;

    @Override
    @BeforeEach
    public void init()
            throws Exception
    {
        super.init();
    }

    @Test
    public void shouldReturnGeneratedToken()
    {
        LoginInput loginInput = new LoginInput();
        loginInput.setUsername("admin");
        loginInput.setPassword("password");

        RestAssuredMockMvc.given()
                          .contentType("application/json")
                          .header("Accept", "application/json")
                          .body(loginInput)
                          .when()
                          .post("/api/login")
                          .peek()
                          .then()
                          .body("token", CoreMatchers.any(String.class))
                          .body("authorities", hasSize(greaterThan(0)))
                          .statusCode(200);
    }

    @WithAnonymousUser
    @Test
    public void shouldReturnInvalidCredentialsError()
    {
        LoginInput loginInput = new LoginInput();
        loginInput.setUsername("przemyslaw_fusik");
        loginInput.setPassword("password");

        RestAssuredMockMvc.given()
                          .contentType("application/json")
                          .header("Accept", "application/json")
                          .body(loginInput)
                          .when()
                          .post("/api/login")
                          .peek()
                          .then()
                          .body("error", CoreMatchers.equalTo("invalid.credentials"))
                          .statusCode(401);
    }

    @Test
    @WithAnonymousUser
    public void shouldReturnInvalidCredentialsWhenUserIsDisabled()
    {
        UserDto disabledUser = new UserDto();
        disabledUser.setUsername("test-disabled-user-login");
        disabledUser.setPassword("1234");
        disabledUser.setEnabled(false);
        userService.save(disabledUser);

        LoginInput loginInput = new LoginInput();
        loginInput.setUsername("test-disabled-user-login");
        loginInput.setPassword("1234");

        RestAssuredMockMvc.given()
                          .contentType("application/json")
                          .header("Accept", "application/json")
                          .body(loginInput)
                          .when()
                          .post("/api/login")
                          .peek()
                          .then()
                          .body("error", CoreMatchers.equalTo("User account is locked"))
                          .statusCode(401);
    }

}
