package org.ovirt.engine.core.bll.storage.domain;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

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
import org.ovirt.engine.core.common.action.ExtendSANStorageDomainParameters;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatic;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatus;
import org.ovirt.engine.core.common.businessentities.StorageDomainType;
import org.ovirt.engine.core.common.businessentities.StorageFormatType;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.businessentities.StoragePoolStatus;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.storage.LUNs;
import org.ovirt.engine.core.common.businessentities.storage.StorageType;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.core.dao.LunDao;
import org.ovirt.engine.core.dao.StorageDomainStaticDao;
import org.ovirt.engine.core.utils.RandomUtils;
import org.ovirt.engine.core.utils.RandomUtilsSeedingExtension;

@ExtendWith(RandomUtilsSeedingExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class RefreshLunsSizeCommandTest extends BaseCommandTest {

    private static final String STORAGE = "STORAGE";
    private Guid sdId = Guid.newGuid();
    private StorageDomain sd;
    private Guid spId;

    @Spy
    @InjectMocks
    private RefreshLunsSizeCommand<ExtendSANStorageDomainParameters> cmd =
            new RefreshLunsSizeCommand<>(
                    new ExtendSANStorageDomainParameters(sdId, new HashSet<>(Arrays.asList("1", "2"))), null);

    @Mock
    private StorageDomainStaticDao sdsDao;

    @Mock
    private LunDao lunsDao;

    @BeforeEach
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
        sp.setCompatibilityVersion(Version.v4_3);

        doReturn(sd).when(cmd).getStorageDomain();
        doReturn(sp).when(cmd).getStoragePool();

        when(sdsDao.get(sdId)).thenReturn(sdStatic);

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
        assertTrue(messages.remove(EngineMessage.VAR__ACTION__UPDATE.name()), "action name not in messages");
        assertTrue(messages.remove(EngineMessage.VAR__TYPE__STORAGE__DOMAIN.name()), "type not in messages");
        assertTrue(messages.isEmpty(), "redundant messages " + messages);
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

    @Test
    public void validateLunSizeSimilarOnAllHostsSucceeds() {
        assertTrue(cmd.getFailedLuns(createLunMap(true)).isEmpty());
    }

    @Test
    public void validateLunSizeDifferentOnOneHostFails() {
        assertFalse(cmd.getFailedLuns(createLunMap(false)).isEmpty());
    }

    private Map<String, List<Pair<VDS, LUNs>>> createLunMap(boolean sameLunSizesPerHost) {
        RandomUtils rnd = RandomUtils.instance();
        String lunId = rnd.nextString(34);
        int lunSize = rnd.nextInt();

        List<Pair<VDS, LUNs>> lunList = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            LUNs lun = new LUNs();
            if (!sameLunSizesPerHost) {
                lunSize++;
            }
            lun.setDeviceSize(lunSize);
            lunList.add(new Pair<>(new VDS(), lun));
        }
        return Collections.singletonMap(lunId, lunList);
    }
}
