package org.ovirt.engine.core.config.db;

import java.net.ConnectException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;
import javax.xml.xpath.XPathExpressionException;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.ovirt.engine.core.config.entity.ConfigKey;
import org.ovirt.engine.core.config.entity.ConfigKeyFactory;
import org.ovirt.engine.core.utils.db.StandaloneDataSource;

public class ConfigDaoImpl implements ConfigDao {

    private String updateSql;
    private String selectSql;
    private String selectKeysForNameSql;
    private String selectConfigDiff;
    private String configTable;
    private String nameColumn;
    private String valueColumn;
    private String versionColumn;
    private String defaultColumn;
    private DataSource ds;

    public ConfigDaoImpl(Configuration appConfig) throws ClassNotFoundException, SQLException, ConfigurationException,
            ConnectException, XPathExpressionException {
        valueColumn = appConfig.getString("configColumnValue");
        configTable = appConfig.getString("configTable");
        nameColumn = appConfig.getString("configColumnName");
        versionColumn = appConfig.getString("configColumnVersion");
        defaultColumn = appConfig.getString("configColumnDefault");
        selectSql =
                MessageFormat.format("select {0} from {1} where {2}=? and {3} =?",
                                 valueColumn,
                                 configTable,
                                 nameColumn,
                                 versionColumn);
        updateSql =
                MessageFormat.format("update {0} set {1}=? where {2}=? and {3}=?",
                                 configTable,
                                 valueColumn,
                                 nameColumn,
                                 versionColumn);
        selectKeysForNameSql =
                MessageFormat.format("select * from {0} where {1}=? ",
                        configTable,
                        nameColumn);
        selectConfigDiff =
                MessageFormat.format("SELECT {0}, {1}, {2}, {3} FROM {4} WHERE {5} != {6}",
                        nameColumn,
                        valueColumn,
                        defaultColumn,
                        versionColumn,
                        configTable,
                        valueColumn,
                        defaultColumn);
        ds = new StandaloneDataSource();
    }

    @Override
    public int updateKey(ConfigKey configKey) throws SQLException {
        int executeUpdate;
        try (
                Connection connection = ds.getConnection();
                PreparedStatement prepareStatement = connection.prepareStatement(updateSql);
        ) {
            prepareStatement.setString(1, configKey.getValue());
            prepareStatement.setString(2, configKey.getKey());
            prepareStatement.setString(3, configKey.getVersion());
            executeUpdate = prepareStatement.executeUpdate();
        }
        return executeUpdate;
    }

    @Override
    public ConfigKey getKey(ConfigKey key) throws SQLException {
        ConfigKey ckReturn;
        try (
                Connection connection = ds.getConnection();
                PreparedStatement prepareStatement = connection.prepareStatement(selectSql);
        ) {
            prepareStatement.setString(1, key.getKey());
            prepareStatement.setString(2, key.getVersion());
            try (ResultSet resultSet = prepareStatement.executeQuery()) {
                resultSet.next();
                try {
                    String value = resultSet.getString(valueColumn);
                    ckReturn = ConfigKeyFactory.getInstance().copyOf(key, value, key.getVersion());
                } catch (Exception e) {
                    throw new SQLException("Failed to fetch value of " + key.getKey() + " with version " + key.getVersion() + " from DB. " + e.getMessage());
                }
            }
        }
        return ckReturn;
    }

    @Override
    public List<ConfigKey> getKeysForName(String name) throws SQLException {
        List<ConfigKey> keys = new ArrayList<>();
        try (
                Connection connection = ds.getConnection();
                PreparedStatement prepareStatement = connection.prepareStatement(selectKeysForNameSql);
        ) {
            prepareStatement.setString(1, name);
            try (ResultSet resultSet = prepareStatement.executeQuery()) {
                while (resultSet.next()) {
                    try {
                        keys.add(ConfigKeyFactory.getInstance().fromResultSet(resultSet));
                    } catch (Exception e) {
                        throw new SQLException("Failed to fetch value of " + name + " from DB. " +
                                e.getMessage());
                    }
                }
            }
        }

        return keys;
    }

    @Override public List<ConfigKey> getConfigDiff() throws SQLException {
        List<ConfigKey> configDiffs = new ArrayList<>();
        try (
                Connection connection = ds.getConnection();
                PreparedStatement prepareStatement = connection.prepareStatement(selectConfigDiff)
        ) {
            try (ResultSet resultSet = prepareStatement.executeQuery()) {
                while (resultSet.next()) {
                    configDiffs.add(ConfigKeyFactory.getInstance().fromResultSet(resultSet));
                }
            }
        }

        return configDiffs;
    }
}
