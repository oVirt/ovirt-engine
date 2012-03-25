package org.ovirt.engine.core.bll;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mockStatic;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.queries.GetAllDisksByVmIdParameters;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dao.VmDAO;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ DbFacade.class })
public class GetNextAvailableDiskAliasNameByVMIdQueryTest extends AbstractQueryTest<GetAllDisksByVmIdParameters, GetNextAvailableDiskAliasNameByVMIdQuery<GetAllDisksByVmIdParameters>> {
    @Mock
    private DbFacade db;

    @Mock
    private VmDAO vmDAO;

    private VM vm;
    private final Guid vmId = new Guid();
    private final String VM_NAME = "VmTESTNaME";


    @Test
    public void testExecuteQuery() {
        mockDAOForQuery();
        vm = mockVm();
        Mockito.when(vmDAO.getById(vmId)).thenReturn(vm);
        String diskAliasName = VM_NAME + "_DISK1";

        // Execute query.
        getQuery().executeQueryCommand();
        assertEquals(diskAliasName, getQuery().getQueryReturnValue().getReturnValue().toString());
    }

    @Test
    public void testExecuteQueryWithInValidVmId() {
        mockDAOForQuery();
        vm = mockVm();

        // Execute query.
        getQuery().executeQueryCommand();
        assertEquals(null, getQuery().getQueryReturnValue().getReturnValue());
    }

    @Test
    public void testExecuteQueryWithOneImage() {
        mockDAOForQuery();
        vm = mockVm();
        Map<String, DiskImage> diskMap = vm.getDiskMap();
        DiskImage diskImage = new DiskImage();
        diskImage.setinternal_drive_mapping("1");
        diskImage.setId(Guid.NewGuid());
        diskMap.put("1", diskImage);
        Mockito.when(vmDAO.getById(vmId)).thenReturn(vm);
        String diskAliasName = VM_NAME + "_DISK2";

        // Execute query.
        getQuery().executeQueryCommand();
        assertEquals(diskAliasName, getQuery().getQueryReturnValue().getReturnValue());
    }

    @Test
    public void testExecuteQueryWithMissisngImage() {
        mockDAOForQuery();
        vm = mockVm();
        Map<String, DiskImage> diskMap = vm.getDiskMap();
        DiskImage diskImage = new DiskImage();
        diskImage.setinternal_drive_mapping("1");
        diskImage.setId(Guid.NewGuid());
        diskMap.put("1", diskImage);
        DiskImage secondDiskImage = new DiskImage();
        diskImage.setinternal_drive_mapping("4");
        diskImage.setId(Guid.NewGuid());
        diskMap.put("4", secondDiskImage);
        Mockito.when(vmDAO.getById(vmId)).thenReturn(vm);
        String diskAliasName = VM_NAME + "_DISK2";

        // Execute query.
        getQuery().executeQueryCommand();
        assertEquals(diskAliasName, getQuery().getQueryReturnValue().getReturnValue());
    }

    /**
     * Initialize DAO to be used in query.
     */
    private void mockDAOForQuery() {
        MockitoAnnotations.initMocks(this);
        mockStatic(DbFacade.class);
        when(DbFacade.getInstance()).thenReturn(db);
        when(db.getVmDAO()).thenReturn(vmDAO);
        when(getQueryParameters().getVmId()).thenReturn(vmId);
    }

    private VM mockVm() {
        vm = new VM();
        vm.setId(vmId);
        vm.setvm_name(VM_NAME);
        vm.setDiskMap(new HashMap<String, DiskImage>());
        return vm;
    }
}
