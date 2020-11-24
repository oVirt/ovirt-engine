package org.ovirt.engine.core.bll.storage.pool;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.ovirt.engine.core.bll.BaseCommandTest;
import org.ovirt.engine.core.bll.ValidateTestUtils;
import org.ovirt.engine.core.common.action.SwitchMasterStorageDomainCommandParameters;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatus;
import org.ovirt.engine.core.common.businessentities.StorageDomainType;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.businessentities.StoragePoolIsoMap;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.core.dao.StorageDomainDao;
import org.ovirt.engine.core.dao.StorageDomainStaticDao;
import org.ovirt.engine.core.dao.StoragePoolDao;
import org.ovirt.engine.core.dao.StoragePoolIsoMapDao;

@MockitoSettings(strictness = Strictness.LENIENT)
public class SwitchMasterStorageDomainCommandTest extends BaseCommandTest {

    @Mock
    private StorageDomainDao storageDomainDao;
    @Mock
    private StoragePoolDao storagePoolDao;
    @Mock
    private StorageDomainStaticDao storageDomainStaticDao;
    @Mock
    private StoragePoolIsoMapDao storagePoolIsoMapDao;

    private static final int MASTER_VERSION = 100;
    private Guid storagePoolId = Guid.newGuid();
    private Guid currentMasterStorageDomainId = Guid.newGuid();
    private Guid newMasterStorageDomainId = Guid.newGuid();
    private StoragePool storagePool;
    private StorageDomain newMasterStorageDomain;
    private StorageDomain currentMasterStorageDomain;

    @InjectMocks
    @Spy
    private SwitchMasterStorageDomainCommand<SwitchMasterStorageDomainCommandParameters> command =
            new SwitchMasterStorageDomainCommand<>(
                    new SwitchMasterStorageDomainCommandParameters(storagePoolId, newMasterStorageDomainId),
                    null);

    @BeforeEach
    public void setup() {
        initializeStoragePool();
        initializeStorageDomains();
        mockCommand();
    }

    @Test
    public void testNullStoragePool() {
        when(command.getStoragePool()).thenReturn(null);
        ValidateTestUtils.runAndAssertValidateFailure
                ("validate did not fail on a null storage pool", command,
                        EngineMessage.ACTION_TYPE_FAILED_STORAGE_POOL_NOT_EXIST);
    }

    @Test
    public void testEmptyDomainID() {
        when(command.getStorageDomainId()).thenReturn(Guid.Empty);
        ValidateTestUtils.runAndAssertValidateFailure
                ("validate did not fail on an empty storage domain ID", command,
                        EngineMessage.STORAGE_DOMAIN_DOES_NOT_EXIST);
    }

    @Test
    public void testAlreadyMasterDomainID() {
        when(command.getStorageDomainId()).thenReturn(currentMasterStorageDomainId);
        ValidateTestUtils.runAndAssertValidateFailure
                ("validate did not fail on the current master storage domain ID", command,
                        EngineMessage.ACTION_TYPE_FAILED_STORAGE_DOMAIN_ALREADY_MASTER);
    }

    @Test
    public void testDomainNotInPool() {
        doReturn(null).when(storageDomainDao).getForStoragePool(newMasterStorageDomainId, storagePoolId);
        ValidateTestUtils.runAndAssertValidateFailure
                ("validate did not fail on storage domain that not in the pool", command,
                        EngineMessage.ACTION_TYPE_FAILED_STORAGE_DOMAIN_NOT_IN_STORAGE_POOL);
    }

    @Test
    public void testDomainNotActive() {
        newMasterStorageDomain.setStatus(StorageDomainStatus.Maintenance);
        ValidateTestUtils.runAndAssertValidateFailure
                ("validate did not fail on storage domain that is not active", command,
                        EngineMessage.ACTION_TYPE_FAILED_STORAGE_DOMAIN_MUST_BE_ACTIVE);
    }

    @Test
    public void testDomainMarkedAsBackup() {
        newMasterStorageDomain.setBackup(true);
        ValidateTestUtils.runAndAssertValidateFailure
                ("validate did not fail on storage domain that marked as backup", command,
                        EngineMessage.ACTION_TYPE_FAILED_ACTION_IS_SUPPORTED_ONLY_FOR_DATA_DOMAINS);
    }

    @Test
    public void testHasRunningTasks() {
        doReturn(true).when(command).hasRunningTasks(storagePoolId);
        ValidateTestUtils.runAndAssertValidateFailure
                ("validate did not fail on storage pool that has running tasks", command,
                        EngineMessage.ACTION_TYPE_FAILED_STORAGE_POOL_HAS_RUNNING_TASKS);
    }

    @Test
    public void testValidateSuccess() {
        ValidateTestUtils.runAndAssertValidateSuccess(command);
    }

    @Test
    public void testExecuteCommand() {
        doReturn(MASTER_VERSION + 1).when(storagePoolDao).increaseStoragePoolMasterVersion(storagePoolId);

        List<StorageDomain> listStorageDomains = Arrays.asList(newMasterStorageDomain, currentMasterStorageDomain);
        listStorageDomains.forEach(sd -> {
            doNothing().when(storageDomainStaticDao).update(sd.getStorageStaticData());
            doNothing().when(storagePoolIsoMapDao)
                    .updateStatus(sd.getStoragePoolIsoMapData().getId(), sd.getStoragePoolIsoMapData().getStatus());
        });

        // Skip the VDS call
        command.updateStoragePoolOnDB();
        assertEquals(MASTER_VERSION + 1, storagePool.getMasterDomainVersion());
        assertEquals(StorageDomainType.Master, newMasterStorageDomain.getStorageDomainType());
        assertEquals(StorageDomainType.Data, currentMasterStorageDomain.getStorageDomainType());
    }

    private void mockCommand() {
        command.getParameters().setCurrentMasterStorageDomainId(currentMasterStorageDomainId);
        doReturn(currentMasterStorageDomainId).when(storageDomainDao).getMasterStorageDomainIdForPool(storagePoolId);
        doReturn(newMasterStorageDomain).when(storageDomainDao).getForStoragePool(newMasterStorageDomainId,
                storagePoolId);
        doReturn(currentMasterStorageDomain).when(storageDomainDao).getForStoragePool(currentMasterStorageDomainId,
                storagePoolId);
        doReturn(false).when(command).hasRunningTasks(storagePoolId);
    }

    private void initializeStoragePool() {
        storagePool = new StoragePool();
        storagePool.setId(storagePoolId);
        storagePool.setCompatibilityVersion(Version.getLast());
        storagePool.setMasterDomainVersion(MASTER_VERSION);
        doReturn(storagePool).when(command).getStoragePool();
    }

    private void initializeStorageDomains() {
        currentMasterStorageDomain = new StorageDomain();
        newMasterStorageDomain = new StorageDomain();
        createStorageDomain(currentMasterStorageDomain, currentMasterStorageDomainId, StorageDomainType.Master);
        createStorageDomain(newMasterStorageDomain, newMasterStorageDomainId, StorageDomainType.Data);

        doReturn(newMasterStorageDomain).when(command).getStorageDomain();
        doReturn(newMasterStorageDomainId).when(command).getStorageDomainId();
    }

    private void createStorageDomain(StorageDomain sd, Guid id, StorageDomainType type) {
        sd.setId(id);
        StoragePoolIsoMap isoMap = new StoragePoolIsoMap(id, storagePoolId, StorageDomainStatus.Active);
        sd.setStoragePoolIsoMapData(isoMap);
        sd.getStorageStaticData().setStorageDomainType(type);
    }
}
