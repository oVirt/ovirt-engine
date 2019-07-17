package org.ovirt.engine.core.bll.storage.domain;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.ovirt.engine.core.bll.BaseCommandTest;
import org.ovirt.engine.core.common.action.StorageDomainManagementParameter;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatic;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.businessentities.storage.LUNs;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.core.dao.StorageDomainStaticDao;
import org.ovirt.engine.core.utils.MockConfigExtension;

@ExtendWith(MockConfigExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class AddExistingBlockStorageDomainCommandTest extends BaseCommandTest {

    private StorageDomainManagementParameter parameters = new StorageDomainManagementParameter(getStorageDomain());

    @Spy
    @InjectMocks
    private AddExistingBlockStorageDomainCommand<StorageDomainManagementParameter> command =
            new AddExistingBlockStorageDomainCommand<>(parameters, null);

    @Mock
    private StorageDomainStaticDao storageDomainStaticDao;

    @BeforeEach
    public void setUp() {
        doNothing().when(command).addStorageDomainInDb();
        doNothing().when(command).updateStorageDomainFromIrs();
        doNothing().when(command).saveLUNsInDB(any());
        doNothing().when(command).updateMetadataDevices();
        command.setStoragePool(getStoragePool());
        command.init();
    }

    @Test
    public void testAddExistingBlockDomainSuccessfully() {
        doReturn(getLUNs()).when(command).getLUNsFromVgInfo();
        command.executeCommand();
        assertTrue(command.getReturnValue().getSucceeded());
    }

    @Test
    public void testAddExistingBlockDomainWhenVgInfoReturnsEmptyLunList() {
        doReturn(Collections.emptyList()).when(command).getLUNsFromVgInfo();
        assertFalse(command.canAddDomain(), "Could not connect to Storage Domain");
        assertTrue(command.getReturnValue()
                        .getValidationMessages()
                        .contains(EngineMessage.ACTION_TYPE_FAILED_PROBLEM_WITH_CANDIDATE_INFO.toString()),
                "Import block Storage Domain should have failed due to empty Lun list returned from VGInfo");
    }

    @Test
    public void testAlreadyExistStorageDomain() {
        when(storageDomainStaticDao.get(any())).thenReturn(getStorageDomain());
        assertFalse(command.canAddDomain(), "Storage Domain already exists");
        assertTrue(
                command.getReturnValue()
                        .getValidationMessages()
                        .contains(EngineMessage.ACTION_TYPE_FAILED_STORAGE_DOMAIN_ALREADY_EXIST.toString()),
                "Import block Storage Domain should have failed due to already existing Storage Domain");
    }

    private static StorageDomainStatic getStorageDomain() {
        StorageDomainStatic storageDomain = new StorageDomainStatic();
        storageDomain.setStorage(Guid.newGuid().toString());
        return storageDomain;
    }

    private static List<LUNs> getLUNs() {
        LUNs lun = new LUNs();
        lun.setId(Guid.newGuid().toString());
        return Collections.singletonList(lun);
    }

    private static StoragePool getStoragePool() {
        StoragePool storagePool = new StoragePool();
        storagePool.setId(Guid.newGuid());
        storagePool.setCompatibilityVersion(Version.getLast());
        return storagePool;
    }
}
