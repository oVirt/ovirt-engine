package org.ovirt.engine.core.bll.storage.repoimage;

import static org.mockito.Mockito.when;

import java.util.Collections;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.ovirt.engine.core.bll.ImportExportRepoImageCommandTest;
import org.ovirt.engine.core.bll.ValidateTestUtils;
import org.ovirt.engine.core.common.action.ExportRepoImageParameters;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatus;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.businessentities.storage.ImageStatus;
import org.ovirt.engine.core.common.businessentities.storage.LunDisk;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.VmDao;


/** A test case for {@link ExportRepoImageCommand} */
@RunWith(MockitoJUnitRunner.class)
public class ExportRepoImageCommandTest extends ImportExportRepoImageCommandTest {

    @Mock
    protected VmDao vmDao;

    @InjectMocks
    protected ExportRepoImageCommand<ExportRepoImageParameters> cmd =
            new ExportRepoImageCommand<>(
                    new ExportRepoImageParameters(getDiskImageGroupId(), getRepoStorageDomainId()), null);

    protected VM vm;

    @Override
    @Before
    public void setUp() {
        super.setUp();

        vm = new VM();
        vm.setStatus(VMStatus.Down);

        when(vmDao.getVmsListForDisk(getDiskImageId(), Boolean.FALSE)).thenReturn(Collections.singletonList(vm));
    }

    @Test
    public void testValidateSuccess() {
        ValidateTestUtils.runAndAssertValidateSuccess(cmd);
    }

    @Test
    public void testValidateLunDisk() {
        when(getDiskDao().get(getDiskImageGroupId())).thenReturn(new LunDisk());
        ValidateTestUtils.runAndAssertValidateFailure(cmd,
                EngineMessage.ACTION_TYPE_FAILED_NOT_SUPPORTED_DISK_STORAGE_TYPE);
    }

    @Test
    public void testValidateImageDoesNotExist() {
        when(getDiskDao().get(getDiskImageGroupId())).thenReturn(null);
        ValidateTestUtils.runAndAssertValidateFailure(cmd,
                EngineMessage.ACTION_TYPE_FAILED_DISK_NOT_EXIST);
    }

    @Test
    public void testValidateDomainInMaintenance() {
        getDiskStorageDomain().setStatus(StorageDomainStatus.Maintenance);
        ValidateTestUtils.runAndAssertValidateFailure(cmd,
                EngineMessage.ACTION_TYPE_FAILED_STORAGE_DOMAIN_STATUS_ILLEGAL2);
    }

    @Test
    public void testValidateImageHasParent() {
        getDiskImage().setParentId(Guid.newGuid());
        ValidateTestUtils.runAndAssertValidateFailure(cmd,
                EngineMessage.ACTION_TYPE_FAILED_DISK_CONFIGURATION_NOT_SUPPORTED);
    }

    @Test
    public void testValidateVmRunning() {
        vm.setStatus(VMStatus.Up);
        ValidateTestUtils.runAndAssertValidateFailure(cmd,
                EngineMessage.ACTION_TYPE_FAILED_VM_IS_RUNNING);
    }

    @Test
    public void testValidateImageLocked() {
        getDiskImage().setImageStatus(ImageStatus.LOCKED);
        ValidateTestUtils.runAndAssertValidateFailure(cmd,
                EngineMessage.ACTION_TYPE_FAILED_DISKS_LOCKED);
    }
}
