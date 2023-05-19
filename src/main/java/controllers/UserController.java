package controllers;

import models.AuthToken;
import models.User;
import repositories.HibernateAuthTokenRepository;
import repositories.HibernateUserRepository;

import javax.servlet.http.HttpServletResponse;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

import static spark.Spark.halt;

public class UserController {
    private final HibernateUserRepository hibernateUserRepository;
    private final HibernateAuthTokenRepository hibernateAuthTokenRepository;
    public UserController(HibernateUserRepository hibernateUserRepository, HibernateAuthTokenRepository hibernateAuthTokenRepository) {
        this.hibernateUserRepository = hibernateUserRepository;
        this.hibernateAuthTokenRepository = hibernateAuthTokenRepository;
    }
    public String register(spark.Request req, spark.Response res) {
        String username = req.queryParams("username"), password = req.queryParams("password"), passwordConfirm = req.queryParams("passwordConfirm");
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
        User user = new User();
        user.setUsername(username);
        user.createPassword(password);
        int tagId, total = 0;
        BitSet foundTags = new BitSet(10000);
        if(hibernateUserRepository.getCountByUsername(username) >= 100) {
            halt(HttpServletResponse.SC_CONFLICT, "Please pick a different username");
        }
        do {
            tagId = ThreadLocalRandom.current().nextInt(0, 10000);
            total++;

            if(foundTags.get(tagId)) {
                continue;
            }
            if (!hibernateUserRepository.doesUsernameAndTagExist(username, String.format("%0" + 4 + "d", tagId))) {
                break;
            }
            foundTags.set(tagId);
        } while (total <= 100);
        if(total > 100) {
            halt(HttpServletResponse.SC_CONFLICT, "Please pick a different username");
        }
        user.setTagId(tagId);
        return hibernateUserRepository.save(user).getId() != null ? user.toString() : "There was an issue creating the user.";
    }

    public String login(spark.Request req, spark.Response res) {
        String username = req.queryParams("username");
        String tag = req.queryParams("tag");
        String password = req.queryParams("password");
        String cookieAuthToken = req.cookie("RefreshToken");
        System.out.println(cookieAuthToken);
        Optional<AuthToken> existingAuthToken;
        // check if the user has the correct password, or a valid RefreshToken cookie
        if(req.cookies().containsKey("RefreshToken") &&
                (existingAuthToken = hibernateAuthTokenRepository.findByValue(req.cookies().get("RefreshToken"))).isPresent() &&
                existingAuthToken.get().isActive()) {
            halt(HttpServletResponse.SC_OK, "Already logged in.");
        }
        User user = hibernateUserRepository.findByUsernameAndTag(username, tag);
        if(!user.isCorrectPassword(password)) {
            halt(401);
        }
        // else, generate a refresh token for supplying a JWT
        // return refresh token for HTTP-Only
        SecureRandom random = new SecureRandom();
        String authTokenValue = new BigInteger(512, random).toString(32);
        while(!hibernateAuthTokenRepository.findByValue(authTokenValue).isEmpty()) {
            authTokenValue = new BigInteger(512, random).toString(32);
        }
        AuthToken authToken = new AuthToken(authTokenValue, user);
        authToken.setExpiryInSeconds(new Date().toInstant().getEpochSecond() + (86400 * 90));
        authToken.setActive(true);
        hibernateAuthTokenRepository.save(authToken);
        res.cookie("/",
                "RefreshToken",
                authTokenValue,
                86400 * 90,
                false,
                true);
        res.status(HttpServletResponse.SC_CREATED);
        return "Logged in.";
    }
}
