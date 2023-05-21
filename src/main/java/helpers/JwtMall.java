package helpers;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import models.User;
import org.tinylog.Logger;
import repositories.HibernateUserRepository;
import spark.Request;

import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.Properties;

public class JwtMall {
    private static HibernateUserRepository hibernateUserRepository;
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
}
