package org.ovirt.engine.core.bll.storage.domain;

import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;
import static org.ovirt.engine.core.common.utils.MockConfigRule.mockConfig;

import java.util.Collections;
import java.util.List;

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.mockito.Mock;
import org.ovirt.engine.core.bll.BaseCommandTest;
import org.ovirt.engine.core.bll.ValidateTestUtils;
import org.ovirt.engine.core.common.action.StorageDomainManagementParameter;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatic;
import org.ovirt.engine.core.common.businessentities.StorageDomainType;
import org.ovirt.engine.core.common.businessentities.StorageFormatType;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.utils.MockConfigRule;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.common.vdscommands.HSMGetStorageDomainInfoVDSCommandParameters;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.core.dao.StorageDomainStaticDao;
import org.ovirt.engine.core.dao.StoragePoolDao;
import org.ovirt.engine.core.dao.VdsDao;

public class AddExistingFileStorageDomainCommandTest extends BaseCommandTest {

    private AddExistingFileStorageDomainCommand<StorageDomainManagementParameter> command;
    private StorageDomainManagementParameter parameters;

    @ClassRule
    public static MockConfigRule mcr = new MockConfigRule(
            mockConfig(ConfigValues.StorageDomainNameSizeLimit, 50),
            mockConfig(ConfigValues.WarningLowSpaceIndicator, 10),
            mockConfig(ConfigValues.CriticalSpaceActionBlocker, 5)
    );

    @Mock
    private VdsDao vdsDao;

    @Mock
    private StoragePoolDao storagePoolDao;

    @Mock
    private StorageDomainStaticDao storageDomainStaticDao;

    @Before
    public void setUp() {
        parameters = new StorageDomainManagementParameter(getStorageDomain());
        parameters.setVdsId(Guid.newGuid());
        parameters.setStoragePoolId(Guid.newGuid());
        command = spy(new AddExistingFileStorageDomainCommand<>(parameters, null));

        command.setStoragePool(getStoragePool());

        doReturn(vdsDao).when(command).getVdsDao();
        doReturn(storagePoolDao).when(command).getStoragePoolDao();
        doReturn(storageDomainStaticDao).when(command).getStorageDomainStaticDao();

        doReturn(false).when(command).isStorageWithSameNameExists();

        doNothing().when(command).addStorageDomainInDb();
        doNothing().when(command).updateStorageDomainDynamicFromIrs();

        when(command.getVdsDao().getAllForStoragePoolAndStatus(any(Guid.class), eq(VDSStatus.Up))).thenReturn(getHosts());
        when(command.getStoragePoolDao().get(any(Guid.class))).thenReturn(getStoragePool());
    }

    @Test
    public void testAddExistingSuccessfully() {
        when(command.getStorageDomainStaticDao().get(any(Guid.class))).thenReturn(null);

        StorageDomainStatic sdStatic = command.getStorageDomain().getStorageStaticData();
        doReturn(new Pair<>(sdStatic, sdStatic.getId())).when(command).executeHSMGetStorageDomainInfo(
                any(HSMGetStorageDomainInfoVDSCommandParameters.class));

        ValidateTestUtils.runAndAssertValidateSuccess(command);

        command.executeCommand();
        assertTrue(command.getReturnValue().getSucceeded());
    }

    @Test
    public void testAlreadyExistStorageDomain() {
        when(command.getStorageDomainStaticDao().get(any(Guid.class))).thenReturn(parameters.getStorageDomain());

        ValidateTestUtils.runAndAssertValidateFailure(command,
                EngineMessage.ACTION_TYPE_FAILED_STORAGE_DOMAIN_ALREADY_EXIST);
    }

    @Test
    public void testNonExistingStorageDomain() {
        when(command.getStorageDomainStaticDao().get(any(Guid.class))).thenReturn(null);

        doReturn(null).when(command).executeHSMGetStorageDomainInfo(
                any(HSMGetStorageDomainInfoVDSCommandParameters.class));

        ValidateTestUtils.runAndAssertValidateFailure(command,
                EngineMessage.ACTION_TYPE_FAILED_STORAGE_DOMAIN_NOT_EXIST);
    }

    @Test
    public void testSwitchStorageDomainType() {
        when(command.getStorageDomainStaticDao().get(any(Guid.class))).thenReturn(null);

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
