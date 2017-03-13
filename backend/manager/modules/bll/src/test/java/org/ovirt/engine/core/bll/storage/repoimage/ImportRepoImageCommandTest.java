package org.ovirt.engine.core.bll.storage.repoimage;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner.Silent;
import org.ovirt.engine.core.bll.ImportExportRepoImageCommandTest;
import org.ovirt.engine.core.bll.ValidateTestUtils;
import org.ovirt.engine.core.common.action.ImportRepoImageParameters;
import org.ovirt.engine.core.common.businessentities.StoragePoolStatus;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.errors.EngineMessage;


/** A test case for {@link ImportRepoImageCommand} */
@RunWith(Silent.class)
public class ImportRepoImageCommandTest extends ImportExportRepoImageCommandTest {

    @Spy
    @InjectMocks
    protected ImportRepoImageCommand<ImportRepoImageParameters> cmd =
            new ImportRepoImageCommand<>(new ImportRepoImageParameters(), null);

    @Override
    @Before
    public void setUp() {
        super.setUp();

        cmd.getParameters().setSourceRepoImageId(repoImageId);
        cmd.getParameters().setSourceStorageDomainId(repoStorageDomainId);
        cmd.getParameters().setStoragePoolId(storagePoolId);
        cmd.getParameters().setStorageDomainId(storageDomainId);

        doReturn(true).when(cmd).validateSpaceRequirements(any(DiskImage.class));
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
    public void testValidatePoolInMaintenance() {
        storagePool.setStatus(StoragePoolStatus.Maintenance);
        ValidateTestUtils.runAndAssertValidateFailure(cmd,
                EngineMessage.ACTION_TYPE_FAILED_IMAGE_REPOSITORY_NOT_FOUND);
    }
}
