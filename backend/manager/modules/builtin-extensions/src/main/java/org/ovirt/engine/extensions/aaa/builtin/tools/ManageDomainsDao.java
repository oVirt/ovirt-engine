package org.ovirt.engine.extensions.aaa.builtin.tools;

import java.sql.SQLException;

public interface ManageDomainsDao {
    boolean updatePermissionsTable(String uuid, String username, String domain) throws SQLException;
    boolean getUserHasPermissions(String userName, String domain) throws SQLException;
}

