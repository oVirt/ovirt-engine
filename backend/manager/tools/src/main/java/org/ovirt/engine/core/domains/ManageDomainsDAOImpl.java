package org.ovirt.engine.core.domains;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.UUID;

import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.ovirt.engine.core.tools.common.db.StandaloneDataSource;
import org.ovirt.engine.core.utils.db.DbUtils;

public class ManageDomainsDAOImpl implements ManageDomainsDAO {

    private DataSource ds;
    private String actionQuery = "select attach_user_to_su_role(?,?,?,?)";
    private String selectQuery = "select get_user_permissions_for_domain(?,?)";
    private final static Logger log = Logger.getLogger(ManageDomainsDAOImpl.class);

    public ManageDomainsDAOImpl() throws SQLException {
        ds = new StandaloneDataSource();
    }

    @Override
    public boolean updatePermissionsTable(String userId, String userName, String domain) throws SQLException {
        Connection connection = null;
        PreparedStatement prepareStatement = null;
        boolean result = false;
        try {
            log.info("uuid: " + userId + " username: " + userName + " domain: " + domain);
            connection = ds.getConnection();
            prepareStatement = connection.prepareStatement(actionQuery);
            String permissionId = UUID.randomUUID().toString();
            prepareStatement.setObject(1, permissionId, Types.OTHER);
            prepareStatement.setString(2, userId);
            prepareStatement.setString(3, userName);
            prepareStatement.setString(4, domain);
            result = prepareStatement.execute();
        } finally {
            DbUtils.closeQuietly(prepareStatement, connection);
        }
        return result;
    }

    @Override
    public boolean getUserHasPermissions(String userName, String domain) throws SQLException {
        Connection connection = null;
        PreparedStatement prepareStatement = null;
        ResultSet resultSet = null;
        try {
            log.info("getPermissionsForUser  username: " + userName + " domain: " + domain);
            connection = ds.getConnection();
            prepareStatement = connection.prepareStatement(selectQuery);
            prepareStatement.setString(1, userName);
            prepareStatement.setString(2, domain);
            resultSet = prepareStatement.executeQuery();
            if (resultSet.next()) {
                return true;
            }
        } finally {
            DbUtils.closeQuietly(resultSet, prepareStatement, connection);
        }
        return false;
    }
}
