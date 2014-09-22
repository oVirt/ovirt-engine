package org.ovirt.engine.core.bll.storage;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;
import static org.ovirt.engine.core.utils.MockConfigRule.mockConfig;

import java.util.Collections;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.ovirt.engine.core.bll.CanDoActionTestUtils;
import org.ovirt.engine.core.common.action.StorageDomainManagementParameter;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatic;
import org.ovirt.engine.core.common.businessentities.StorageDomainType;
import org.ovirt.engine.core.common.businessentities.StorageFormatType;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.businessentities.StorageServerConnections;
import org.ovirt.engine.core.common.businessentities.StorageType;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.errors.VdcBllMessages;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.core.dao.StorageDomainDAO;
import org.ovirt.engine.core.dao.StorageDomainStaticDAO;
import org.ovirt.engine.core.dao.StoragePoolDAO;
import org.ovirt.engine.core.dao.StorageServerConnectionDAO;
import org.ovirt.engine.core.dao.VdsDAO;
import org.ovirt.engine.core.utils.MockConfigRule;
import org.ovirt.engine.core.utils.RandomUtils;

@RunWith(MockitoJUnitRunner.class)
public class AddStorageDomainCommonTest {

    private AddStorageDomainCommon<StorageDomainManagementParameter> cmd;

    @ClassRule
    public static MockConfigRule mcr = new MockConfigRule(
            mockConfig(ConfigValues.StorageDomainNameSizeLimit, 10),
            mockConfig(ConfigValues.SupportedStorageFormats, Version.v3_5.toString(), "0,2,3"),
            mockConfig(ConfigValues.SupportedStorageFormats, Version.v3_0.toString(), "0")
            );

    @Mock
    private VdsDAO vdsDao;
    @Mock
    private StorageDomainDAO sdDao;
    @Mock
    private StorageDomainStaticDAO sdsDao;
    @Mock
    private StoragePoolDAO spDao;
    @Mock
    private StorageServerConnectionDAO sscDao;

    private StorageDomainStatic sd;
    private VDS vds;
    private Guid spId;
    private StoragePool sp;
    private Guid connId;
    private StorageDomainManagementParameter params;

    @Before
    public void setUp() {
        Guid vdsId = Guid.newGuid();
        spId = Guid.newGuid();
        connId = Guid.newGuid();

        sd = new StorageDomainStatic();
        sd.setId(Guid.newGuid());
        sd.setStorageType(StorageType.NFS);
        sd.setStorageDomainType(StorageDomainType.Data);
        sd.setStorageName("newStorage");
        sd.setStorageFormat(StorageFormatType.V3);
        sd.setStorage(connId.toString());

        vds = new VDS();
        vds.setId(vdsId);
        vds.setStatus(VDSStatus.Up);
        vds.setStoragePoolId(spId);
        when(vdsDao.get(vdsId)).thenReturn(vds);

        sp = new StoragePool();
        sp.setId(spId);
        sp.setcompatibility_version(Version.v3_5);
        when(spDao.get(spId)).thenReturn(sp);

        StorageServerConnections conn = new StorageServerConnections();
        conn.setid(connId.toString());
        conn.setstorage_type(StorageType.NFS);

        when(sscDao.get(connId.toString())).thenReturn(conn);

        params = new StorageDomainManagementParameter(sd);
        params.setVdsId(vdsId);

        cmd = spy(new AddStorageDomainCommon<>(params));
        doReturn(vdsDao).when(cmd).getVdsDAO();
        doReturn(sdDao).when(cmd).getStorageDomainDAO();
        doReturn(sdsDao).when(cmd).getStorageDomainStaticDAO();
        doReturn(spDao).when(cmd).getStoragePoolDAO();
        doReturn(sscDao).when(cmd).getStorageServerConnectionDAO();
    }

    @Test
    public void canDoActionSucceeds() {
        CanDoActionTestUtils.runAndAssertCanDoActionSuccess(cmd);
    }

    @Test
    public void canDoActionSucceedsInitFormatDataDomain() {
        sd.setStorageFormat(null);
        CanDoActionTestUtils.runAndAssertCanDoActionSuccess(cmd);
        assertEquals("Format not initialized correctly", StorageFormatType.V3, sd.getStorageFormat());
    }

    @Test
    public void canDoActionSucceedsInitFormatDataDomain30() {
        sd.setStorageFormat(null);
        sp.setcompatibility_version(Version.v3_0);
        CanDoActionTestUtils.runAndAssertCanDoActionSuccess(cmd);
        assertEquals("Format not initialized correctly", StorageFormatType.V1, sd.getStorageFormat());
    }

    @Test
    public void canDoActionSucceedsInitFormatIsoDomain() {
        sd.setStorageFormat(null);
        sd.setStorageDomainType(StorageDomainType.ISO);
        CanDoActionTestUtils.runAndAssertCanDoActionSuccess(cmd);
        assertEquals("Format not initialized correctly", StorageFormatType.V1, sd.getStorageFormat());
    }

