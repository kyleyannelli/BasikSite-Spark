package routes;

import controllers.PresetController;
import controllers.UserController;
import filters.RefreshOrJwtFilter;
import repositories.HibernateAuthTokenRepository;
import repositories.HibernateUserRepository;

import static spark.Spark.*;

public class Api {
    public Api(UserController userController, PresetController presetController, HibernateUserRepository hibernateUserRepository, HibernateAuthTokenRepository hibernateAuthTokenRepository) {
        setupRoutes(userController, presetController, hibernateUserRepository, hibernateAuthTokenRepository);
    }
    public void setupRoutes(UserController userController, PresetController presetController, HibernateUserRepository hibernateUserRepository, HibernateAuthTokenRepository hibernateAuthTokenRepository) {
        RefreshOrJwtFilter refreshOrJwtFilter = new RefreshOrJwtFilter(hibernateUserRepository, hibernateAuthTokenRepository);
        path("/api", () -> {
            post("/register", userController::register);
            post("/login", userController::login);

            before("/users/*", refreshOrJwtFilter);
            path("/users", () -> {
                get("/me", userController::me);
                delete("/me/authentication", userController::invalidateAuthentication);
                get("/:usernameOrId", userController::getUserByIdOrUsername);
            });

            before("/presets/*", refreshOrJwtFilter);
            path("/presets", () -> {
                post("/", presetController::create);
                get("/:presetId", presetController::get);
            });
        });
    }
}
