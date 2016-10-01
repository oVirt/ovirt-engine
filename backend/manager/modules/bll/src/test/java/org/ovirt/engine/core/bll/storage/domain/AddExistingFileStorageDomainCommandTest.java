package org.ovirt.engine.core.bll.storage.domain;

import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
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
import org.ovirt.engine.core.common.vdscommands.HSMGetStorageDomainInfoVDSCommandParameters;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.core.dao.StorageDomainStaticDao;
import org.ovirt.engine.core.dao.StoragePoolDao;
import org.ovirt.engine.core.dao.VdsDao;
import org.ovirt.engine.core.utils.MockConfigRule;

public class AddExistingFileStorageDomainCommandTest extends BaseCommandTest {

    private StorageDomainManagementParameter parameters = new StorageDomainManagementParameter(getStorageDomain());

    @Spy
    @InjectMocks
    private AddExistingFileStorageDomainCommand<StorageDomainManagementParameter> command =
            new AddExistingFileStorageDomainCommand<>(parameters, null);

    @ClassRule
    public static MockConfigRule mcr = new MockConfigRule();

    @Mock
    private VdsDao vdsDao;

    @Mock
    private StoragePoolDao storagePoolDao;

    @Mock
    private StorageDomainStaticDao storageDomainStaticDao;

    @Before
    public void setUp() {
        command.setStoragePool(getStoragePool());

        doReturn(false).when(command).isStorageWithSameNameExists();

        doNothing().when(command).addStorageDomainInDb();
        doNothing().when(command).updateStorageDomainDynamicFromIrs();

        when(vdsDao.getAllForStoragePoolAndStatus(any(Guid.class), eq(VDSStatus.Up))).thenReturn(getHosts());
        when(storagePoolDao.get(any(Guid.class))).thenReturn(getStoragePool());
    }

    @Test
    public void testAddExistingSuccessfully() {
        StorageDomainStatic sdStatic = command.getStorageDomain().getStorageStaticData();
        doReturn(new Pair<>(sdStatic, sdStatic.getId())).when(command).executeHSMGetStorageDomainInfo(
                any(HSMGetStorageDomainInfoVDSCommandParameters.class));

        ValidateTestUtils.runAndAssertValidateSuccess(command);

        command.executeCommand();
        assertTrue(command.getReturnValue().getSucceeded());
    }

    @Test
    public void testAlreadyExistStorageDomain() {
        when(storageDomainStaticDao.get(any(Guid.class))).thenReturn(parameters.getStorageDomain());

        ValidateTestUtils.runAndAssertValidateFailure(command,
                EngineMessage.ACTION_TYPE_FAILED_STORAGE_DOMAIN_ALREADY_EXIST);
    }

    @Test
    public void testNonExistingStorageDomain() {
        doReturn(null).when(command).executeHSMGetStorageDomainInfo(
                any(HSMGetStorageDomainInfoVDSCommandParameters.class));

        ValidateTestUtils.runAndAssertValidateFailure(command,
                EngineMessage.ACTION_TYPE_FAILED_STORAGE_DOMAIN_NOT_EXIST);
    }

    @Test
    public void testSwitchStorageDomainType() {
        StorageDomainStatic sdStatic = command.getStorageDomain().getStorageStaticData();
        doReturn(new Pair<>(sdStatic, sdStatic.getId())).when(command).executeHSMGetStorageDomainInfo(
                any(HSMGetStorageDomainInfoVDSCommandParameters.class));

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
