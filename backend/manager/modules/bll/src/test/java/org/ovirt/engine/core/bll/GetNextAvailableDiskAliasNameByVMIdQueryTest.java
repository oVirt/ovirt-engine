package org.ovirt.engine.core.bll;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.ovirt.engine.core.common.businessentities.Disk;
import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.queries.GetAllDisksByVmIdParameters;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.VmDAO;

@RunWith(MockitoJUnitRunner.class)
public class GetNextAvailableDiskAliasNameByVMIdQueryTest extends AbstractQueryTest<GetAllDisksByVmIdParameters, GetNextAvailableDiskAliasNameByVMIdQuery<GetAllDisksByVmIdParameters>> {
    @Mock
    private VmDAO vmDAO;

    private VM vm;
    private final Guid vmId = new Guid();
    private final String VM_NAME = "VmTESTNaME";

    @Override
    protected void setUpSpyQuery() throws Exception {
        super.setUpSpyQuery();
        doNothing().when(getQuery()).updateDisksFromDb(any(VM.class));
    }

    @Test
    public void testExecuteQuery() throws Exception {
        mockDAOForQuery();
        vm = mockVmAndReturnFromDAO();
        String diskAliasName = VM_NAME + "_Disk1";

        // Execute query.
        getQuery().executeQueryCommand();
        assertEquals(diskAliasName, getQuery().getQueryReturnValue().getReturnValue().toString());
    }

    @Test
    public void testExecuteQueryWithInValidVmId() throws Exception {
        mockDAOForQuery();
        vm = mockVm();

        // Execute query.
        getQuery().executeQueryCommand();
        assertEquals(null, getQuery().getQueryReturnValue().getReturnValue());
    }

    @Test
    public void testExecuteQueryWithOneImage() throws Exception {
        mockDAOForQuery();
        vm = mockVmAndReturnFromDAO();
        Map<String, Disk> diskMap = vm.getDiskMap();
        DiskImage diskImage = new DiskImage();
        diskImage.setInternalDriveMapping(1);
        diskImage.setImageId(Guid.NewGuid());
        diskMap.put("1", diskImage);
        String diskAliasName = VM_NAME + "_Disk2";

        // Execute query.
        getQuery().executeQueryCommand();
        assertEquals(diskAliasName, getQuery().getQueryReturnValue().getReturnValue());
    }

    @Test
    public void testExecuteQueryWithMissisngImage() throws Exception {
        mockDAOForQuery();
        vm = mockVmAndReturnFromDAO();
        Map<String, Disk> diskMap = vm.getDiskMap();
        DiskImage diskImage = new DiskImage();
        diskImage.setInternalDriveMapping(1);
        diskImage.setImageId(Guid.NewGuid());
        diskMap.put("1", diskImage);
        DiskImage secondDiskImage = new DiskImage();
        diskImage.setInternalDriveMapping(4);
        diskImage.setImageId(Guid.NewGuid());
        diskMap.put("4", secondDiskImage);
        String diskAliasName = VM_NAME + "_Disk2";

        // Execute query.
        getQuery().executeQueryCommand();
        assertEquals(diskAliasName, getQuery().getQueryReturnValue().getReturnValue());
    }

    /**
     * Initialize DAO to be used in query.
     *
     * @throws Exception
     */
    private void mockDAOForQuery() throws Exception {
        when(getDbFacadeMockInstance().getVmDAO()).thenReturn(vmDAO);
        when(getQueryParameters().getVmId()).thenReturn(vmId);
    }

    private VM mockVm() {
        vm = new VM();
        vm.setId(vmId);
        vm.setvm_name(VM_NAME);
        vm.setDiskMap(new HashMap<String, Disk>());
        return vm;
    }

    private VM mockVmAndReturnFromDAO() {
        vm = mockVm();
        when(vmDAO.get(vmId)).thenReturn(vm);
        return vm;
    }
}
