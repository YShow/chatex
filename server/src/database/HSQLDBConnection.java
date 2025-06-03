package database;

import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Connection;

public final class HSQLDBConnection {
    private HSQLDBConnection() {
    }

    public static final Connection createConnection() throws SQLException {
	return DriverManager.getConnection("jdbc:hsqldb:file:testdb", "SA", "");
    }
}
