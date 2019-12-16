package org.ovirt.engine.core.bll.storage.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
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
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.core.dao.StorageDomainDao;
import org.ovirt.engine.core.dao.StorageDomainStaticDao;
import org.ovirt.engine.core.dao.StoragePoolDao;
import org.ovirt.engine.core.dao.StorageServerConnectionDao;
import org.ovirt.engine.core.dao.VdsDao;
import org.ovirt.engine.core.utils.MockConfigDescriptor;
import org.ovirt.engine.core.utils.MockConfigExtension;
import org.ovirt.engine.core.utils.RandomUtils;

@ExtendWith(MockConfigExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class AddStorageDomainCommonTest extends BaseCommandTest {

    @InjectMocks
    private AddStorageDomainCommon<StorageDomainManagementParameter> cmd =
            new AddStorageDomainCommon<>(new StorageDomainManagementParameter(), null){};

    public static Stream<MockConfigDescriptor<?>> mockConfiguration() {
        return Stream.of(MockConfigDescriptor.of(ConfigValues.StorageDomainNameSizeLimit, 10));
    }

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
    private StoragePool sp;
    private Guid connId;

    @BeforeEach
    public void setUp() {
        Guid vdsId = Guid.newGuid();
        Guid spId = Guid.newGuid();
        connId = Guid.newGuid();

        sd = new StorageDomainStatic();
        sd.setId(Guid.newGuid());
        sd.setStorageType(StorageType.NFS);
        sd.setStorageDomainType(StorageDomainType.Data);
        sd.setStorageName("newStorage");
        sd.setStorageFormat(StorageFormatType.V3);
        sd.setStorage(connId.toString());

        VDS vds = new VDS();
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

        cmd.getParameters().setStorageDomainId(sd.getId());
        cmd.getParameters().setStorageDomain(sd);
        cmd.getParameters().setVdsId(vdsId);
        cmd.setVdsId(vdsId);
        cmd.init();
    }

    @Test
    public void validateSucceeds() {
        ValidateTestUtils.runAndAssertValidateSuccess(cmd);
    }

    @Test
    public void validateSucceedsInitFormatDataDomain() {
        sd.setStorageFormat(null);
        ValidateTestUtils.runAndAssertValidateSuccess(cmd);
        StorageFormatType targetStorageFormatType = StorageFormatType.values()[StorageFormatType.values().length - 1];
        assertEquals(targetStorageFormatType, sd.getStorageFormat(), "Format not initialized correctly");
    }

    @Test
    public void validateSucceedsInitFormatIsoDomain() {
        sd.setStorageFormat(null);
        sd.setStorageDomainType(StorageDomainType.ISO);
        ValidateTestUtils.runAndAssertValidateSuccess(cmd);
        assertEquals(StorageFormatType.V1, sd.getStorageFormat(), "Format not initialized correctly");
    }

    @Test
    public void validateSucceedsInitFormatExportDomain() {
        sd.setStorageFormat(null);
        sd.setStorageDomainType(StorageDomainType.ImportExport);
        ValidateTestUtils.runAndAssertValidateSuccess(cmd);
        assertEquals(StorageFormatType.V1, sd.getStorageFormat(), "Format not initialized correctly");
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

    @Test
    public void validateFailsUnsupportedIsoBackupDomain() {
        sd.setStorageDomainType(StorageDomainType.ISO);
        sd.setBackup(true);
        ValidateTestUtils.runAndAssertValidateFailure(cmd,
                EngineMessage.ACTION_TYPE_FAILED_DOMAIN_TYPE_DOES_NOT_SUPPORT_BACKUP);
    }

    @Test
    public void validateFailsUnsupportedImportExportBackupDomain() {
        sd.setStorageDomainType(StorageDomainType.ImportExport);
        sd.setBackup(true);
        ValidateTestUtils.runAndAssertValidateFailure(cmd,
                EngineMessage.ACTION_TYPE_FAILED_DOMAIN_TYPE_DOES_NOT_SUPPORT_BACKUP);
    }
}
