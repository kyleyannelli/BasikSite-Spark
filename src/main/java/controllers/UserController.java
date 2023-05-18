package controllers;

import javax.servlet.http.HttpServletResponse;
import java.math.BigInteger;
import java.security.SecureRandom;

import static spark.Spark.halt;

public class UserController {
    public String register(spark.Request req, spark.Response res) {
        res.status(500);

        return "Registered.";
    }

    public String login(spark.Request req, spark.Response res) {
        // check if the user has the correct password, or a valid RefreshToken cookie
        if(req.cookies().containsKey("RefreshToken")) {
            halt(HttpServletResponse.SC_ACCEPTED);
            return "Already logged in.";
        }
        // halt() if neither
        // else, generate a refresh token for supplying a JWT
        // return refresh token for HTTP-Only
        SecureRandom random = new SecureRandom();
        String authTokenValue = new BigInteger(512, random).toString(32);
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
