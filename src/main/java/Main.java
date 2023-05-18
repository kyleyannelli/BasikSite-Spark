import controllers.UserController;
import routes.Api;

public class Main {
    public static void main(String[] args) {
        new Api(new UserController());
    }
}
