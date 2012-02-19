package org.ovirt.engine.core.dao;

import org.junit.Test;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.NotImplementedException;

/**
 * A sparse test case for {@link StoragePoolHibernateImpl}
 * intended to fail once we start implementing actual methods
 */
public class StoragePoolDAOHibernateImplTest {

    @Test(expected = NotImplementedException.class)
    public void testUnimplementedMethod() {
        new StoragePoolDAOHibernateImpl().get(new Guid(), new Guid(), true);
    }
}
