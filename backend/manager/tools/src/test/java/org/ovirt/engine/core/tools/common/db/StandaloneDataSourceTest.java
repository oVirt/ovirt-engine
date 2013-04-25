package org.ovirt.engine.core.tools.common.db;

import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.junit.Test;
import org.junit.Assert;

public class StandaloneDataSourceTest {

    @Test
    public void testOpenConnection() throws SQLException {
        DataSource ds = new StandaloneDataSource();
        Connection connection = null;
        try {
            connection = ds.getConnection();
            Assert.assertNotNull(connection);
        }
        finally {
            if (connection != null) {
                connection.close();
            }
        }
    }

}
