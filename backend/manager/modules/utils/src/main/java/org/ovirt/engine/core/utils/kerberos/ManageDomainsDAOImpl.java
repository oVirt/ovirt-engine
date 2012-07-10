package org.ovirt.engine.core.utils.kerberos;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.ovirt.engine.core.tools.common.db.StandaloneDataSource;

public class ManageDomainsDAOImpl implements ManageDomainsDAO {

    private DataSource ds;
    private String actionQuery = "select attach_user_to_su_role(?,?,?)";
    private final static Logger log = Logger.getLogger(ManageDomainsDAOImpl.class);

    public ManageDomainsDAOImpl() throws SQLException {
        ds = new StandaloneDataSource();
    }

    @Override
    public boolean updatePermissionsTable(String uuid, String username, String domain) throws SQLException {
        Connection connection = null;
        PreparedStatement prepareStatement = null;
        boolean result = false;
        try {
            log.info("uuid: " + uuid + " username: " + username + " domain: " + domain);
            connection = ds.getConnection();
            prepareStatement = connection.prepareStatement(actionQuery);
            prepareStatement.setString(1, uuid);
            prepareStatement.setString(2, username);
            prepareStatement.setString(3, domain);
            result = prepareStatement.execute();
        } finally {
            if (prepareStatement != null) {
                prepareStatement.close();
            }
            if (connection != null) {
                connection.close();
            }
        }
        return result;
    }
}
