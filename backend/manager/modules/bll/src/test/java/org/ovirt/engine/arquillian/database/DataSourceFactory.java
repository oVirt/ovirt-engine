package org.ovirt.engine.arquillian.database;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import javax.enterprise.inject.Produces;
import javax.inject.Singleton;
import javax.sql.DataSource;

import org.ovirt.engine.core.dal.dbbroker.PostgresDbEngineDialect;
import org.ovirt.engine.core.dal.dbbroker.SimpleJdbcCallsHandler;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.SingleConnectionDataSource;
import org.springframework.transaction.PlatformTransactionManager;

@Singleton
public class DataSourceFactory {

    @Produces
    @Singleton
    private final PostgresDbEngineDialect engineDialect;

    @Produces
    @Singleton
    private final PlatformTransactionManager transactionManager;

    @Produces
    @Singleton
    private final JdbcTemplate jdbcTemplate;

    @Produces
    @Singleton
    private final DataSource dataSource;

    @Produces
    @Singleton
    private final SimpleJdbcCallsHandler jdbcCallsHandler;

    public DataSourceFactory() throws ClassNotFoundException, IOException {
        ClassLoader.getSystemClassLoader().loadClass("org.postgresql.Driver");
        Properties properties = new Properties();
        try (InputStream resource = getClass().getResourceAsStream("/test-database.properties")) {
            properties.load(resource);
        }
        dataSource = new SingleConnectionDataSource(
                System.getProperty("engine.db.url", properties.getProperty("engine.db.url")),
                System.getProperty("engine.db.username", properties.getProperty("engine.db.username")),
                System.getProperty("engine.db.password", properties.getProperty("engine.db.password")),
                true);

        engineDialect = new PostgresDbEngineDialect();
        transactionManager = new DataSourceTransactionManager(dataSource);
        jdbcTemplate = new JdbcTemplate(dataSource);
        jdbcCallsHandler = new SimpleJdbcCallsHandler(engineDialect, jdbcTemplate);
    }
}
