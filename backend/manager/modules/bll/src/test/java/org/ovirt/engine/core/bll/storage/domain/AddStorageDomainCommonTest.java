package org.ovirt.engine.core.bll.storage.domain;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;
import static org.ovirt.engine.core.common.utils.MockConfigRule.mockConfig;

import java.util.Collections;

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.mockito.Mock;
import org.ovirt.engine.core.bll.BaseCommandTest;
import org.ovirt.engine.core.bll.ValidateTestUtils;
import org.ovirt.engine.core.common.action.StorageDomainManagementParameter;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatic;
import org.ovirt.engine.core.common.businessentities.StorageDomainType;
import org.ovirt.engine.core.common.businessentities.StorageFormatType;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.businessentities.StorageServerConnections;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.core.common.businessentities.storage.StorageType;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.utils.MockConfigRule;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.core.dao.StorageDomainDao;
import org.ovirt.engine.core.dao.StorageDomainStaticDao;
import org.ovirt.engine.core.dao.StoragePoolDao;
import org.ovirt.engine.core.dao.StorageServerConnectionDao;
import org.ovirt.engine.core.dao.VdsDao;
import org.ovirt.engine.core.utils.RandomUtils;

public class AddStorageDomainCommonTest extends BaseCommandTest {

    private AddStorageDomainCommon<StorageDomainManagementParameter> cmd;

    @ClassRule
    public static MockConfigRule mcr = new MockConfigRule(
            mockConfig(ConfigValues.StorageDomainNameSizeLimit, 10)
    );

    @Mock
    private VdsDao vdsDao;
    @Mock
    private StorageDomainDao sdDao;
    @Mock
    private StorageDomainStaticDao sdsDao;
    @Mock
    private StoragePoolDao spDao;
    @Mock
    private StorageServerConnectionDao sscDao;

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
        sp.setCompatibilityVersion(Version.getLast());
        when(spDao.get(spId)).thenReturn(sp);

        StorageServerConnections conn = new StorageServerConnections();
        conn.setId(connId.toString());
        conn.setStorageType(StorageType.NFS);

        when(sscDao.get(connId.toString())).thenReturn(conn);

        params = new StorageDomainManagementParameter(sd);
        params.setVdsId(vdsId);

