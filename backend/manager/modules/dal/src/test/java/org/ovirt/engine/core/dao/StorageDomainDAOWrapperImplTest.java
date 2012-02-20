package org.ovirt.engine.core.dao;

import org.junit.Before;
import org.junit.Test;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.NotImplementedException;

/**
 * A sparse test case for {@DiskImageDAOWrapperImpl}
 * intended to fail once we start implementing actual methods
 */
public class StorageDomainDAOWrapperImplTest {
    /** The DAO to test */
    private StorageDomainDAOWrapperImpl dao;

    @Before
    public void setUp() {
        dao = new StorageDomainDAOWrapperImpl();
    }

    @Test(expected = NotImplementedException.class)
    public void testUnimplementedFilteredGetAllForStoragePool() {
        dao.getAllForStoragePool(Guid.NewGuid(), Guid.NewGuid(), true);
    }

    @Test(expected = NotImplementedException.class)
    public void testUnimplementedFilteredGet() {
        dao.get(Guid.NewGuid(), Guid.NewGuid(), true);
    }
}
