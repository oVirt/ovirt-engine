package org.ovirt.engine.core.bll.storage.domain;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.util.Collections;

import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
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

@MockitoSettings(strictness = Strictness.LENIENT)
public class ActivateStorageDomainCommandTest extends BaseCommandTest {
    @Mock
    private StorageDomainDao storageDomainDao;
    @Mock
    private StoragePoolDao storagePoolDao;
    @Mock
    private VdsDao vdsDao;

    @InjectMocks
    private ActivateStorageDomainCommand<StorageDomainPoolParametersBase> cmd =
            new ActivateStorageDomainCommand<>(new StorageDomainPoolParametersBase(Guid.newGuid(), Guid.newGuid()), null);

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
        when(storageDomainDao.get(any())).thenReturn(domain);
        when(storageDomainDao.getForStoragePool(any(), any())).thenReturn(domain);
    }

    private void createUpStoragePool() {
        StoragePool pool = new StoragePool();
        pool.setId(Guid.newGuid());
        pool.setStatus(StoragePoolStatus.Up);
        when(storagePoolDao.get(any())).thenReturn(pool);
    }

    private void createUpVds() {
        when(vdsDao.getAllForStoragePoolAndStatus(any(), eq(VDSStatus.Up))).thenReturn
                (Collections.singletonList(new VDS()));
    }

    private void createCommand() {
        cmd.init();
    }

    private void setIsInternal() {
        cmd.setInternalExecution(true);
    }

}
