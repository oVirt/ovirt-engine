package org.ovirt.engine.core.bll.storage.repoimage;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.ovirt.engine.core.bll.ValidateTestUtils;
import org.ovirt.engine.core.bll.ValidationResult;
import org.ovirt.engine.core.bll.provider.storage.OpenStackImageProviderProxy;
import org.ovirt.engine.core.bll.validator.storage.DiskImagesValidator;
import org.ovirt.engine.core.common.action.ImportRepoImageParameters;
import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.businessentities.StoragePoolStatus;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.ClusterDao;
import org.ovirt.engine.core.dao.StoragePoolDao;

/** A test case for {@link ImportRepoImageCommand} */
@MockitoSettings(strictness = Strictness.LENIENT)
public class ImportRepoImageCommandTest extends ImportExportRepoImageCommandTest {
    private String repoImageId = Guid.newGuid().toString();

    private Guid clusterId = Guid.newGuid();

    @Mock
    private Cluster cluster;

    @Mock
    private StoragePoolDao storagePoolDao;

    @Mock
    private OpenStackImageProviderProxy providerProxy;

    @Mock
    protected DiskImagesValidator diskImagesValidator;

    @Mock
    private ClusterDao clusterDao;

    @Spy
    @InjectMocks
    protected ImportRepoImageCommand<ImportRepoImageParameters> cmd =
            new ImportRepoImageCommand<>(createParameters(), null);

    private ImportRepoImageParameters createParameters() {
        ImportRepoImageParameters p = new ImportRepoImageParameters();
        p.setSourceRepoImageId(repoImageId);
        p.setSourceStorageDomainId(repoStorageDomainId);
        p.setStoragePoolId(storagePoolId);
        p.setStorageDomainId(storageDomainId);
        p.setClusterId(clusterId);
        return p;
    }

    @Override
    @BeforeEach
    public void setUp() {
        super.setUp();

        when(storagePoolDao.get(storagePoolId)).thenReturn(storagePool);
        when(providerProxy.getImageAsDiskImage(repoImageId)).thenReturn(diskImage);
        when(clusterDao.get(clusterId)).thenReturn(cluster);
        when(cluster.getStoragePoolId()).thenReturn(storagePoolId);

        doReturn(true).when(cmd).validateSpaceRequirements(any());
        doReturn(diskImagesValidator).when(cmd).createDiskImagesValidator(any());
    }

    @Test
    public void testValidateSuccess() {
        ValidateTestUtils.runAndAssertValidateSuccess(cmd);
    }

    @Test
    public void testValidateImageDoesNotExist() {
        when(providerProxy.getImageAsDiskImage(repoImageId)).thenReturn(null);
        ValidateTestUtils.runAndAssertValidateFailure(cmd,
                EngineMessage.ACTION_TYPE_FAILED_DISK_NOT_EXIST);
    }

    @Test
    public void testValidateImageQcowVersionNotMatchingDcVersion() {
        doReturn(new ValidationResult(EngineMessage.ACTION_TYPE_FAILED_QCOW_COMPAT_DOES_NOT_MATCH_DC_VERSION)).when(diskImagesValidator)
                .isQcowVersionSupportedForDcVersion();
        ValidateTestUtils.runAndAssertValidateFailure(cmd,
                EngineMessage.ACTION_TYPE_FAILED_QCOW_COMPAT_DOES_NOT_MATCH_DC_VERSION);
    }

    @Test
    public void testValidatePoolInMaintenance() {
        storagePool.setStatus(StoragePoolStatus.Maintenance);
        ValidateTestUtils.runAndAssertValidateFailure(cmd,
                EngineMessage.ACTION_TYPE_FAILED_STORAGE_POOL_STATUS_ILLEGAL);
    }

    @Test
    public void testValidateStorageDomainNotInDataCenter() {
        Guid newStoragePoolId = Guid.newGuid();
        when(cmd.getStoragePoolId()).thenReturn(newStoragePoolId);
        when(storagePoolDao.get(newStoragePoolId)).thenReturn(storagePool);
        ValidateTestUtils.runAndAssertValidateFailure(cmd,
                EngineMessage.ACTION_TYPE_FAILED_STORAGE_DOMAIN_BELONGS_TO_DIFFERENT_STORAGE_POOL);
    }

    @Test
    public void testValidateStorageDomainClusterMismatch() {
        Guid newStoragePoolId = Guid.newGuid();
        when(cluster.getStoragePoolId()).thenReturn(newStoragePoolId);
        ValidateTestUtils.runAndAssertValidateFailure(cmd,
                EngineMessage.ACTION_TYPE_FAILED_STORAGE_DOMAIN_AND_CLUSTER_IN_DIFFERENT_POOL);
    }
}
