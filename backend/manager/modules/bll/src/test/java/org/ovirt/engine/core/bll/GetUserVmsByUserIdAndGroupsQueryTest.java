package org.ovirt.engine.core.bll;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.queries.GetUserVmsByUserIdAndGroupsParameters;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.VmDao;

@MockitoSettings(strictness = Strictness.LENIENT)
public class GetUserVmsByUserIdAndGroupsQueryTest
        extends AbstractUserQueryTest<GetUserVmsByUserIdAndGroupsParameters, GetUserVmsByUserIdAndGroupsQuery<GetUserVmsByUserIdAndGroupsParameters>> {

    @Mock
    private VmDao vmDaoMock;

    /** Tests that executing a query with the same user works when requesting disks */
    @Test
    public void testQueryWithSameUserAndDisks() {
        assertExecuteQueryCommandResult(getUser().getId(), true, true);
    }

    /** Tests that executing a query with the same user works when not requesting disks */
    @Test
    public void testQueryWithSameUserNoDisks() {
        assertExecuteQueryCommandResult(getUser().getId(), false, true);
    }

    @SuppressWarnings("rawtypes")
    public void assertExecuteQueryCommandResult(Guid requestedUser, boolean includeDiskData, boolean expectedResults) {
        mockQueryParameters(requestedUser, includeDiskData);

        // Mock the result of the Dao
        final VM expectedVM = mockVMFromDao(requestedUser);
        final DiskImage expectedDisk = mockDisk();

        final ArrayList<DiskImage> snapshots = mockSnapshots();
        DiskImage expectedSnapshot = snapshots.get(0);

        // Mock the disks, if needed
        if (includeDiskData) {
            doAnswer(invocation -> {
                expectedDisk.getSnapshots().addAll(snapshots);
                expectedVM.getDiskMap().put(expectedDisk.getId(), expectedDisk);
                expectedVM.getDiskList().add(expectedDisk);
                return null;
            }).when(getQuery()).updateDisksFromDB(expectedVM);

            doNothing().when(getQuery()).fillImagesBySnapshots(expectedVM);
        }

        doNothing().when(getQuery()).updateVmInit(expectedVM);
        doNothing().when(getQuery()).updateVmGuestAgentVersion(expectedVM);

        getQuery().executeQueryCommand();

        @SuppressWarnings("unchecked")
        List<VM> actualVMs = getQuery().getQueryReturnValue().getReturnValue();
        if (!expectedResults) {
            assertTrue(actualVMs.isEmpty(), "no VMs should have been returned");
        } else {
            assertEquals(1, actualVMs.size(), "wrong number of VMs returned");
            VM actualVM = actualVMs.get(0);
            assertEquals(expectedVM, actualVM, "wrong VMs returned");

            if (includeDiskData) {
                assertEquals(1, actualVM.getDiskList().size(), "Wrong number of disks on VM");
                DiskImage actualDisk = actualVM.getDiskList().get(0);
                assertEquals(expectedDisk, actualDisk, "Wrong disk on VM");

                assertEquals(1, actualDisk.getSnapshots().size(), "Wrong number of snapshots");
                DiskImage actualSnapshot = actualDisk.getSnapshots().get(0);
                assertEquals(expectedSnapshot, actualSnapshot, "Wrong snapshot");
            }
        }
    }

    /**
     * Adds additional parameters to the parameters object
     * @param requestedUser The user to get the VMs for
     * @param includeDiskData Whether or not to include disk data
     */
    private void mockQueryParameters(Guid requestedUser, boolean includeDiskData) {
        when(getQuery().getUserID()).thenReturn(requestedUser);
        when(getQueryParameters().getIncludeDiskData()).thenReturn(includeDiskData);
    }

    /**
     * Mocks the Daos to return a VM
     * @param requestedUser The user on the parameter object to return the VM for
     * @return The VM the mocked Dao will return
     */
    private VM mockVMFromDao(Guid requestedUser) {
        VM expectedVM = new VM();
        when(vmDaoMock.getAllForUserWithGroupsAndUserRoles(requestedUser)).thenReturn(Collections.singletonList(expectedVM));

        return expectedVM;
    }

    /** @return A disk to add to the VM */
    private static DiskImage mockDisk() {
        // Prepare the disk
        Guid diskGuid = Guid.newGuid();
        Guid itGuid = Guid.newGuid();
        final DiskImage expectedDisk = new DiskImage();
        expectedDisk.setImageId(diskGuid);
        expectedDisk.setImageTemplateId(itGuid);

        return expectedDisk;
    }

    /** @return The snapshots to add to the disk */
    private static ArrayList<DiskImage> mockSnapshots() {
        Guid snapshotGuid = Guid.newGuid();
        DiskImage expectedSnapshot = new DiskImage();
        expectedSnapshot.setImageId(snapshotGuid);
        ArrayList<DiskImage> snapshots = new ArrayList<>(1);
        snapshots.add(expectedSnapshot);

        return snapshots;
    }
}
