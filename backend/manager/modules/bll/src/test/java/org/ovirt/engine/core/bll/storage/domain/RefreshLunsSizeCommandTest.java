package org.ovirt.engine.core.bll.storage.domain;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;
import static org.ovirt.engine.core.utils.MockConfigRule.mockConfig;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.mockito.Mock;
import org.ovirt.engine.core.bll.BaseCommandTest;
import org.ovirt.engine.core.bll.ValidateTestUtils;
import org.ovirt.engine.core.common.action.ExtendSANStorageDomainParameters;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatic;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatus;
import org.ovirt.engine.core.common.businessentities.StorageDomainType;
import org.ovirt.engine.core.common.businessentities.StorageFormatType;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.businessentities.StoragePoolStatus;
import org.ovirt.engine.core.common.businessentities.storage.LUNs;
import org.ovirt.engine.core.common.businessentities.storage.StorageType;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.core.dao.LunDao;
import org.ovirt.engine.core.dao.StorageDomainStaticDao;
import org.ovirt.engine.core.utils.MockConfigRule;

public class RefreshLunsSizeCommandTest extends BaseCommandTest {

    private static final String STORAGE = "STORAGE";
    private static final Version SUPPORTED_VERSION = new Version(3, 6);
    private static final Version UNSUPPORTED_VERSION = new Version(3, 0);
    private Guid sdId;
    private StorageDomain sd;
    private StoragePool sp;
    private StorageDomainStatic sdStatic;
    private Guid spId;
    private RefreshLunsSizeCommand<ExtendSANStorageDomainParameters> cmd;
    private List<String> lunIdList;
    private List<LUNs> lunsFromDb;

    @Mock
    private StorageDomainStaticDao sdsDao;

    @Mock
    private LunDao lunsDao;

    @ClassRule
    public static MockConfigRule mcr = new MockConfigRule(
            mockConfig(ConfigValues.RefreshLunSupported, SUPPORTED_VERSION.getValue(), true),
            mockConfig(ConfigValues.RefreshLunSupported, UNSUPPORTED_VERSION.getValue(), false));

    @Before
    public void setUp() {
        sdId = Guid.newGuid();
        sdStatic = createStorageDomain();
        spId = Guid.newGuid();

        sd = new StorageDomain();
        sd.setStorageStaticData(sdStatic);
        sd.setStatus(StorageDomainStatus.Active);
        sd.setStoragePoolId(spId);

        sp = new StoragePool();
        sp.setId(spId);
        sp.setStatus(StoragePoolStatus.Up);
        sp.setIsLocal(false);
        sp.setCompatibilityVersion(Version.v3_6);

        lunIdList = new ArrayList<>();
        lunIdList.add("1");
        lunIdList.add("2");

        cmd = spy(new RefreshLunsSizeCommand<>(new ExtendSANStorageDomainParameters(sdId, (ArrayList)lunIdList)));
        doReturn(sd).when(cmd).getStorageDomain();
        doReturn(sp).when(cmd).getStoragePool();
        doReturn(sdsDao).when(cmd).getStorageDomainStaticDao();

        when(sdsDao.get(sdId)).thenReturn(sdStatic);

        doReturn(lunsDao).when(cmd).getLunDao();

        lunsFromDb = new ArrayList<>();
        LUNs lun1 = new LUNs();
        lun1.setLUNId("1");
        lun1.setStorageDomainId(sdId);
        LUNs lun2 = new LUNs();
        lun2.setLUNId("2");
        lun2.setStorageDomainId(sdId);
        lunsFromDb.add(lun1);
        lunsFromDb.add(lun2);
        when(lunsDao.getAll()).thenReturn(lunsFromDb);
    }

    private StorageDomainStatic createStorageDomain() {
        StorageDomainStatic sd = new StorageDomainStatic();
        sd.setId(sdId);
        sd.setStorageName("newStorageDomain");
        sd.setComment("a storage domain for testing");
        sd.setDescription("a storage domain for testing");
        sd.setStorageDomainType(StorageDomainType.Data);
        sd.setStorageType(StorageType.ISCSI);
        sd.setStorageFormat(StorageFormatType.V3);
        sd.setStorage(STORAGE);
        return sd;
    }

