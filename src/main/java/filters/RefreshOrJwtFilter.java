package filters;

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
        System.out.println(jwt + refreshToken);
        halt(401);
    }
}
