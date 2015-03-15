package org.ovirt.engine.core.bll;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.ovirt.engine.core.common.action.ExportRepoImageParameters;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatus;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.businessentities.storage.ImageStatus;
import org.ovirt.engine.core.common.errors.VdcBllMessages;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.VmDAO;


import java.util.Arrays;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;


/** A test case for {@link org.ovirt.engine.core.bll.ExportRepoImageCommandTest} */
@RunWith(MockitoJUnitRunner.class)
public class ExportRepoImageCommandTest extends ImportExportRepoImageCommandTest {

    @Mock
    protected VmDAO vmDao;

    protected ExportRepoImageCommand<ExportRepoImageParameters> cmd;

    protected VM vm;

    @Before
    public void setUp() {
        super.setUp();

        vm = new VM();
        vm.setStatus(VMStatus.Down);

        when(vmDao.getVmsListForDisk(getDiskImageId(), Boolean.FALSE)).thenReturn(Arrays.asList(vm));

        ExportRepoImageParameters exportParameters = new ExportRepoImageParameters(
                getDiskImageGroupId(), getRepoStorageDomainId());

        cmd = spy(new ExportRepoImageCommand<>(exportParameters));

        doReturn(vmDao).when(cmd).getVmDAO();
        doReturn(getStorageDomainDao()).when(cmd).getStorageDomainDAO();
        doReturn(getStoragePoolDao()).when(cmd).getStoragePoolDAO();
        doReturn(getDiskDao()).when(cmd).getDiskDao();
        doReturn(getProviderProxy()).when(cmd).getProviderProxy();
    }

    @Test
    public void testCanDoActionSuccess() {
        CanDoActionTestUtils.runAndAssertCanDoActionSuccess(cmd);
    }

    @Test
    public void testCanDoActionImageDoesNotExist() {
        when(getDiskDao().get(getDiskImageGroupId())).thenReturn(null);
        CanDoActionTestUtils.runAndAssertCanDoActionFailure(cmd,
                VdcBllMessages.ACTION_TYPE_FAILED_DISK_NOT_EXIST);
    }

    @Test
    public void testCanDoActionDomainInMaintenance() {
        getDiskStorageDomain().setStatus(StorageDomainStatus.Maintenance);
        CanDoActionTestUtils.runAndAssertCanDoActionFailure(cmd,
                VdcBllMessages.ACTION_TYPE_FAILED_STORAGE_DOMAIN_STATUS_ILLEGAL2);
    }

    @Test
    public void testCanDoActionImageHasParent() {
        getDiskImage().setParentId(Guid.newGuid());
        CanDoActionTestUtils.runAndAssertCanDoActionFailure(cmd,
                VdcBllMessages.ACTION_TYPE_FAILED_DISK_CONFIGURATION_NOT_SUPPORTED);
    }

    @Test
    public void testCanDoActionVmRunning() {
        vm.setStatus(VMStatus.Up);
        CanDoActionTestUtils.runAndAssertCanDoActionFailure(cmd,
                VdcBllMessages.ACTION_TYPE_FAILED_VM_IS_RUNNING);
    }

    @Test
    public void testCanDoActionImageLocked() {
        getDiskImage().setImageStatus(ImageStatus.LOCKED);
        CanDoActionTestUtils.runAndAssertCanDoActionFailure(cmd,
                VdcBllMessages.ACTION_TYPE_FAILED_DISKS_LOCKED);
    }
}