    @Test
    public void canDoActionSucceedsInitFormatExportDomain() {
        sd.setStorageFormat(null);
        sd.setStorageDomainType(StorageDomainType.ImportExport);
        CanDoActionTestUtils.runAndAssertCanDoActionSuccess(cmd);
        assertEquals("Format not initialized correctly", StorageFormatType.V1, sd.getStorageFormat());
    }

    @Test
    public void canDoActionFailsNameExists() {
        when(sdsDao.getByName(sd.getName())).thenReturn(new StorageDomainStatic());
        CanDoActionTestUtils.runAndAssertCanDoActionFailure
                (cmd, VdcBllMessages.ACTION_TYPE_FAILED_STORAGE_DOMAIN_NAME_ALREADY_EXIST);
    }

    @Test
    public void canDoActionFailsLongName() {
        sd.setStorageName(RandomUtils.instance().nextString(11));
        CanDoActionTestUtils.runAndAssertCanDoActionFailure
                (cmd, VdcBllMessages.ACTION_TYPE_FAILED_NAME_LENGTH_IS_TOO_LONG);
    }

    @Test
    public void canDoActionSucceedsPoolNotSpecified() {
        sd.setStorageFormat(null);
        vds.setStoragePoolId(null);
        CanDoActionTestUtils.runAndAssertCanDoActionSuccess(cmd);
    }

    @Test
    public void canDoActionFailsPoolSpecifiedDoesNotExist() {
        params.setStoragePoolId(spId);
        when(spDao.get(spId)).thenReturn(null);
        CanDoActionTestUtils.runAndAssertCanDoActionFailure
                (cmd, VdcBllMessages.ACTION_TYPE_FAILED_STORAGE_POOL_NOT_EXIST);
    }


    @Test
    public void canDoActionFailsBlockIso() {
        sd.setStorageDomainType(StorageDomainType.ISO);
        sd.setStorageType(StorageType.FCP);
        CanDoActionTestUtils.runAndAssertCanDoActionFailure
                (cmd, VdcBllMessages.ACTION_TYPE_FAILED_STORAGE_DOMAIN_TYPE_ILLEGAL);
    }

    @Test
    public void canDoActionFailsBlockLocal() {
        sd.setStorageDomainType(StorageDomainType.ImportExport);
        sd.setStorageType(StorageType.LOCALFS);
        CanDoActionTestUtils.runAndAssertCanDoActionFailure
                (cmd, VdcBllMessages.ACTION_TYPE_FAILED_STORAGE_DOMAIN_TYPE_ILLEGAL);
    }

    @Test
    public void canDoActionFailsMaster() {
        sd.setStorageDomainType(StorageDomainType.Master);
        CanDoActionTestUtils.runAndAssertCanDoActionFailure
                (cmd, VdcBllMessages.ACTION_TYPE_FAILED_STORAGE_DOMAIN_TYPE_ILLEGAL);
    }

    @Test
    public void canDoActionFailsUnsupportedFormat() {
        sp.setcompatibility_version(Version.v3_0);
        CanDoActionTestUtils.runAndAssertCanDoActionFailure
                (cmd, VdcBllMessages.ACTION_TYPE_FAILED_STORAGE_DOMAIN_FORMAT_ILLEGAL_HOST);
    }

    @Test
    public void canDoActionFailsUnsupportedBlockOnlyFormat() {
        sd.setStorageFormat(StorageFormatType.V2);
        CanDoActionTestUtils.runAndAssertCanDoActionFailure
                (cmd, VdcBllMessages.ACTION_TYPE_FAILED_STORAGE_DOMAIN_FORMAT_ILLEGAL_HOST);
    }

    @Test
    public void canDoActionFailsUnsupportedIsoFormat() {
        sd.setStorageDomainType(StorageDomainType.ISO);
        CanDoActionTestUtils.runAndAssertCanDoActionFailure
                (cmd, VdcBllMessages.ACTION_TYPE_FAILED_STORAGE_DOMAIN_FORMAT_ILLEGAL_HOST);
    }

    @Test
    public void canDoActionFailsUnsupportedExportFormat() {
        sd.setStorageDomainType(StorageDomainType.ImportExport);
        CanDoActionTestUtils.runAndAssertCanDoActionFailure
                (cmd, VdcBllMessages.ACTION_TYPE_FAILED_STORAGE_DOMAIN_FORMAT_ILLEGAL_HOST);
    }

    @Test
    public void canDoActionFailsNoConnection() {
        when(sscDao.get(connId.toString())).thenReturn(null);
        CanDoActionTestUtils.runAndAssertCanDoActionFailure
                (cmd, VdcBllMessages.ACTION_TYPE_FAILED_STORAGE_CONNECTION_NOT_EXIST);
    }

    @Test
    public void canDoActionFailsConnectionAlreadyUsed() {
        when(sdDao.getAllByConnectionId(connId)).thenReturn(Collections.singletonList(new StorageDomain()));
        CanDoActionTestUtils.runAndAssertCanDoActionFailure
                (cmd, VdcBllMessages.ACTION_TYPE_FAILED_STORAGE_CONNECTION_BELONGS_TO_SEVERAL_STORAGE_DOMAINS);
    }
}
