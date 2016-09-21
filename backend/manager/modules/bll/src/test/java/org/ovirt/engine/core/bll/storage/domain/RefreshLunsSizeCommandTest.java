package org.ovirt.engine.core.bll.storage.domain;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
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
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.core.dao.LunDao;
import org.ovirt.engine.core.dao.StorageDomainStaticDao;

public class RefreshLunsSizeCommandTest extends BaseCommandTest {

    private static final String STORAGE = "STORAGE";
    private Guid sdId = Guid.newGuid();
    private StorageDomain sd;
    private Guid spId;

    @Spy
    @InjectMocks
    private RefreshLunsSizeCommand<ExtendSANStorageDomainParameters> cmd =
            new RefreshLunsSizeCommand<>(
                    new ExtendSANStorageDomainParameters(sdId, new ArrayList<>(Arrays.asList("1", "2"))), null);

    @Mock
    private StorageDomainStaticDao sdsDao;

    @Mock
    private LunDao lunsDao;

    @Before
    public void setUp() {
        StorageDomainStatic sdStatic = createStorageDomain();
        spId = Guid.newGuid();

        sd = new StorageDomain();
        sd.setStorageStaticData(sdStatic);
        sd.setStatus(StorageDomainStatus.Active);
        sd.setStoragePoolId(spId);

        StoragePool sp = new StoragePool();
        sp.setId(spId);
        sp.setStatus(StoragePoolStatus.Up);
        sp.setIsLocal(false);
        sp.setCompatibilityVersion(Version.v3_6);

        doReturn(sd).when(cmd).getStorageDomain();
        doReturn(sp).when(cmd).getStoragePool();
        doReturn(sdsDao).when(cmd).getStorageDomainStaticDao();

        when(sdsDao.get(sdId)).thenReturn(sdStatic);

        doReturn(lunsDao).when(cmd).getLunDao();

        LUNs lun1 = new LUNs();
        lun1.setLUNId("1");
        lun1.setStorageDomainId(sdId);
        LUNs lun2 = new LUNs();
        lun2.setLUNId("2");
        lun2.setStorageDomainId(sdId);
        when(lunsDao.getAll()).thenReturn(Arrays.asList(lun1, lun2));
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
        LUNs lun1 = new LUNs();
        lun1.setLUNId("111");
        lun1.setStorageDomainId(sdId);
        LUNs lun2 = new LUNs();
        lun2.setLUNId("222");
        lun2.setStorageDomainId(sdId);
        when(lunsDao.getAllForVolumeGroup(STORAGE)).thenReturn(Arrays.asList(lun1, lun2));

        ValidateTestUtils.runAndAssertValidateFailure(cmd,
                EngineMessage.ACTION_TYPE_FAILED_LUNS_NOT_PART_OF_STORAGE_DOMAIN);
    }

    @Test
    public void validate() {
        LUNs lun1 = new LUNs();
        lun1.setLUNId("1");
        lun1.setStorageDomainId(sdId);
        LUNs lun2 = new LUNs();
        lun2.setLUNId("2");
        lun2.setStorageDomainId(sdId);
        when(lunsDao.getAllForVolumeGroup(STORAGE)).thenReturn(Arrays.asList(lun1, lun2));
        ValidateTestUtils.runAndAssertValidateSuccess(cmd);
    }
}
