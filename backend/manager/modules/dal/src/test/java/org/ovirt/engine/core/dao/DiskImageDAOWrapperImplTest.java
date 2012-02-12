package org.ovirt.engine.core.dao;

import static org.mockito.Mockito.mock;

import org.junit.Before;
import org.junit.Test;
import org.ovirt.engine.core.compat.Guid;

/**
 * A sparse test case for {@DiskImageDAOWrapperImpl}
 * intended to fail once we start implementing actual methods
 */
public class DiskImageDAOWrapperImplTest {
    /** The DAO to test */
    private DiskImageDAOWrapperImpl dao;

    @Before
    public void setUp() {
        dao = new DiskImageDAOWrapperImpl();
    }

    @Test (expected=UnsupportedOperationException.class)
    public void testUnimplementedMethods() {
        dao.getAllForVm(mock(Guid.class), mock(Guid.class), true);
    }
}
