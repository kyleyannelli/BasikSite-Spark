import controllers.UserController;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import repositories.HibernateAuthTokenRepository;
import repositories.HibernateUserRepository;
import routes.Api;
import spark.Spark;

public class Main {
    public static void main(String[] args) {
        // adjust this for your server
        Spark.threadPool(50);

        SessionFactory sessionFactory = new Configuration().configure().buildSessionFactory();
        HibernateUserRepository hibernateUserRepository = new HibernateUserRepository(sessionFactory);
        HibernateAuthTokenRepository hibernateAuthTokenRepository = new HibernateAuthTokenRepository(sessionFactory);

        new Api(new UserController(hibernateUserRepository, hibernateAuthTokenRepository),
                hibernateUserRepository,
                hibernateAuthTokenRepository);
    }
}
