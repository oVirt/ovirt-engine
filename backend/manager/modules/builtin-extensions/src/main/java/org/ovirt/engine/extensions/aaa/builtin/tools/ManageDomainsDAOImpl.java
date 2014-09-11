package org.ovirt.engine.extensions.aaa.builtin.tools;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.ovirt.engine.core.utils.db.StandaloneDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ManageDomainsDAOImpl implements ManageDomainsDAO {

    private static final String SUPER_USER = "SuperUser";
    private DataSource ds;
    private String actionQuery = "select attach_user_to_role(?,?,?,?)";
    private String selectQuery = "select get_user_permissions_for_domain(?,?)";
    private final static Logger log = LoggerFactory.getLogger(ManageDomainsDAOImpl.class);

    public ManageDomainsDAOImpl() throws SQLException {
        ds = new StandaloneDataSource();
    }

    @Override
    public boolean updatePermissionsTable(String userId, String userName, String domain) throws SQLException {
        boolean result = false;
        try (Connection connection = ds.getConnection();
                PreparedStatement prepareStatement = connection.prepareStatement(actionQuery);
            ) {
            log.info("uuid: {} username: {} domain: {}", userId, userName, domain);
            prepareStatement.setString(1, userId);
            prepareStatement.setString(2, userName);
            prepareStatement.setString(3, domain);
            prepareStatement.setString(4, SUPER_USER);
            result = prepareStatement.execute();
        }
        return result;
    }

    @Override
    public boolean getUserHasPermissions(String userName, String domain) throws SQLException {
        boolean result = false;
        try (Connection connection = ds.getConnection();
                PreparedStatement prepareStatement = connection.prepareStatement(selectQuery);) {
            log.info("getPermissionsForUser  username: {} domain: {}", userName, domain);
            prepareStatement.setString(1, userName);
            prepareStatement.setString(2, domain);

            try (ResultSet resultSet = prepareStatement.executeQuery()) {
                result = resultSet.next();
            }
        }
        return result;
    }
}
