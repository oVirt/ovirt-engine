package org.ovirt.engine.core.bll;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import org.ovirt.engine.core.common.action.ImportRepoImageParameters;
import org.ovirt.engine.core.common.businessentities.StoragePoolStatus;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.errors.VdcBllMessages;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;


/** A test case for {@link org.ovirt.engine.core.bll.ImportRepoImageCommandTest} */
@RunWith(MockitoJUnitRunner.class)
public class ImportRepoImageCommandTest extends ImportExportRepoImageCommandTest {

    protected ImportRepoImageCommand<ImportRepoImageParameters> cmd;

    @Before
    public void setUp() {
        super.setUp();

        ImportRepoImageParameters importParameters = new ImportRepoImageParameters();

        importParameters.setSourceRepoImageId(getRepoImageId());
        importParameters.setSourceStorageDomainId(getRepoStorageDomainId());
        importParameters.setStoragePoolId(getStoragePoolId());
        importParameters.setStorageDomainId(getStorageDomainId());

        cmd = spy(new ImportRepoImageCommand<>(importParameters));

        doReturn(getStorageDomainDao()).when(cmd).getStorageDomainDAO();
        doReturn(getStoragePoolDao()).when(cmd).getStoragePoolDAO();
        doReturn(getProviderProxy()).when(cmd).getProviderProxy();
        doReturn(true).when(cmd).validateSpaceRequirements(any(DiskImage.class));
    }

    @Test
    public void testCanDoActionSuccess() {
        CanDoActionTestUtils.runAndAssertCanDoActionSuccess(cmd);
    }

    @Test
    public void testCanDoActionImageDoesNotExist() {
        when(getProviderProxy().getImageAsDiskImage(getRepoImageId())).thenReturn(null);
        CanDoActionTestUtils.runAndAssertCanDoActionFailure(cmd,
                VdcBllMessages.ACTION_TYPE_FAILED_DISK_NOT_EXIST);
    }

    @Test
    public void testCanDoActionPoolInMaintenance() {
        getStoragePool().setStatus(StoragePoolStatus.Maintenance);
        CanDoActionTestUtils.runAndAssertCanDoActionFailure(cmd,
                VdcBllMessages.ACTION_TYPE_FAILED_IMAGE_REPOSITORY_NOT_FOUND);
    }
}
