package org.ovirt.engine.core.utils.kerberos;

import java.sql.SQLException;

public interface ManageDomainsDAO {
    boolean updatePermissionsTable(String uuid, String username, String domain) throws SQLException;
}

