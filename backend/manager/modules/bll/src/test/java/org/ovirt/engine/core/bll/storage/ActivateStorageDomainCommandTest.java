package org.ovirt.engine.core.bll.storage;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.ovirt.engine.core.common.action.StorageDomainPoolParametersBase;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatus;
import org.ovirt.engine.core.common.businessentities.StoragePoolStatus;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.errors.VdcBllMessages;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.StorageDomainDAO;
import org.ovirt.engine.core.dao.StoragePoolDAO;

@RunWith(MockitoJUnitRunner.class)
public class ActivateStorageDomainCommandTest {
    @Mock
    StorageDomainDAO storageDomainDAO;
    @Mock
    StoragePoolDAO storagePoolDAO;

    private ActivateStorageDomainCommand<StorageDomainPoolParametersBase> cmd;

    @Test
    public void internalLockedAllowed() {
        testInternalExecution(StorageDomainStatus.Locked);
        canDoActionSucceeds();
        noIllegalStatusMessage();
    }

    @Test
    public void nonInternalLockedDisallowed() {
        testExecution(StorageDomainStatus.Locked);
        canDoActionFails();
        assertTrue(cmd.getReturnValue().getCanDoActionMessages().contains(
                VdcBllMessages.ACTION_TYPE_FAILED_OBJECT_LOCKED.name()));
    }

    @Test
    public void internalInactiveAllowed() {
        testInternalExecution(StorageDomainStatus.InActive);
        canDoActionSucceeds();
        noIllegalStatusMessage();
    }

    @Test
    public void nonInternalInactiveAllowed() {
        testExecution(StorageDomainStatus.InActive);
        canDoActionSucceeds();
        noIllegalStatusMessage();
    }

    @Test
    public void activeDisallowed() {
        testExecution(StorageDomainStatus.Active);
        canDoActionFails();
        hasIllegalStatusMessage();
    }

    @Test
    public void internalActiveDisallowed() {
        testInternalExecution(StorageDomainStatus.Active);
        canDoActionFails();
        hasIllegalStatusMessage();
    }

    @Test
    public void internalUnknownAllowed() {
        testInternalExecution(StorageDomainStatus.Unknown);
        canDoActionSucceeds();
        noIllegalStatusMessage();
    }

    @Test
    public void nonInternalUnknownAllowed() {
        testExecution(StorageDomainStatus.Unknown);
        canDoActionSucceeds();
        noIllegalStatusMessage();
    }

    @Test
    public void internalMaintenanceAllowed() {
        testInternalExecution(StorageDomainStatus.Maintenance);
        canDoActionSucceeds();
        noIllegalStatusMessage();
    }

    @Test
    public void nonInternalMaintenanceAllowed() {
        testExecution(StorageDomainStatus.Maintenance);
        canDoActionSucceeds();
        noIllegalStatusMessage();
    }

    private void testInternalExecution(StorageDomainStatus status) {
        testExecution(status);
        setIsInternal();
    }

    private void testExecution(StorageDomainStatus status) {
        createStorageDomain(status);
        createUpStoragePool();
        createCommand();
    }

    private void createStorageDomain(StorageDomainStatus status) {
        StorageDomain domain = new StorageDomain();
        domain.setStatus(status);
        domain.setId(Guid.newGuid());
        when(storageDomainDAO.get(any(Guid.class))).thenReturn(domain);
        when(storageDomainDAO.getForStoragePool(any(Guid.class), any(Guid.class))).thenReturn(domain);
    }

    private void createUpStoragePool() {
        StoragePool pool = new StoragePool();
        pool.setId(Guid.newGuid());
        pool.setstatus(StoragePoolStatus.Up);
        when(storagePoolDAO.get(any(Guid.class))).thenReturn(pool);
    }

    private void createCommand() {
        StorageDomainPoolParametersBase params = new StorageDomainPoolParametersBase();
        params.setStorageDomainId(Guid.newGuid());
        params.setStoragePoolId(Guid.newGuid());
        cmd = spy(new ActivateStorageDomainCommand<StorageDomainPoolParametersBase>(params));
        doReturn(storageDomainDAO).when(cmd).getStorageDomainDAO();
        doReturn(storagePoolDAO).when(cmd).getStoragePoolDAO();
    }

    private void canDoActionSucceeds() {
        assertTrue(cmd.canDoAction());
    }

    private void canDoActionFails() {
        assertFalse(cmd.canDoAction());
    }

    private void noIllegalStatusMessage() {
        assertFalse(cmd.getReturnValue().getCanDoActionMessages().contains(
                VdcBllMessages.ACTION_TYPE_FAILED_STORAGE_DOMAIN_STATUS_ILLEGAL.toString()));
    }

    private void hasIllegalStatusMessage() {
        assertTrue(cmd.getReturnValue().getCanDoActionMessages().contains(
                VdcBllMessages.ACTION_TYPE_FAILED_STORAGE_DOMAIN_STATUS_ILLEGAL.toString()));
    }

    private void setIsInternal() {
        cmd.setInternalExecution(true);
    }

}
