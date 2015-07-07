package org.ovirt.engine.core.utils.db;

import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

@Ignore
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
