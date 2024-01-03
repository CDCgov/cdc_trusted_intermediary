package gov.hhs.cdc.trustedintermediary.external.database;

import gov.hhs.cdc.trustedintermediary.context.ApplicationContext;
import gov.hhs.cdc.trustedintermediary.etor.metadata.PartnerMetadata;
import gov.hhs.cdc.trustedintermediary.external.azure.AzureClient;
import gov.hhs.cdc.trustedintermediary.wrappers.DbDao;
import gov.hhs.cdc.trustedintermediary.wrappers.Logger;
import gov.hhs.cdc.trustedintermediary.wrappers.SqlDriverManager;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Properties;
import javax.inject.Inject;

/** Class for accessing and managing data for the postgres Database */
public class PostgresDao implements DbDao {

    private static final PostgresDao INSTANCE = new PostgresDao();

    @Inject Logger logger;
    @Inject SqlDriverManager driverManager;
    @Inject AzureClient azureClient;

    private PostgresDao() {}

    protected Connection connect() throws SQLException {
        Connection conn;
        String url =
                "jdbc:postgresql://"
                        + ApplicationContext.getProperty("DB_URL")
                        + ":"
                        + ApplicationContext.getProperty("DB_PORT")
                        + "/"
                        + ApplicationContext.getProperty("DB_NAME");

        logger.logInfo("going to connect to db url {}", url);

        // Ternaries prevent NullPointerException during testing since we decided not to mock env
        // vars.
        String user =
                ApplicationContext.getProperty("DB_USER") == null
                        ? ""
                        : ApplicationContext.getProperty("DB_USER");
        String pass =
                ApplicationContext.getProperty("DB_PASS") == null
                        ? ""
                        : ApplicationContext.getProperty("DB_PASS");
        String ssl =
                ApplicationContext.getProperty("DB_SSL") == null
                        ? ""
                        : ApplicationContext.getProperty("DB_SSL");

        Properties props = new Properties();
        props.setProperty("user", user);
        logger.logInfo("About to get the db password");

        String token =
                pass.isBlank()
                        ? azureClient.getScopedToken(
                                "https://ossrdbms-aad.database.windows.net/.default")
                        : pass;

        logger.logInfo("got the db password");

        props.setProperty("password", token);

        // If the below prop isn't set to require and we just set ssl=true it will expect a CA cert
        // in azure which breaks it
        props.setProperty("sslmode", ssl);
        conn = driverManager.getConnection(url, props);
        logger.logInfo("DB Connected Successfully");
        return conn;
    }

    public static PostgresDao getInstance() {
        return INSTANCE;
    }

    @Override
    public synchronized void upsertMetadata(
            String receivedSubmissionId,
            String sender,
            String receiver,
            String hash,
            Instant timeReceived)
            throws SQLException {

        try (Connection conn = connect();
                PreparedStatement statement =
                        conn.prepareStatement("INSERT INTO metadata VALUES (?, ?, ?, ?, ?)")) {
            // TODO: Update the below statement to handle on conflict, after we figure out what that
            // behavior should be
            statement.setString(1, receivedSubmissionId);
            statement.setString(2, sender);
            statement.setString(3, receiver);
            statement.setString(4, hash);
            statement.setTimestamp(5, Timestamp.from(timeReceived));

            statement.executeUpdate();
        }
    }

    @Override
    public synchronized PartnerMetadata fetchMetadata(String receivedSubmissionId)
            throws SQLException {
        try (Connection conn = connect();
                PreparedStatement statement =
                        conn.prepareStatement("SELECT * FROM metadata where message_id = ?")) {

            statement.setString(1, receivedSubmissionId);

            ResultSet result = statement.executeQuery();

            var hasValidData = result.next();
            if (!hasValidData) {
                return null;
            }

            return new PartnerMetadata(
                    result.getString("message_id"),
                    result.getString("receiver"),
                    result.getTimestamp("time_received").toInstant(),
                    result.getString("hash_of_order"));
        }
    }
}
