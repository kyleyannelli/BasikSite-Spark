package filters;

import helpers.JwtMall;
import models.AuthToken;
import org.tinylog.Logger;
import repositories.HibernateAuthTokenRepository;
import repositories.HibernateUserRepository;
import spark.Filter;
import spark.Request;
import spark.Response;

import java.util.Optional;

import static spark.Spark.halt;

public class RefreshOrJwtFilter implements Filter {
    private final HibernateUserRepository hibernateUserRepository;
    private final HibernateAuthTokenRepository hibernateAuthTokenRepository;
    public RefreshOrJwtFilter(HibernateUserRepository hibernateUserRepository, HibernateAuthTokenRepository hibernateAuthTokenRepository) {
        this.hibernateUserRepository = hibernateUserRepository;
        this.hibernateAuthTokenRepository = hibernateAuthTokenRepository;
    }
    @Override
    public void handle(Request request, Response response) {
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
            createJwtWithAuthToken(refreshToken, request, response);
        }
        // if there is a refresh token and JWT
        else if(refreshToken != null) {
            // check if the JWT is invalid
            if(JwtMall.isInvalidJwt(jwt)) {
                createJwtWithAuthToken(refreshToken, request, response);
            }
            // else continue...
        }
        // there is just a JWT, the user will likely have to login soon after...
        else {
            // check if the JWT is invalid
            // if it is not valid halt(401)
            if(JwtMall.isInvalidJwt(jwt)) halt(401);
        }
    }

    private void createJwtWithAuthToken(String refreshToken, Request request, Response response) {
        // make sure the refresh token is valid
        Optional<AuthToken> authToken = hibernateAuthTokenRepository.findByValue(refreshToken);
        if((authToken.isPresent() && !authToken.get().isActive()) || authToken.isEmpty()) {
            halt(401);
        }
        // check the user agent has not changed
        if(!authToken.get().isSameUserAgent(request.userAgent())) {
            // it has changed, so it is somewhat likely that the auth token has been stolen.
            hibernateAuthTokenRepository.setIsActive(authToken.get(), false);
            halt(401);
        }
        // else create a jwt for the user and store as http only cookie in response
        response.cookie("/",
                "JWT", JwtMall.createJwtFromUser(authToken.get().getUser()),
                86400 * 90, false, true);
    }
}
