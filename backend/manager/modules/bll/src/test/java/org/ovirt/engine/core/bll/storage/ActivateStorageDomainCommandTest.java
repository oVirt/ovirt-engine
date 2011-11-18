package org.ovirt.engine.core.bll.storage;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.when;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.ovirt.engine.core.common.action.StorageDomainPoolParametersBase;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatus;
import org.ovirt.engine.core.common.businessentities.StoragePoolStatus;
import org.ovirt.engine.core.common.businessentities.storage_domains;
import org.ovirt.engine.core.common.businessentities.storage_pool;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.VdcBllMessages;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dao.StorageDomainDAO;
import org.ovirt.engine.core.dao.StoragePoolDAO;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@PrepareForTest({DbFacade.class})
@RunWith(PowerMockRunner.class)
public class ActivateStorageDomainCommandTest {

    @Mock DbFacade dbFacade;
    @Mock StorageDomainDAO storageDomainDAO;
    @Mock StoragePoolDAO storagePoolDAO;

    private ActivateStorageDomainCommand cmd;

    @Before
    public void setUp() {
        initMocks(this);
        mockStatic(DbFacade.class);
        when(DbFacade.getInstance()).thenReturn(dbFacade);
        when(dbFacade.getStorageDomainDAO()).thenReturn(storageDomainDAO);
        when(dbFacade.getStoragePoolDAO()).thenReturn(storagePoolDAO);
    }

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
        hasIllegalStatusMessage();
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


    private void testInternalExecution(StorageDomainStatus status) {
        testExecution(status);
        setIsInternal();
    }

    private void testExecution(StorageDomainStatus status) {
        createStorageDomain(status);
        createUpStoragePool();
        createCommand();
    }

    private void createUnknownStorageDomain() {
        createStorageDomain(StorageDomainStatus.Unknown);
    }

    private void createLockedStorageDomain() {
        createStorageDomain(StorageDomainStatus.Locked);
    }

    private void createStorageDomain(StorageDomainStatus status) {
       storage_domains domain = new storage_domains();
        domain.setstatus(status);
        domain.setid(Guid.NewGuid());
        when(storageDomainDAO.get(any(Guid.class))).thenReturn(domain);
        when(storageDomainDAO.getForStoragePool(any(Guid.class), any(Guid.class))).thenReturn(domain);
    }

    private void createUpStoragePool() {
        storage_pool pool = new storage_pool();
        pool.setId(Guid.NewGuid());
        pool.setstatus(StoragePoolStatus.Up);
        when(storagePoolDAO.get(any(Guid.class))).thenReturn(pool);
    }

    private void createCommand() {
        StorageDomainPoolParametersBase params = new StorageDomainPoolParametersBase();
        params.setStorageDomainId(Guid.NewGuid());
        params.setStoragePoolId(Guid.NewGuid());
        cmd = new ActivateStorageDomainCommand(params);
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
