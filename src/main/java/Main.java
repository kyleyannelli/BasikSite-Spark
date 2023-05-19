import controllers.UserController;
import helpers.TinylogWriter;
import liquibase.Contexts;
import liquibase.Liquibase;
import liquibase.database.Database;
import liquibase.database.DatabaseConnection;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.LiquibaseException;
import liquibase.resource.ClassLoaderResourceAccessor;
import routes.Api;

import java.io.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class Main {
    public static void main(String[] args) {
        try {
            runLiquibase();
            new Api(new UserController());
        } catch (IOException |
                 LiquibaseException |
                 SQLException exception) {
            exception.printStackTrace();
        }
    }

    private static void runLiquibase() throws IOException, LiquibaseException, SQLException {
        Properties props = new Properties();
        InputStream input = Main.class.getClassLoader().getResourceAsStream("liquibase.properties");
        props.load(input);

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
        TinylogWriter tinylogWriter = new TinylogWriter();
        liquibase.update("", tinylogWriter);
    }
}
