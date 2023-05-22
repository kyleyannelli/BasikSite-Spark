package controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import helpers.JwtMall;
import models.AuthToken;
import models.User;
import org.tinylog.Logger;
import repositories.HibernateAuthTokenRepository;
import repositories.HibernateUserRepository;
import spark.Request;
import spark.Response;

import javax.servlet.http.HttpServletResponse;
import java.util.*;

import static spark.Spark.halt;

public class UserController {
    private final HibernateUserRepository hibernateUserRepository;
    private final HibernateAuthTokenRepository hibernateAuthTokenRepository;
    public UserController(HibernateUserRepository hibernateUserRepository, HibernateAuthTokenRepository hibernateAuthTokenRepository) {
        this.hibernateUserRepository = hibernateUserRepository;
        this.hibernateAuthTokenRepository = hibernateAuthTokenRepository;
    }
    public String register(spark.Request req, spark.Response res) {
        String username = req.queryParams("username");
        String password = req.queryParams("password");
        String passwordConfirm = req.queryParams("passwordConfirm");
        verifyRegistryParams(username, password, passwordConfirm);

        User user = new User();
        user.setUsername(username);
        user.createPassword(password);
        user.setTagId(User.generateTag(hibernateUserRepository, username));

        if(hibernateUserRepository.save(user).getId() != null) {
            res.status(HttpServletResponse.SC_CREATED);
            return user.toString();
        } else {
            res.status(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            return "There was an issue creating the user.";
        }
    }

    private void verifyRegistryParams(String username, String password, String passwordConfirm) {
        if(username == null
                || password == null
                || passwordConfirm == null) {
            halt(HttpServletResponse.SC_BAD_REQUEST, "Ensure username, password, & passwordConfirm are included in your request...");
        } else if(username.length() < 4) {
            halt(HttpServletResponse.SC_BAD_REQUEST, "Ensure username is at least 4 characters");
        } else if(password.length() <= 12 || password.length() > 50) {
            halt(HttpServletResponse.SC_BAD_REQUEST, "Ensure password length is between 12 and 50 characters.");
        } else if(!(password.contains("*") ||
                password.contains("@") ||
                password.contains("!") ||
                password.contains("&") ||
                password.contains("#") ||
                password.contains("$"))) {
            halt(HttpServletResponse.SC_BAD_REQUEST, "Ensure password contains at least one of the following characters. !,@,&,#, or $");
        } else if(!password.equals(passwordConfirm)) {
            halt(HttpServletResponse.SC_BAD_REQUEST, "Ensure password, & passwordConfirm match");
        }
    }

    public String login(spark.Request req, spark.Response res) {
        String username = req.queryParams("username");
        String tag = req.queryParams("tag");
        String password = req.queryParams("password");
        String cookieAuthToken = req.cookie("RefreshToken");

        if (isValidRefreshToken(cookieAuthToken)) {
            halt(HttpServletResponse.SC_OK, "Already logged in.");
        }

        User user = User.validateUser(hibernateUserRepository, username, tag, password);

        AuthToken authToken = new AuthToken(user);
        authToken.generate(hibernateAuthTokenRepository);
        authToken.setUserAgent(req.userAgent());
        hibernateAuthTokenRepository.save(authToken);

        res.cookie("/",
                "RefreshToken",
                authToken.getValue(),
                86400 * 90,
                false,
                true);
        res.status(HttpServletResponse.SC_CREATED);

        return "Logged in.";
    }

    private boolean isValidRefreshToken(String cookieAuthToken) {
        if (cookieAuthToken == null) {
            return false;
        }
        Optional<AuthToken> existingAuthToken = hibernateAuthTokenRepository.findByValue(cookieAuthToken);
        return existingAuthToken.isPresent() && existingAuthToken.get().isActive();
    }

    public String me(Request request, Response response) throws JsonProcessingException {
        response.type("application/json");
        return JwtMall.getUserFromJwt(request).toJson();
    }

    public String getUserByIdOrUsername(Request request, Response response) {
        String usernameOrId = request.params(":usernameOrId");
        try {
            response.status(HttpServletResponse.SC_FOUND);
            if(usernameOrId.chars().allMatch(Character::isDigit)) {
                return hibernateUserRepository.findById(Long.parseLong(usernameOrId)).toJson();
            } else {
                String[] usernameAndTag = usernameOrId.split("@");
                return hibernateUserRepository.findByUsernameAndTag(usernameAndTag[0], usernameAndTag[1]).toJson();
            }
        } catch (Exception e) {
            Logger.warn(e.getMessage() + " occurred while retrieving user " + usernameOrId + " via path " + request.pathInfo());
            response.status(HttpServletResponse.SC_NOT_FOUND);
            return "Not found or invalid format. Make sure you are using an id OR username with the tag formatted as kyle@2319";
        }
    }

    public String invalidateAuthentication(Request request, Response response) {
        if(request.attribute("AuthenticationTokenObject") == null) halt(401);
        AuthToken authToken = request.attribute("AuthenticationTokenObject");
        hibernateAuthTokenRepository.setIsActive(authToken, false);
        response.status(HttpServletResponse.SC_OK);
        response.cookie("/",
                "RefreshToken",
                null,
                0,
                false,
                true);
        response.cookie("/",
                "JWT",
                null,
                0,
                false,
                true);
        return "Token has been invalidated!";
    }
}
