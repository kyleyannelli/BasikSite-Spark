package routes;

import controllers.UserController;
import filters.RefreshOrJwtFilter;

import static spark.Spark.*;

public class Api {
    private UserController userController;
    public Api(UserController userController) {
        this.userController = userController;
        setupRoutes();
    }
    public void setupRoutes() {
        path("/api", () -> {
            post("/register", userController::register);
            post("/login", userController::login);

            before("/users/*", new RefreshOrJwtFilter());
            path("/users", () -> {
                get("/me", (req, res) -> {
                    return "Hello this is a test";
                });
            });
        });
    }
}
