package org.ovirt.engine.core.bll.storage.domain;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.util.Collections;

import org.junit.Test;
import org.mockito.Mock;
import org.ovirt.engine.core.bll.BaseCommandTest;
import org.ovirt.engine.core.bll.ValidateTestUtils;
import org.ovirt.engine.core.common.action.StorageDomainPoolParametersBase;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatus;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.businessentities.StoragePoolStatus;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.StorageDomainDao;
import org.ovirt.engine.core.dao.StoragePoolDao;
import org.ovirt.engine.core.dao.VdsDao;

public class ActivateStorageDomainCommandTest extends BaseCommandTest {
    @Mock
    private StorageDomainDao storageDomainDao;
    @Mock
    private StoragePoolDao storagePoolDao;
    @Mock
    private VdsDao vdsDao;

    private ActivateStorageDomainCommand<StorageDomainPoolParametersBase> cmd;

    @Test
    public void internalLockedAllowed() {
        internalActionAllowed(StorageDomainStatus.Locked);
    }

    @Test
    public void nonInternalLockedDisallowed() {
        testExecution(StorageDomainStatus.Locked);
        ValidateTestUtils.runAndAssertValidateFailure(cmd, EngineMessage.ACTION_TYPE_FAILED_OBJECT_LOCKED);
    }

    @Test
    public void internalInactiveAllowed() {
        internalActionAllowed(StorageDomainStatus.Inactive);
    }

    @Test
    public void nonInternalInactiveAllowed() {
        nonInternalActionAllowed(StorageDomainStatus.Inactive);
    }

    @Test
    public void nonInternalActiveDisallowed() {
        testExecution(StorageDomainStatus.Active);
        testActionDisallowed();
    }

    @Test
    public void internalActiveDisallowed() {
        testInternalExecution(StorageDomainStatus.Active);
        testActionDisallowed();
    }

    @Test
    public void internalUnknownAllowed() {
        internalActionAllowed(StorageDomainStatus.Unknown);
    }

    @Test
    public void nonInternalUnknownAllowed() {
        nonInternalActionAllowed(StorageDomainStatus.Unknown);
    }

    @Test
    public void internalMaintenanceAllowed() {
        internalActionAllowed(StorageDomainStatus.Maintenance);
    }

    @Test
    public void nonInternalMaintenanceAllowed() {
        nonInternalActionAllowed(StorageDomainStatus.Maintenance);
    }

    @Test
    public void internalPreparingForMaintenanceAllowed() {
        internalActionAllowed(StorageDomainStatus.PreparingForMaintenance);
    }

    @Test
    public void nonInternalPreparingForMaintenanceAllowed() {
        nonInternalActionAllowed(StorageDomainStatus.PreparingForMaintenance);
    }

    @Test
    public void nonActiveVdsDisallowed() {
        testNonActiveVdsExecution(StorageDomainStatus.Maintenance);
        ValidateTestUtils.runAndAssertValidateFailure(cmd, EngineMessage.ACTION_TYPE_FAILED_NO_VDS_IN_POOL);
    }

    private void testActionAllowed() {
        ValidateTestUtils.runAndAssertValidateSuccess(cmd);
    }

    private void testActionDisallowed() {
        ValidateTestUtils.runAndAssertValidateFailure
                (cmd, EngineMessage.ACTION_TYPE_FAILED_STORAGE_DOMAIN_STATUS_ILLEGAL2);
    }

    public void internalActionAllowed(StorageDomainStatus status) {
        testInternalExecution(status);
        testActionAllowed();
    }

    public void nonInternalActionAllowed(StorageDomainStatus status) {
        testExecution(status);
        testActionAllowed();
    }

    private void testNonActiveVdsExecution(StorageDomainStatus status) {
        createStorageDomain(status);
        createUpStoragePool();
        createNonUpVds();
        createCommand();
    }

    private void testInternalExecution(StorageDomainStatus status) {
        testExecution(status);
        setIsInternal();
    }

    private void testExecution(StorageDomainStatus status) {
        createStorageDomain(status);
        createUpStoragePool();
        createUpVds();
        createCommand();
    }

    private void createStorageDomain(StorageDomainStatus status) {
        StorageDomain domain = new StorageDomain();
        domain.setStatus(status);
        domain.setId(Guid.newGuid());
        when(storageDomainDao.get(any(Guid.class))).thenReturn(domain);
        when(storageDomainDao.getForStoragePool(any(Guid.class), any(Guid.class))).thenReturn(domain);
    }

    private void createUpStoragePool() {
        StoragePool pool = new StoragePool();
        pool.setId(Guid.newGuid());
        pool.setStatus(StoragePoolStatus.Up);
        when(storagePoolDao.get(any(Guid.class))).thenReturn(pool);
    }

    private void createUpVds() {
        when(vdsDao.getAllForStoragePoolAndStatus(any(Guid.class), eq(VDSStatus.Up)))
                .thenReturn(Collections.singletonList(new VDS()));
    }

    private void createNonUpVds() {
        when(vdsDao.getAllForStoragePoolAndStatus(any(Guid.class), eq(VDSStatus.Up)))
                .thenReturn(Collections.<VDS>emptyList());
    }

    private void createCommand() {
        StorageDomainPoolParametersBase params = new StorageDomainPoolParametersBase();
        params.setStorageDomainId(Guid.newGuid());
        params.setStoragePoolId(Guid.newGuid());
        cmd = spy(new ActivateStorageDomainCommand<>(params, null));
        doReturn(storageDomainDao).when(cmd).getStorageDomainDao();
        doReturn(storagePoolDao).when(cmd).getStoragePoolDao();
        doReturn(vdsDao).when(cmd).getVdsDao();
    }

    private void setIsInternal() {
        cmd.setInternalExecution(true);
    }

}
