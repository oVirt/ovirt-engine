package org.ovirt.engine.core.bll.storage.disk;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.ovirt.engine.core.bll.AbstractUserQueryTest;
import org.ovirt.engine.core.bll.storage.disk.image.ImagesHandler;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.storage.Disk;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.VmDao;

@MockitoSettings(strictness = Strictness.LENIENT)
public class GetNextAvailableDiskAliasNameByVMIdQueryTest extends AbstractUserQueryTest<IdQueryParameters, GetNextAvailableDiskAliasNameByVMIdQuery<IdQueryParameters>> {
    @Mock
    private VmDao vmDao;

    private VM vm;
    private static final Guid vmId = Guid.newGuid();
    private static final String VM_NAME = "VmTESTNaME";

    @Override
    protected void initQuery(GetNextAvailableDiskAliasNameByVMIdQuery<IdQueryParameters> query) {
        super.initQuery(query);
        doNothing().when(query).updateDisksFromDb(any());
    }

    @Test
    public void testExecuteQueryVmWithNoDisks() {
        mockParameters();
        vm = mockVmAndReturnFromDao();
        String diskAliasName = ImagesHandler.getDefaultDiskAlias(vm.getName(), "1");

        // Execute query.
        getQuery().executeQueryCommand();
        assertEquals(diskAliasName, getQuery().getQueryReturnValue().getReturnValue().toString());
    }

    @Test
    public void testExecuteQueryWithInValidVmIdOrMissingPermissions() {
        mockParameters();
        vm = mockVm();

        // Execute query.
        getQuery().executeQueryCommand();
        assertNull(getQuery().getQueryReturnValue().getReturnValue());
    }

    @Test
    public void testExecuteQueryVmWithMultipleDisks() {
        mockParameters();
        vm = mockVmAndReturnFromDao();
        populateVmDiskMap(vm, 5);
        String expectedDiskAlias = ImagesHandler.getDefaultDiskAlias(vm.getName(), "6");

        getQuery().executeQueryCommand();
        assertEquals(expectedDiskAlias, getQuery().getQueryReturnValue().getReturnValue());
    }

    /**
     * When removing the first disk from VM with n disks the default disk alias should be recycled and take the
     * disk alias of the disk we removed
     */
    @Test
    public void testExecuteQueryRecycling() {
        mockParameters();
        vm = mockVmAndReturnFromDao();
        populateVmDiskMap(vm, 5);

        String expectedDiskAlias = ImagesHandler.getDefaultDiskAlias(vm.getName(), "3");

        Optional<Guid> removedDiskId =
                vm.getDiskMap().values().stream()
                        .filter(d -> d.getDiskAlias().equals(expectedDiskAlias)).map(Disk::getId).findFirst();

        removedDiskId.ifPresent(id -> vm.getDiskMap().remove(id));

        getQuery().executeQueryCommand();
        assertEquals(expectedDiskAlias, getQuery().getQueryReturnValue().getReturnValue());
    }

    /**
     * Populates the VM disk map with the amount of disks specified, each with a default disk alias
     */
    private static void populateVmDiskMap(VM vm, int numOfDisks) {
        Map<Guid, Disk> diskMap = vm.getDiskMap();

        for (Integer i = 0; i < numOfDisks; i++) {
            DiskImage diskImage = new DiskImage();
            diskImage.setId(Guid.newGuid());
            diskImage.setDiskAlias(ImagesHandler.getDefaultDiskAlias(vm.getName(), Integer.toString(i + 1)));
            diskMap.put(diskImage.getId(), diskImage);
        }
    }

    private void mockParameters() {
        when(getQueryParameters().getId()).thenReturn(vmId);
    }

    private VM mockVm() {
        vm = new VM();
        vm.setId(vmId);
        vm.setName(VM_NAME);
        vm.setDiskMap(new HashMap<>());
        return vm;
    }

    private VM mockVmAndReturnFromDao() {
        vm = mockVm();
        when(vmDao.get(vmId, getUser().getId(), getQueryParameters().isFiltered())).thenReturn(vm);
        return vm;
    }
}
