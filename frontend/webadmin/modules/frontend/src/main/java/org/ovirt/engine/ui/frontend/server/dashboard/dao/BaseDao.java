package org.ovirt.engine.ui.frontend.server.dashboard.dao;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;

import javax.sql.DataSource;

import org.ovirt.engine.ui.frontend.server.dashboard.DashboardDataException;

public abstract class BaseDao {

    interface QueryResultCallback {

        /**
         * Called for each row of query's result set.
         */
        void onResult(ResultSet rs) throws SQLException;

    }

    private final DataSource dataSource;
    private final Properties sqlQueries;

    public <T extends BaseDao> BaseDao(DataSource dataSource, String propertiesFile, Class<T> daoClass)
            throws DashboardDataException {
        this.dataSource = dataSource;
        this.sqlQueries = new Properties();

        try (InputStream is = daoClass.getResourceAsStream(propertiesFile)) {
            sqlQueries.load(is);
        } catch (IOException e) {
            throw new DashboardDataException("Unable to load SQL queries", e); //$NON-NLS-1$
        }
    }

    /**
     * Runs the SQL query associated with the given {@code key}.
     *
     * @param key Key that identifies the query within the properties file.
     * @param callback Callback for processing query results.
     * @throws DashboardDataException Bad query {@code key} or SQL connection error.
     */
    protected void runQuery(String key, QueryResultCallback callback) throws DashboardDataException {
        if (!sqlQueries.containsKey(key)) {
            throw new DashboardDataException("SQL query not found: " + key); //$NON-NLS-1$
        }

        try (Connection con = dataSource.getConnection();
             PreparedStatement ps = con.prepareStatement(sqlQueries.getProperty(key));
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                callback.onResult(rs);
            }
        } catch (SQLException e) {
            throw new DashboardDataException("Error while running SQL query", e); //$NON-NLS-1$
        }
    }

}
