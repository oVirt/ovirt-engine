package org.ovirt.engine.core.bll.storage;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;
import static org.ovirt.engine.core.utils.MockConfigRule.mockConfig;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.ovirt.engine.core.bll.CanDoActionTestUtils;
import org.ovirt.engine.core.common.action.StorageDomainManagementParameter;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatic;
import org.ovirt.engine.core.common.businessentities.StorageDomainType;
import org.ovirt.engine.core.common.businessentities.StorageFormatType;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.errors.VdcBllMessages;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.common.vdscommands.HSMGetStorageDomainInfoVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.HSMGetStorageDomainsListVDSCommandParameters;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dao.StorageDomainStaticDAO;
import org.ovirt.engine.core.dao.StoragePoolDAO;
import org.ovirt.engine.core.dao.VdsDAO;
import org.ovirt.engine.core.utils.MockConfigRule;

@RunWith(MockitoJUnitRunner.class)
public class AddExistingFileStorageDomainCommandTest {

    private AddExistingFileStorageDomainCommand<StorageDomainManagementParameter> command;
    private StorageDomainManagementParameter parameters;

    private static final int SD_MAX_NAME_LENGTH = 50;

    @ClassRule
    public static MockConfigRule mcr = new MockConfigRule(
            mockConfig(ConfigValues.StorageDomainNameSizeLimit, SD_MAX_NAME_LENGTH),
            mockConfig(ConfigValues.SupportedStorageFormats, Version.v3_4.toString(), "3"),
            mockConfig(ConfigValues.SupportedStorageFormats, Version.v3_5.toString(), "3")
    );

    @Mock
    private DbFacade dbFacade;

    @Mock
    private VdsDAO vdsDAO;

    @Mock
    private StoragePoolDAO storagePoolDAO;

    @Mock
    private StorageDomainStaticDAO storageDomainStaticDAO;

    @Before
    public void setUp() {
        parameters = new StorageDomainManagementParameter(getStorageDomain());
        parameters.setVdsId(Guid.newGuid());
        parameters.setStoragePoolId(Guid.newGuid());
        command = spy(new AddExistingFileStorageDomainCommand<>(parameters));

        command.setStoragePool(getStoragePool());

        doReturn(dbFacade).when(command).getDbFacade();
        doReturn(vdsDAO).when(command).getVdsDAO();
        doReturn(storagePoolDAO).when(command).getStoragePoolDAO();
        doReturn(storageDomainStaticDAO).when(command).getStorageDomainStaticDAO();

        doReturn(false).when(command).isStorageWithSameNameExists();

        doNothing().when(command).addStorageDomainInDb();
        doNothing().when(command).updateStorageDomainDynamicFromIrs();

        when(command.getVdsDAO().getAllForStoragePoolAndStatus(any(Guid.class), eq(VDSStatus.Up))).thenReturn(getHosts());
        when(command.getStoragePoolDAO().get(any(Guid.class))).thenReturn(getStoragePool());
    }

    @Test
    public void testCandoPassSuccessfully() {
        when(command.getStorageDomainStaticDAO().get(any(Guid.class))).thenReturn(null);

        StorageDomainStatic sdStatic = command.getStorageDomain().getStorageStaticData();
        doReturn(new Pair<>(sdStatic, sdStatic.getId())).when(command).executeHSMGetStorageDomainInfo(
                any(HSMGetStorageDomainInfoVDSCommandParameters.class));
        doReturn(Collections.singletonList(sdStatic.getId())).when(command).executeHSMGetStorageDomainsList(
                any(HSMGetStorageDomainsListVDSCommandParameters.class));

        CanDoActionTestUtils.runAndAssertCanDoActionSuccess(command);
    }

    @Test
    public void testAlreadyExistStorageDomain() {
        when(command.getStorageDomainStaticDAO().get(any(Guid.class))).thenReturn(parameters.getStorageDomain());

        CanDoActionTestUtils.runAndAssertCanDoActionFailure(command,
                VdcBllMessages.ACTION_TYPE_FAILED_STORAGE_DOMAIN_ALREADY_EXIST);
    }

    @Test
    public void testNonExistingStorageDomain() {
        when(command.getStorageDomainStaticDAO().get(any(Guid.class))).thenReturn(null);

        doReturn(Collections.emptyList()).when(command).executeHSMGetStorageDomainsList(
                any(HSMGetStorageDomainsListVDSCommandParameters.class));

        CanDoActionTestUtils.runAndAssertCanDoActionFailure(command,
                VdcBllMessages.ACTION_TYPE_FAILED_STORAGE_DOMAIN_NOT_EXIST);
    }

    @Test
    public void testCannotChangeStorageDomainType() {
        when(command.getStorageDomainStaticDAO().get(any(Guid.class))).thenReturn(null);

        StorageDomainStatic sdStatic = getStorageDomain();
        sdStatic.setStorageDomainType(StorageDomainType.Image);

        doReturn(new Pair<>(sdStatic, sdStatic.getId())).when(command).executeHSMGetStorageDomainInfo(
                any(HSMGetStorageDomainInfoVDSCommandParameters.class));
        doReturn(Collections.singletonList(sdStatic.getId())).when(command).executeHSMGetStorageDomainsList(
                any(HSMGetStorageDomainsListVDSCommandParameters.class));

        CanDoActionTestUtils.runAndAssertCanDoActionFailure(command,
                VdcBllMessages.ACTION_TYPE_FAILED_CANNOT_CHANGE_STORAGE_DOMAIN_TYPE);
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
        storagePool.setcompatibility_version(Version.v3_5);
        return storagePool;
    }

    private static List<VDS> getHosts() {
        List<VDS> hosts = new ArrayList<>();
        VDS host = new VDS();
        host.setId(Guid.newGuid());
        host.setStatus(VDSStatus.Up);
        hosts.add(host);
        return hosts;
    }
}
