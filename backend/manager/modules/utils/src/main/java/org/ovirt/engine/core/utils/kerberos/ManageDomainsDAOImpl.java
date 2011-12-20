package org.ovirt.engine.core.utils.kerberos;

import java.net.ConnectException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import javax.xml.xpath.XPathExpressionException;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.log4j.Logger;
import org.ovirt.engine.core.tools.common.db.ConnectionFactory;
import org.ovirt.engine.core.tools.common.db.JbossConnectionFactory;

public class ManageDomainsDAOImpl implements ManageDomainsDAO {

    private ManageDomainsConfiguration utilityConfiguration;
    private Connection connection;
    private String actionQuery = "select attach_user_to_su_role(?,?,?)";
    private final static Logger log = Logger.getLogger(ManageDomainsDAOImpl.class);

    public ManageDomainsDAOImpl(ManageDomainsConfiguration utilityConfiguration) throws ConnectException,
            ConfigurationException, ClassNotFoundException, SQLException, XPathExpressionException {
        this.utilityConfiguration = utilityConfiguration;
        connection = getDbConnection();
    }

    @Override
    public boolean updatePermissionsTable(String uuid, String username, String domain) throws SQLException {
        PreparedStatement prepareStatement = null;
        boolean result = false;
        try {
            log.info("uuid: " + uuid + " username: " + username + " domain: " + domain);
            prepareStatement = connection.prepareStatement(actionQuery);
            prepareStatement.setString(1, uuid);
            prepareStatement.setString(2, username);
            prepareStatement.setString(3, domain);
            result = prepareStatement.execute();
        } finally {
            if (prepareStatement != null) {
                prepareStatement.close();
            }
        }
        return result;
    }

    private Connection getDbConnection() throws ClassNotFoundException, SQLException, ConnectException, XPathExpressionException {
        ConnectionFactory factory =
                new JbossConnectionFactory(utilityConfiguration.getJbossDataSourceFilePath(),
                        utilityConfiguration.getLoginConfigFilePath());
        return factory.getConnection();
    }
}
