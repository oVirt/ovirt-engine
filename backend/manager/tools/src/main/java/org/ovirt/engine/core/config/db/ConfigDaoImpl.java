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
import org.ovirt.engine.core.tools.common.db.StandaloneDataSource;

public class ConfigDaoImpl implements ConfigDAO {

    private String updateSql;
    private String selectSql;
    private String selectKeysForNameSql;
    private String configTable;
    private String nameColumn;
    private String valueColumn;
    private String versionColumn;
    private DataSource ds;

    public ConfigDaoImpl(Configuration appConfig) throws ClassNotFoundException, SQLException, ConfigurationException,
            ConnectException, XPathExpressionException {
        valueColumn = appConfig.getString("configColumnValue");
        configTable = appConfig.getString("configTable");
        nameColumn = appConfig.getString("configColumnName");
        versionColumn = appConfig.getString("configColumnVersion");
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
        ds = new StandaloneDataSource();
    }

    @Override
    public int updateKey(ConfigKey configKey) throws SQLException {
        Connection connection = null;
        PreparedStatement prepareStatement = null;
        int executeUpdate;
        try {
            connection = ds.getConnection();
            prepareStatement = connection.prepareStatement(updateSql);
            prepareStatement.setString(1, configKey.getValue());
            prepareStatement.setString(2, configKey.getKey());
            prepareStatement.setString(3, configKey.getVersion());
            executeUpdate = prepareStatement.executeUpdate();
        } finally {
            if (prepareStatement != null) {
                prepareStatement.close();
            }
            if (connection != null) {
                connection.close();
            }
        }
        return executeUpdate;
    }

    @Override
    public ConfigKey getKey(ConfigKey key) throws SQLException {
        Connection connection = null;
        PreparedStatement prepareStatement = null;
        ConfigKey ckReturn = null;
        ResultSet resultSet = null;
        try {
            connection = ds.getConnection();
            prepareStatement = connection.prepareStatement(selectSql);
            prepareStatement.setString(1, key.getKey());
            prepareStatement.setString(2, key.getVersion());
            resultSet = prepareStatement.executeQuery();
            resultSet.next();
            try {
                String value = resultSet.getString(valueColumn);
                ckReturn = ConfigKeyFactory.getInstance().copyOf(key, value, key.getVersion());
            } catch (Exception e) {
                throw new SQLException("Failed to fetch value of " + key.getKey() + " with version " + key.getVersion() + " from DB. " + e.getMessage());
            }
        } finally {
            if (resultSet != null) {
                resultSet.close();
            }
            if (prepareStatement != null) {
                prepareStatement.close();
            }
            if (connection != null) {
                connection.close();
            }
        }
        return ckReturn;
    }

    @Override
    public List<ConfigKey> getKeysForName(String name) throws SQLException {
        Connection connection = null;
        PreparedStatement prepareStatement = null;
        ResultSet resultSet = null;
        List<ConfigKey> keys = new ArrayList<ConfigKey>();
        try {
            connection = ds.getConnection();
            prepareStatement = connection.prepareStatement(selectKeysForNameSql);
            prepareStatement.setString(1, name);
            resultSet = prepareStatement.executeQuery();
            while (resultSet.next()) {
                try {
                    keys.add(ConfigKeyFactory.getInstance().fromResultSet(resultSet));
                } catch (Exception e) {
                    throw new SQLException("Failed to fetch value of " + name + " from DB. " +
                            e.getMessage());
                }
            }
        } finally {
            if (resultSet != null) {
                resultSet.close();
            }
            if (prepareStatement != null) {
                prepareStatement.close();
            }
            if (connection != null) {
                connection.close();
            }
        }

        return keys;
    }
}
