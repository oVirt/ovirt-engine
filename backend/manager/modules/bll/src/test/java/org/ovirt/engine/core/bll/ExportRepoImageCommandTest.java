package org.ovirt.engine.core.bll;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.ovirt.engine.core.common.action.ExportRepoImageParameters;
import org.ovirt.engine.core.common.businessentities.StoragePoolStatus;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VMStatus;
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

        when(vmDao.getVmsListForDisk(getDiskImageId())).thenReturn(Arrays.asList(vm));

        ExportRepoImageParameters exportParameters = new ExportRepoImageParameters(getDiskImageId());

        exportParameters.setStoragePoolId(getStoragePoolId());
        exportParameters.setStorageDomainId(getStorageDomainId());
        exportParameters.setImageGroupID(getDiskImageGroupId());
        exportParameters.setDestinationDomainId(getRepoStorageDomainId());

        cmd = spy(new ExportRepoImageCommand<>(exportParameters));

        doReturn(vmDao).when(cmd).getVmDAO();
        doReturn(getStorageDomainDao()).when(cmd).getStorageDomainDAO();
        doReturn(getStoragePoolDao()).when(cmd).getStoragePoolDAO();
        doReturn(getDiskImageDao()).when(cmd).getDiskImageDao();
        doReturn(getProviderProxy()).when(cmd).getProviderProxy();
    }

    @Test
    public void testCanDoActionSuccess() {
        CanDoActionTestUtils.runAndAssertCanDoActionSuccess(cmd);
    }

    @Test
    public void testCanDoActionImageDoesNotExist() {
        when(getDiskImageDao().get(getDiskImageId())).thenReturn(null);
        CanDoActionTestUtils.runAndAssertCanDoActionFailure(cmd,
                VdcBllMessages.ACTION_TYPE_FAILED_DISK_NOT_EXIST);
    }

    @Test
    public void testCanDoActionPoolInMaintenance() {
        getStoragePool().setstatus(StoragePoolStatus.Maintenance);
        CanDoActionTestUtils.runAndAssertCanDoActionFailure(cmd,
                VdcBllMessages.ACTION_TYPE_FAILED_IMAGE_REPOSITORY_NOT_FOUND);
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

}
