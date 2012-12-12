package org.ovirt.engine.core.dao;

import static org.mockito.Mockito.mock;

import org.apache.commons.lang.NotImplementedException;
import org.junit.Before;
import org.junit.Test;
import org.ovirt.engine.core.compat.Guid;

/**
 * A sparse test case for {@link NetworkDAOHibernateImpl}
 * intended to fail once we start implementing actual methods
 */
public class NetworkDAOHibernateImplTest {
    /** The DAO to test */
    private NetworkDAOHibernateImpl dao;

    @Before
    public void setUp() {
        dao = new NetworkDAOHibernateImpl();
    }

    @Test(expected = NotImplementedException.class)
    public void testUnimplementedMethods() {
        dao.getAllForCluster(mock(Guid.class), mock(Guid.class), true);
    }
}
