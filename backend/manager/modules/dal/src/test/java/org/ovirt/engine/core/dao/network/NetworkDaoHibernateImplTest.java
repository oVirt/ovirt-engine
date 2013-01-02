package org.ovirt.engine.core.dao.network;

import static org.mockito.Mockito.mock;

import org.apache.commons.lang.NotImplementedException;
import org.junit.Before;
import org.junit.Test;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.NetworkDaoHibernateImpl;

/**
 * A sparse test case for {@link NetworkDaoHibernateImpl}
 * intended to fail once we start implementing actual methods
 */
public class NetworkDaoHibernateImplTest {
    /** The DAO to test */
    private NetworkDaoHibernateImpl dao;

    @Before
    public void setUp() {
        dao = new NetworkDaoHibernateImpl();
    }

    @Test(expected = NotImplementedException.class)
    public void testUnimplementedMethods() {
        dao.getAllForCluster(mock(Guid.class), mock(Guid.class), true);
    }
}
