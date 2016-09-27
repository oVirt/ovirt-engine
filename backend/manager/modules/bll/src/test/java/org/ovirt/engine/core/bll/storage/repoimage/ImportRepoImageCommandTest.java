package org.ovirt.engine.core.bll.storage.repoimage;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import org.ovirt.engine.core.bll.ImportExportRepoImageCommandTest;
import org.ovirt.engine.core.bll.ValidateTestUtils;
import org.ovirt.engine.core.common.action.ImportRepoImageParameters;
import org.ovirt.engine.core.common.businessentities.StoragePoolStatus;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.errors.EngineMessage;


/** A test case for {@link ImportRepoImageCommand} */
@RunWith(MockitoJUnitRunner.class)
public class ImportRepoImageCommandTest extends ImportExportRepoImageCommandTest {

    protected ImportRepoImageCommand<ImportRepoImageParameters> cmd;

    @Override
    @Before
    public void setUp() {
        super.setUp();

        ImportRepoImageParameters importParameters = new ImportRepoImageParameters();

        importParameters.setSourceRepoImageId(getRepoImageId());
        importParameters.setSourceStorageDomainId(getRepoStorageDomainId());
        importParameters.setStoragePoolId(getStoragePoolId());
        importParameters.setStorageDomainId(getStorageDomainId());

        cmd = spy(new ImportRepoImageCommand<>(importParameters, null));

        doReturn(getStorageDomainDao()).when(cmd).getStorageDomainDao();
        doReturn(getStoragePoolDao()).when(cmd).getStoragePoolDao();
        doReturn(getProviderProxy()).when(cmd).getProviderProxy();
        doReturn(true).when(cmd).validateSpaceRequirements(any(DiskImage.class));
    }

    @Test
    public void testValidateSuccess() {
        ValidateTestUtils.runAndAssertValidateSuccess(cmd);
    }

    @Test
    public void testValidateImageDoesNotExist() {
        when(getProviderProxy().getImageAsDiskImage(getRepoImageId())).thenReturn(null);
        ValidateTestUtils.runAndAssertValidateFailure(cmd,
                EngineMessage.ACTION_TYPE_FAILED_DISK_NOT_EXIST);
    }

    @Test
    public void testValidatePoolInMaintenance() {
        getStoragePool().setStatus(StoragePoolStatus.Maintenance);
        ValidateTestUtils.runAndAssertValidateFailure(cmd,
                EngineMessage.ACTION_TYPE_FAILED_IMAGE_REPOSITORY_NOT_FOUND);
    }
}
