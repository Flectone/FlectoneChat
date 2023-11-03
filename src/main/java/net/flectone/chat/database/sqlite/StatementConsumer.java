package net.flectone.chat.database.sqlite;

import java.sql.Connection;
import java.sql.SQLException;

@FunctionalInterface
public interface StatementConsumer {
    void accept(Connection connection) throws SQLException;
}