        cmd = spy(new AddStorageDomainCommon<>(params, null));
        doReturn(vdsDao).when(cmd).getVdsDao();
        doReturn(sdDao).when(cmd).getStorageDomainDao();
        doReturn(sdsDao).when(cmd).getStorageDomainStaticDao();
        doReturn(spDao).when(cmd).getStoragePoolDao();
        doReturn(sscDao).when(cmd).getStorageServerConnectionDao();
    }

    @Test
    public void validateSucceeds() {
        ValidateTestUtils.runAndAssertValidateSuccess(cmd);
    }

    @Test
    public void validateSucceedsInitFormatDataDomain() {
        sd.setStorageFormat(null);
        ValidateTestUtils.runAndAssertValidateSuccess(cmd);
        assertEquals("Format not initialized correctly", StorageFormatType.V3, sd.getStorageFormat());
    }

    @Test
    public void validateSucceedsInitFormatIsoDomain() {
        sd.setStorageFormat(null);
        sd.setStorageDomainType(StorageDomainType.ISO);
        ValidateTestUtils.runAndAssertValidateSuccess(cmd);
        assertEquals("Format not initialized correctly", StorageFormatType.V1, sd.getStorageFormat());
    }

    @Test
    public void validateSucceedsInitFormatExportDomain() {
        sd.setStorageFormat(null);
        sd.setStorageDomainType(StorageDomainType.ImportExport);
        ValidateTestUtils.runAndAssertValidateSuccess(cmd);
        assertEquals("Format not initialized correctly", StorageFormatType.V1, sd.getStorageFormat());
    }

    @Test
    public void validateFailsNameExists() {
        when(sdsDao.getByName(sd.getName())).thenReturn(new StorageDomainStatic());
        ValidateTestUtils.runAndAssertValidateFailure
                (cmd, EngineMessage.ACTION_TYPE_FAILED_STORAGE_DOMAIN_NAME_ALREADY_EXIST);
    }

    @Test
    public void validateFailsLongName() {
        sd.setStorageName(RandomUtils.instance().nextString(11));
        ValidateTestUtils.runAndAssertValidateFailure
                (cmd, EngineMessage.ACTION_TYPE_FAILED_NAME_LENGTH_IS_TOO_LONG);
    }

    @Test
    public void validateSucceedsPoolNotSpecified() {
        sd.setStorageFormat(null);
        vds.setStoragePoolId(null);
        ValidateTestUtils.runAndAssertValidateSuccess(cmd);
    }

    @Test
    public void validateFailsPoolSpecifiedDoesNotExist() {
        params.setStoragePoolId(spId);
        when(spDao.get(spId)).thenReturn(null);
        ValidateTestUtils.runAndAssertValidateFailure
                (cmd, EngineMessage.ACTION_TYPE_FAILED_STORAGE_POOL_NOT_EXIST);
    }


    @Test
    public void validateFailsBlockIso() {
        sd.setStorageDomainType(StorageDomainType.ISO);
        sd.setStorageType(StorageType.FCP);
        ValidateTestUtils.runAndAssertValidateFailure
                (cmd, EngineMessage.ACTION_TYPE_FAILED_DOMAIN_TYPE_CAN_BE_CREATED_ONLY_ON_SPECIFIC_STORAGE_DOMAINS);
    }

    @Test
    public void validateSucceedsExportOnLocal() {
        sd.setStorageDomainType(StorageDomainType.ImportExport);
        sd.setStorageType(StorageType.LOCALFS);
        sd.setStorageFormat(StorageFormatType.V1);
        sp.setIsLocal(true);
        ValidateTestUtils.runAndAssertValidateSuccess(cmd);
    }

    @Test
    public void validateFailsExportOnBlock() {
        sd.setStorageDomainType(StorageDomainType.ImportExport);
        sd.setStorageType(StorageType.ISCSI);
        ValidateTestUtils.runAndAssertValidateFailure
                (cmd, EngineMessage.ACTION_TYPE_FAILED_DOMAIN_TYPE_CAN_BE_CREATED_ONLY_ON_SPECIFIC_STORAGE_DOMAINS);
    }

    @Test
    public void validateFailsMaster() {
        sd.setStorageDomainType(StorageDomainType.Master);
        ValidateTestUtils.runAndAssertValidateFailure
                (cmd, EngineMessage.ACTION_TYPE_FAILED_STORAGE_DOMAIN_TYPE_ILLEGAL);
    }

    @Test
    public void validateFailsUnsupportedBlockOnlyFormat() {
        sd.setStorageFormat(StorageFormatType.V2);
        ValidateTestUtils.runAndAssertValidateFailure
                (cmd, EngineMessage.ACTION_TYPE_FAILED_STORAGE_DOMAIN_FORMAT_ILLEGAL_HOST);
    }

    @Test
    public void validateFailsUnsupportedIsoFormat() {
        sd.setStorageDomainType(StorageDomainType.ISO);
        ValidateTestUtils.runAndAssertValidateFailure
                (cmd, EngineMessage.ACTION_TYPE_FAILED_STORAGE_DOMAIN_FORMAT_ILLEGAL_HOST);
    }

    @Test
    public void validateFailsUnsupportedExportFormat() {
        sd.setStorageDomainType(StorageDomainType.ImportExport);
        ValidateTestUtils.runAndAssertValidateFailure
                (cmd, EngineMessage.ACTION_TYPE_FAILED_STORAGE_DOMAIN_FORMAT_ILLEGAL_HOST);
    }

    @Test
    public void validateFailsNoConnection() {
        when(sscDao.get(connId.toString())).thenReturn(null);
        ValidateTestUtils.runAndAssertValidateFailure
                (cmd, EngineMessage.ACTION_TYPE_FAILED_STORAGE_CONNECTION_NOT_EXIST);
    }

    @Test
    public void validateFailsConnectionAlreadyUsed() {
        when(sdDao.getAllByConnectionId(connId)).thenReturn(Collections.singletonList(new StorageDomain()));
        ValidateTestUtils.runAndAssertValidateFailure
                (cmd, EngineMessage.ACTION_TYPE_FAILED_STORAGE_CONNECTION_BELONGS_TO_SEVERAL_STORAGE_DOMAINS);
    }
}
