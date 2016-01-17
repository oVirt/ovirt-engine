package org.ovirt.engine.core.bll.storage.pool;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;

import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.ovirt.engine.core.bll.BaseCommandTest;
import org.ovirt.engine.core.bll.ValidateTestUtils;
import org.ovirt.engine.core.bll.lock.InMemoryLockManager;
import org.ovirt.engine.core.bll.validator.storage.StoragePoolValidator;
import org.ovirt.engine.core.common.action.ReconstructMasterParameters;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatus;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.businessentities.StoragePoolIsoMap;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.StoragePoolIsoMapDao;
import org.ovirt.engine.core.utils.MockEJBStrategyRule;
import org.ovirt.engine.core.utils.ejb.BeanType;
import org.ovirt.engine.core.utils.lock.LockManager;


public class ReconstructMasterDomainCommandTest extends BaseCommandTest {

    private LockManager lockManager = new InMemoryLockManager();

    @Rule
    public MockEJBStrategyRule ejbRule = new MockEJBStrategyRule(BeanType.LOCK_MANAGER, lockManager);

    @Mock
    private StoragePoolIsoMapDao storagePoolIsoMapDao;

    private ReconstructMasterDomainCommand<ReconstructMasterParameters> cmd;

    private Guid storagePoolId = Guid.newGuid();

    private Guid masterStorageDomainId = Guid.newGuid();

    private Guid regularStorageDomainId = Guid.newGuid();

    private StoragePool storagePool;

    private StorageDomain masterStorageDomain;

    private StoragePoolIsoMap masterDomainIsoMap;

    private StoragePoolIsoMap regularDomainIsoMap;

    private StoragePoolValidator storagePoolValidator;

    @Before
    public void setUp() {
        cmd = spy(new ReconstructMasterDomainCommand<>(new ReconstructMasterParameters(), null));

        initializeStorageDomains();
        initializeStoragePool();
        initializeStoragePoolValidator();
        doReturn(true).when(cmd).initializeVds();
    }

    private void initializeStorageDomains() {
        masterDomainIsoMap = new StoragePoolIsoMap(masterStorageDomainId, storagePoolId, StorageDomainStatus.Active);
        regularDomainIsoMap = new StoragePoolIsoMap(regularStorageDomainId, storagePoolId, StorageDomainStatus.Active);
        masterStorageDomain = new StorageDomain();
        masterStorageDomain.setStoragePoolIsoMapData(masterDomainIsoMap);
        doReturn(masterStorageDomain).when(cmd).getStorageDomain();
    }

    private void initializeStoragePool() {
        storagePool = new StoragePool();
        storagePool.setId(storagePoolId);
        doReturn(Arrays.asList(masterDomainIsoMap, regularDomainIsoMap)).when(storagePoolIsoMapDao).getAllForStoragePool(storagePoolId);
        doReturn(storagePool).when(cmd).getStoragePool();
    }

    private void initializeStoragePoolValidator() {
        storagePoolValidator = spy(new StoragePoolValidator(storagePool));
        doReturn(storagePoolIsoMapDao).when(storagePoolValidator).getStoragePoolIsoMapDao();
        doReturn(storagePoolValidator).when(cmd).createStoragePoolValidator();
    }

    private void validateDomainInProcess(StorageDomainStatus masterStatus, StorageDomainStatus regularStatus,
                                                    StorageDomainStatus expectedStatus) {
        masterDomainIsoMap.setStatus(masterStatus);
        regularDomainIsoMap.setStatus(regularStatus);

        ValidateTestUtils.runAndAssertValidateFailure(cmd,
                EngineMessage.ACTION_TYPE_FAILED_STORAGE_DOMAIN_STATUS_ILLEGAL2);

        List<String> messages = cmd.getReturnValue().getValidationMessages();

        assertEquals(messages.get(0), EngineMessage.ACTION_TYPE_FAILED_STORAGE_DOMAIN_STATUS_ILLEGAL2.toString());
        assertEquals(messages.get(1), String.format("$status %1$s", expectedStatus));
    }

    @Test
    public void testValidateMasterLocked() {
        validateDomainInProcess(StorageDomainStatus.Locked, StorageDomainStatus.Active,
                StorageDomainStatus.Locked);
    }

    @Test
    public void testValidateMasterPreparingForMaintenance() {
        validateDomainInProcess(StorageDomainStatus.PreparingForMaintenance, StorageDomainStatus.Active,
                StorageDomainStatus.PreparingForMaintenance);
    }

    @Test
    public void testValidateRegularLocked() {
        validateDomainInProcess(StorageDomainStatus.Active, StorageDomainStatus.Locked,
                StorageDomainStatus.Locked);
    }

    @Test
    public void testValidateRegularPreparingForMaintenance() {
        validateDomainInProcess(StorageDomainStatus.Active, StorageDomainStatus.PreparingForMaintenance,
                StorageDomainStatus.PreparingForMaintenance);
    }

    @Test
    public void testValidateSuccess() {
        ValidateTestUtils.runAndAssertValidateSuccess(cmd);
    }
}
