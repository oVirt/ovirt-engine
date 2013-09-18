package org.ovirt.engine.core.bll.storage;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.ovirt.engine.core.bll.CanDoActionTestUtils;
import org.ovirt.engine.core.common.action.RemoveStorageDomainParameters;
import org.ovirt.engine.core.common.errors.VdcBllMessages;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.StorageDomainDAO;
import org.ovirt.engine.core.dao.StoragePoolDAO;

/** A test case for the {@link RemoveStorageDomainCommand} */
@RunWith(MockitoJUnitRunner.class)
public class RemoveStorageDomainCommandTest {

    private RemoveStorageDomainCommand<RemoveStorageDomainParameters> command;

    @Mock
    private StorageDomainDAO storageDomainDAOMock;

    @Mock
    private StoragePoolDAO storagePoolDAOMock;

    @Before
    public void setUp() {
        Guid storageDomainID = Guid.newGuid();
        Guid vdsID = Guid.newGuid();
        Guid storagePoolID = Guid.newGuid();
        RemoveStorageDomainParameters params = new RemoveStorageDomainParameters();
        params.setVdsId(vdsID);
        params.setStorageDomainId(storageDomainID);
        params.setStoragePoolId(storagePoolID);
        params.setDoFormat(true);

        command = spy(new RemoveStorageDomainCommand<>(params));
        doReturn(storageDomainDAOMock).when(command).getStorageDomainDAO();
        doReturn(storagePoolDAOMock).when(command).getStoragePoolDAO();
    }

    @Test
    public void testCanDoActionNonExistingStorageDomain() {
        // All the mock DAOs return nulls (which mocks the objects do not exist)
        // canDoAction should return false, not crash with NullPointerException
        CanDoActionTestUtils.runAndAssertCanDoActionFailure(
                "canDoAction shouldn't be possible for a non-existent storage domain",
                command, VdcBllMessages.ACTION_TYPE_FAILED_STORAGE_DOMAIN_NOT_EXIST);
    }

    @Test
    public void testSetActionMessageParameters() {
        CanDoActionTestUtils.runAndAssertSetActionMessageParameters(command,
                VdcBllMessages.VAR__TYPE__STORAGE__DOMAIN,
                VdcBllMessages.VAR__ACTION__REMOVE);
    }
}
