package org.ovirt.engine.core.bll.storage;

import static org.junit.Assert.assertFalse;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.ovirt.engine.core.bll.CommandAssertUtils.checkMessages;

import org.junit.Before;
import org.junit.Test;
import org.ovirt.engine.core.common.action.RemoveStorageDomainParameters;
import org.ovirt.engine.core.common.errors.VdcBllMessages;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.StorageDomainDAO;
import org.ovirt.engine.core.dao.StoragePoolDAO;

/** A test case for the {@link RemoveStorageDomainCommand} */
public class RemoveStorageDomainCommandTest {

    private Guid storageDomainID;
    private Guid vdsID;
    private Guid storagePoolID;
    private RemoveStorageDomainCommand<RemoveStorageDomainParameters> command;

    private StorageDomainDAO storageDomainDAOMock;
    private StoragePoolDAO storagePoolDAOMock;

    @Before
    public void setUp() {
        storageDomainID = Guid.NewGuid();
        vdsID = Guid.NewGuid();
        storagePoolID = Guid.NewGuid();
        RemoveStorageDomainParameters params = new RemoveStorageDomainParameters();
        params.setVdsId(vdsID);
        params.setStorageDomainId(storageDomainID);
        params.setStoragePoolId(storagePoolID);
        params.setDoFormat(true);

        storageDomainDAOMock = mock(StorageDomainDAO.class);
        storagePoolDAOMock = mock(StoragePoolDAO.class);

        command = spy(new RemoveStorageDomainCommand<RemoveStorageDomainParameters>(params));
        doReturn(storageDomainDAOMock).when(command).getStorageDomainDAO();
        doReturn(storagePoolDAOMock).when(command).getStoragePoolDAO();
    }

    @Test
    public void testCanDoActionNonExistingStorageDomain() {
        // All the mock DAOs return nulls (which mocks the objects do not exist)
        // canDoAction should return false, not crash with NullPointerExcpetion
        assertFalse("canDoActtion shouldn't be possible for a non-existant storage domain", command.canDoAction());
        checkMessages(command,
                VdcBllMessages.ACTION_TYPE_FAILED_STORAGE_DOMAIN_NOT_EXIST);
    }

    @Test
    public void testSetActionMessageParameters() {
        command.setActionMessageParameters();
        checkMessages(command,
                VdcBllMessages.VAR__TYPE__STORAGE__DOMAIN,
                VdcBllMessages.VAR__ACTION__REMOVE);
    }
}
