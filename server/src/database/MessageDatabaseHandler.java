package database;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

import handlers.ClientHandler;

public final class MessageDatabaseHandler {

    private static final System.Logger logger = System.getLogger("Database");

    private MessageDatabaseHandler() {
    }

    private static final String SQL_INSERT_MESSAGE = "INSERT INTO MESSAGESTORE"
	    + " (IDCLIENT, MESSAGESTRING, MESSAGEDATE) VALUES(?, ?, ?);";

    public static final boolean saveMessageToDatabase(final String message, final ClientHandler sender) {
	try (final var con = HSQLDBConnection.createConnection();
		final var preparedStatement = con.prepareStatement(SQL_INSERT_MESSAGE);) {

	    logger.log(System.Logger.Level.INFO, "Saving to db");

	    // TODO: change to string later
	    preparedStatement.setInt(1, Integer.parseInt(sender.getUserName()));
	    preparedStatement.setString(2, message);
	    preparedStatement.setObject(3, LocalDateTime.now(ZoneOffset.UTC));

	    final int numberOfUpdates = preparedStatement.executeUpdate();

	    return numberOfUpdates > 0;

	} catch (final SQLException e) {
	    logger.log(System.Logger.Level.INFO, e::getMessage, e);
	}
	return false;
    }

}
