package org.ovirt.engine.core.bll.validator.storage;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.storage.StorageServerConnectionExtension;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dao.StorageServerConnectionExtensionDao;
import org.ovirt.engine.core.dao.VdsDao;

@RunWith(MockitoJUnitRunner.class)
public class StorageServerConnectionExtensionValidatorTest {
    @Mock
    private DbFacade dbFacade;

    @Mock
    private VdsDao vdsDao;

    @Mock
    private StorageServerConnectionExtensionDao storageServerConnectionExtensionDao;

    @Spy
    private StorageServerConnectionExtensionValidator validator;

    private StorageServerConnectionExtension conn;

    @Before
    public void setup() {
        Guid hostId = Guid.newGuid();
        doReturn(dbFacade).when(validator).getDbFacade();
        doReturn(storageServerConnectionExtensionDao).when(dbFacade).getStorageServerConnectionExtensionDao();
        doReturn(vdsDao).when(dbFacade).getVdsDao();
        doReturn(new VDS()).when(vdsDao).get(hostId);

        conn = new StorageServerConnectionExtension();
        conn.setHostId(hostId);
        conn.setIqn("iqn1");
        conn.setUserName("user1");
        conn.setPassword("password1");
    }

    @Test
    public void testIsConnectionDoesNotExistForHostAndTargetSucceeds() {
        when(storageServerConnectionExtensionDao.getByHostIdAndTarget(conn.getHostId(), conn.getIqn())).thenReturn(null);
        assertTrue(validator.isConnectionDoesNotExistForHostAndTarget(conn).isValid());
    }

    @Test
    public void testIsConnectionDoesNotExistForHostAndTargetFails() {
        when(storageServerConnectionExtensionDao.getByHostIdAndTarget(conn.getHostId(), conn.getIqn())).thenReturn(new StorageServerConnectionExtension());
        assertEquals(EngineMessage.ACTION_TYPE_FAILED_STORAGE_CONNECTION_EXTENSION_ALREADY_EXISTS, validator.isConnectionDoesNotExistForHostAndTarget(conn).getMessage());
    }
}
