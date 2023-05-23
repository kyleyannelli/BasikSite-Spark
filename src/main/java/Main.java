import controllers.PresetController;
import controllers.UserController;
import helpers.JwtMall;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import repositories.HibernateAuthTokenRepository;
import repositories.HibernatePresetRepository;
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
        HibernatePresetRepository hibernatePresetRepository = new HibernatePresetRepository(sessionFactory);

        JwtMall.setHibernateUserRepository(hibernateUserRepository);
        JwtMall.setHibernateAuthTokenRepository(hibernateAuthTokenRepository);
        new Api(new UserController(hibernateUserRepository, hibernateAuthTokenRepository),
                new PresetController(hibernatePresetRepository),
                hibernateUserRepository,
                hibernateAuthTokenRepository);
    }
}
