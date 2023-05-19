package filters;

import models.User;
import org.tinylog.Logger;
import spark.Filter;
import spark.Request;
import spark.Response;

import static spark.Spark.halt;

public class RefreshOrJwtFilter implements Filter {
    @Override
    public void handle(Request request, Response response) throws Exception {
        String refreshToken = request.cookies().containsKey("RefreshToken") ?
                request.cookies().get("RefreshToken") : request.headers("Authorization");
        String jwt = request.cookies().containsKey("JWT") ?
                request.cookies().get("JWT") : request.headers("Authorization");

        // if no attempt of providing auth tokens
        if(refreshToken == null && jwt == null) {
            Logger.warn(request.ip() + " attempted to access " + request.pathInfo() + " without any tokens.");
            halt(401, "Unauthorized.");
        }
        //  if there's a refresh token but no JWT
        else if(refreshToken != null && jwt == null) {
            // make sure the refresh token is valid
            // if it is not valid halt(401)
            // else create a jwt for the user and store as http only cookie in response
        }
        // if there is a refresh token and JWT
        else if(refreshToken != null) {
            // check if the JWT is invalid
            //  if the JWT is not valid check if the refresh token is invalid
            //  if the refresh token is not valid halt(401)
            //  else create new JWT
            // else continue...
        }
        // there is just a JWT, the user will likely have to login soon after...
        else {
            // check if the JWT is invalid
            // if it is not valid halt(401)
        }
    }

    private User validateJwt(String jwt) {

    }
}
