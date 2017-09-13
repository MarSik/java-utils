package com.github.marsik.utils.db;

import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.flywaydb.core.Flyway;

public class BaseDbConnection<D extends DataSource> {
    protected final D dataSource;

    public BaseDbConnection(D source) {
        dataSource = source;
    }

    public D getDataSource() {
        return this.dataSource;
    }

    public <T> T inTransaction(TransactionContext<T> consumer) throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            return consumer.withConnection(connection);
        }
    }

    public void updateDatabase() {
        Flyway flyway = new Flyway();
        flyway.setDataSource(dataSource);
        flyway.migrate();
    }

    public interface TransactionContext<T> {
        T withConnection(final Connection connection) throws SQLException;
    }
}
