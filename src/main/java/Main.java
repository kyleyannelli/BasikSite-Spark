import controllers.UserController;
import liquibase.Contexts;
import liquibase.Liquibase;
import liquibase.database.Database;
import liquibase.database.DatabaseConnection;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.resource.ClassLoaderResourceAccessor;
import routes.Api;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Properties;

public class Main {
    public static void main(String[] args) {
        try {
            // Load properties from the liquibase.properties file
            Properties props = new Properties();
            try (InputStream input = Main.class.getClassLoader().getResourceAsStream("liquibase.properties")) {
                props.load(input);
            }

            Connection connection = DriverManager.getConnection(
                    props.getProperty("url"),
                    props.getProperty("username"),
                    props.getProperty("password")
            );

            DatabaseConnection databaseConnection = new JdbcConnection(connection);

            Database database = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(databaseConnection);

            Liquibase liquibase = new liquibase.Liquibase(
                    props.getProperty("changeLogFile"),
                    new ClassLoaderResourceAccessor(),
                    database
            );

            liquibase.update(new Contexts());
            new Api(new UserController());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
