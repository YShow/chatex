package database.setup;

import java.sql.SQLException;
import java.util.function.Supplier;

import database.HSQLDBConnection;
import server.TcpServer;

public final class DatabaseSetup {
    private static final String SQL_CREATE = """
    				CREATE cached TABLE client (
    	id INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    	name varchar(30) NOT NULL UNIQUE,
    	registerdate timestamp NOT NULL,
    	passhash varchar(512) NOT NULL,
    	salthash varchar(512) NOT null
    	)

    	CREATE cached table messagestore(
    	id INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    	idclient INTEGER NOT NULL,
    	FOREIGN KEY (idclient) REFERENCES client (id),
    	messagestring varchar(500) NOT NULL,
    	messagedate TIMESTAMP NOT null
    	)
    	""";

    private DatabaseSetup() {
    }

    public static final boolean isDatabaseCreated() {

	try (final var con = HSQLDBConnection.createConnection()) {
	    final var res = con.getMetaData().getTables(null, null, "CLIENT", null);
	    return res.next();
	} catch (SQLException e) {
	    TcpServer.getServerLogger().log(System.Logger.Level.INFO, e::getMessage, e);
	}

	return false;
    }

    public static final boolean createDatabase() {

	try (final var con = HSQLDBConnection.createConnection(); final var statement = con.createStatement();) {
	    return statement.execute(SQL_CREATE);
	} catch (SQLException e) {
	    TcpServer.getServerLogger().log(System.Logger.Level.INFO, e::getMessage, e);
	}

	return false;
    }

    public static final boolean closeDatabase() {

	try (final var con = HSQLDBConnection.createConnection(); final var statement = con.createStatement();) {
	    return statement.execute("SHUTDOWN;");
	} catch (SQLException e) {
	    TcpServer.getServerLogger().log(System.Logger.Level.INFO, e::getMessage, e);
	}

	return false;
    }

}
