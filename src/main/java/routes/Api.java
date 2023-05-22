package routes;

import controllers.UserController;
import filters.RefreshOrJwtFilter;
import repositories.HibernateAuthTokenRepository;
import repositories.HibernateUserRepository;

import static spark.Spark.*;

public class Api {
    public Api(UserController userController, HibernateUserRepository hibernateUserRepository, HibernateAuthTokenRepository hibernateAuthTokenRepository) {
        setupRoutes(userController, hibernateUserRepository, hibernateAuthTokenRepository);
    }
    public void setupRoutes(UserController userController, HibernateUserRepository hibernateUserRepository, HibernateAuthTokenRepository hibernateAuthTokenRepository) {
        path("/api", () -> {
            post("/register", userController::register);
            post("/login", userController::login);

            before("/users/*", new RefreshOrJwtFilter(hibernateUserRepository, hibernateAuthTokenRepository));
            path("/users", () -> {
                get("/me", userController::me);
                delete("/me/authentication", userController::invalidateAuthentication);
                get("/:usernameOrId", userController::getUserByIdOrUsername);
            });
        });
    }
}
