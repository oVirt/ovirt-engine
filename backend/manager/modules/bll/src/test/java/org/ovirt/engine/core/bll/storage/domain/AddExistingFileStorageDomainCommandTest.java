package org.ovirt.engine.core.bll.storage.domain;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
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
import org.ovirt.engine.core.bll.ValidateTestUtils;
import org.ovirt.engine.core.common.action.StorageDomainManagementParameter;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatic;
import org.ovirt.engine.core.common.businessentities.StorageDomainType;
import org.ovirt.engine.core.common.businessentities.StorageFormatType;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.core.dao.StorageDomainStaticDao;
import org.ovirt.engine.core.dao.StoragePoolDao;
import org.ovirt.engine.core.dao.VdsDao;
import org.ovirt.engine.core.utils.MockConfigExtension;

@ExtendWith(MockConfigExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class AddExistingFileStorageDomainCommandTest extends BaseCommandTest {

    private StorageDomainManagementParameter parameters = new StorageDomainManagementParameter(getStorageDomain());

    @Spy
    @InjectMocks
    private AddExistingFileStorageDomainCommand<StorageDomainManagementParameter> command =
            new AddExistingFileStorageDomainCommand<>(parameters, null);

    @Mock
    private VdsDao vdsDao;

    @Mock
    private StoragePoolDao storagePoolDao;

    @Mock
    private StorageDomainStaticDao storageDomainStaticDao;

    @BeforeEach
    public void setUp() {
        command.setStoragePool(getStoragePool());

        doReturn(false).when(command).isStorageWithSameNameExists();

        doNothing().when(command).addStorageDomainInDb();
        doNothing().when(command).updateStorageDomainFromIrs();
        command.init();

        when(vdsDao.getAllForStoragePoolAndStatus(any(), eq(VDSStatus.Up))).thenReturn(getHosts());
        when(storagePoolDao.get(any())).thenReturn(getStoragePool());
    }

    @Test
    public void testAddExistingSuccessfully() {
        StorageDomainStatic sdStatic = command.getStorageDomain().getStorageStaticData();
        doReturn(new Pair<>(sdStatic, sdStatic.getId())).when(command).executeHSMGetStorageDomainInfo(any());

        ValidateTestUtils.runAndAssertValidateSuccess(command);

        command.executeCommand();
        assertTrue(command.getReturnValue().getSucceeded());
    }

    @Test
    public void testAlreadyExistStorageDomain() {
        when(storageDomainStaticDao.get(any())).thenReturn(parameters.getStorageDomain());

        ValidateTestUtils.runAndAssertValidateFailure(command,
                EngineMessage.ACTION_TYPE_FAILED_STORAGE_DOMAIN_ALREADY_EXIST);
    }

    @Test
    public void testNonExistingStorageDomain() {
        doReturn(null).when(command).executeHSMGetStorageDomainInfo(any());

        ValidateTestUtils.runAndAssertValidateFailure(command,
                EngineMessage.ACTION_TYPE_FAILED_STORAGE_DOMAIN_NOT_EXIST);
    }

    @Test
    public void testSwitchStorageDomainType() {
        StorageDomainStatic sdStatic = command.getStorageDomain().getStorageStaticData();
        doReturn(new Pair<>(sdStatic, sdStatic.getId())).when(command).executeHSMGetStorageDomainInfo(any());

        ValidateTestUtils.runAndAssertValidateSuccess(command);
    }

    private static StorageDomainStatic getStorageDomain() {
        StorageDomainStatic storageDomain = new StorageDomainStatic();
        storageDomain.setStorage(Guid.newGuid().toString());
        storageDomain.setStorageDomainType(StorageDomainType.Data);
        storageDomain.setStorageFormat(StorageFormatType.V3);
        return storageDomain;
    }

    private static StoragePool getStoragePool() {
        StoragePool storagePool = new StoragePool();
        storagePool.setId(Guid.newGuid());
        storagePool.setCompatibilityVersion(Version.getLast());
        return storagePool;
    }

    private static List<VDS> getHosts() {
        VDS host = new VDS();
        host.setId(Guid.newGuid());
        host.setStatus(VDSStatus.Up);
        return Collections.singletonList(host);
    }
}
