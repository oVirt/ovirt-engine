package org.ovirt.engine.core.dal.dbbroker;

import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.logging.Logger;

import javax.sql.DataSource;

public class PGHack implements DataSource {

    DataSource real;

    public PGHack(DataSource d) {
        this.real = d;
    }

    @Override
    public Connection getConnection() throws SQLException {
        Connection conn = real.getConnection();
        conn.setAutoCommit(false);
        return conn;
    }

    @Override
    public Connection getConnection(String username, String password) throws SQLException {
        Connection conn = real.getConnection(username, password);
        conn.setAutoCommit(false);
        return conn;
    }

    @Override
    public PrintWriter getLogWriter() throws SQLException {
        return real.getLogWriter();
    }

    @Override
    public int getLoginTimeout() throws SQLException {
        return real.getLoginTimeout();
    }

    @Override
    public void setLogWriter(PrintWriter out) throws SQLException {
        real.setLogWriter(out);

    }

    @Override
    public void setLoginTimeout(int seconds) throws SQLException {
        real.setLoginTimeout(seconds);

    }

    @Override
    public boolean isWrapperFor(Class<?> iface) throws SQLException {
        return real.isWrapperFor(iface);
    }

    @Override
    public <T> T unwrap(Class<T> iface) throws SQLException {
        return real.unwrap(iface);
    }

    // In Java 7 the override annotation should be uncommented.
    // @Override
    public Logger getParentLogger() throws SQLFeatureNotSupportedException {
        return real.getParentLogger();
    }

}
