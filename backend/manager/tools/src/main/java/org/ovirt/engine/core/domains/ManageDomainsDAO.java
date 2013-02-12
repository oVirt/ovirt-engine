package org.ovirt.engine.core.domains;

import java.sql.SQLException;

public interface ManageDomainsDAO {
    boolean updatePermissionsTable(String uuid, String username, String domain) throws SQLException;
    boolean getUserHasPermissions(String userName, String domain) throws SQLException;
}

