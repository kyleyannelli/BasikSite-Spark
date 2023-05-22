package helpers;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import models.AuthToken;
import models.User;
import org.tinylog.Logger;
import repositories.HibernateAuthTokenRepository;
import repositories.HibernateUserRepository;
import spark.Request;
import spark.Response;

import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.Optional;
import java.util.Properties;

import static spark.Spark.halt;

public class JwtMall {
    private static HibernateUserRepository hibernateUserRepository;
    private static HibernateAuthTokenRepository hibernateAuthTokenRepository;
    public static String createJwtFromUser(User user) {
        return Jwts.builder()
                .claim("role", "USER")
                .setSubject(user.username())
                .claim("tag", user.getTagId())
                .setIssuedAt(new Date())
                .setExpiration(new Date(new Date().getTime() + (60000 * 5)))
                .signWith(SignatureAlgorithm.HS512, getValueFromProperties("jwt-secret"))
                .compact();
    }

    public static Claims getClaimsFromJwt(String jwt) {
        return Jwts.parser()
                .setSigningKey(getValueFromProperties("jwt-secret"))
                .parseClaimsJws(jwt)
                .getBody();
    }

    public static boolean isInvalidJwt(String jwt) {
        try {
            String starterKey = "JWT ";
            if(jwt.startsWith(starterKey)) jwt = jwt.substring(starterKey.length());
            Jwts.parser()
                    .setSigningKey(getValueFromProperties("jwt-secret"))
                    .parseClaimsJws(jwt)
                    .getBody();
            return false;
        } catch (Exception e) {
            return true;
        }
    }

    public static User getUserFromJwt(String jwt) {
        return hibernateUserRepository.findByJwt(jwt);
    }

    public static User getUserFromJwt(Request request) {
        return hibernateUserRepository.findByJwt(request.cookies().containsKey("JWT") ?
                request.cookies().get("JWT") : request.headers("Authorization"));
    }

    public static void setHibernateUserRepository(HibernateUserRepository userRepository) {
        hibernateUserRepository = userRepository;
    }

    public static Optional<AuthToken> createJwtWithAuthToken(String refreshToken, Request request, Response response) {
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
        return authToken;
    }

    private static String getValueFromProperties(String key) {
        try {
            Properties props = new Properties();
            InputStream input = JwtMall.class.getClassLoader().getResourceAsStream("basik.properties");
            props.load(input);
            return props.get(key).toString();
        } catch (IOException exception) {
            Logger.error("Error while loading value from JwtMall property " + exception.getMessage());
            return "";
        }
    }

    public static void setHibernateAuthTokenRepository(HibernateAuthTokenRepository authTokenRepository) {
        hibernateAuthTokenRepository = authTokenRepository;
    }
}