    @Test
    public void validateWrongVersion() {
        sp.setCompatibilityVersion(Version.v3_0);
        ValidateTestUtils.runAndAssertValidateFailure(cmd,
                EngineMessage.ACTION_TYPE_FAILED_REFRESH_LUNS_UNSUPPORTED_ACTION);
        sp.setCompatibilityVersion(Version.v3_6);
    }

    @Test
    public void validateWrongStorage() {
        StorageDomainStatic nfsStatic = createStorageDomain();
        nfsStatic.setStorageType(StorageType.NFS);
        StorageDomain sd = new StorageDomain();
        sd.setStorageStaticData(nfsStatic);
        sd.setStatus(StorageDomainStatus.Active);
        sd.setStoragePoolId(spId);
        doReturn(sd).when(cmd).getStorageDomain();
        ValidateTestUtils.runAndAssertValidateFailure(cmd,
                EngineMessage.ACTION_TYPE_FAILED_STORAGE_DOMAIN_TYPE_ILLEGAL);
    }

    @Test
    public void validateWrongStatus() {
        sd.setStatus(StorageDomainStatus.Maintenance);
        ValidateTestUtils.runAndAssertValidateFailure(cmd,
                EngineMessage.ACTION_TYPE_FAILED_STORAGE_DOMAIN_STATUS_ILLEGAL2);
    }

    @Test
    public void validateStatusLocked() {
        sd.setStatus(StorageDomainStatus.Locked);
        ValidateTestUtils.runAndAssertValidateFailure(cmd,
                EngineMessage.ACTION_TYPE_FAILED_OBJECT_LOCKED);
    }

    @Test
    public void validateNoDomain() {
        doReturn(null).when(cmd).getStorageDomain();
        ValidateTestUtils.runAndAssertValidateFailure(cmd,
                EngineMessage.ACTION_TYPE_FAILED_STORAGE_DOMAIN_NOT_EXIST);
    }

    @Test
    public void setActionMessageParameters() {
        cmd.setActionMessageParameters();
        List<String> messages = cmd.getReturnValue().getValidationMessages();
        assertTrue("action name not in messages", messages.remove(EngineMessage.VAR__ACTION__UPDATE.name()));
        assertTrue("type not in messages", messages.remove(EngineMessage.VAR__TYPE__STORAGE__DOMAIN.name()));
        assertTrue("redundant messages " + messages, messages.isEmpty());
    }

    @Test
    public void validateLunsNotPartOfStorageDomain() {
        List lunsFromDb = new ArrayList<>();
        LUNs lun1 = new LUNs();
        lun1.setLUNId("111");
        lun1.setStorageDomainId(sdId);
        LUNs lun2 = new LUNs();
        lun2.setLUNId("222");
        lun2.setStorageDomainId(sdId);
        lunsFromDb.add(lun1);
        lunsFromDb.add(lun2);
        when(lunsDao.getAllForVolumeGroup(STORAGE)).thenReturn(lunsFromDb);

        ValidateTestUtils.runAndAssertValidateFailure(cmd,
                EngineMessage.ACTION_TYPE_FAILED_LUNS_NOT_PART_OF_STORAGE_DOMAIN);
    }

    @Test
    public void validate() {
        List lunsFromDb = new ArrayList<>();
        LUNs lun1 = new LUNs();
        lun1.setLUNId("1");
        lun1.setStorageDomainId(sdId);
        LUNs lun2 = new LUNs();
        lun2.setLUNId("2");
        lun2.setStorageDomainId(sdId);
        lunsFromDb.add(lun1);
        lunsFromDb.add(lun2);
        when(lunsDao.getAllForVolumeGroup(STORAGE)).thenReturn(lunsFromDb);
        ValidateTestUtils.runAndAssertValidateSuccess(cmd);
    }
}
