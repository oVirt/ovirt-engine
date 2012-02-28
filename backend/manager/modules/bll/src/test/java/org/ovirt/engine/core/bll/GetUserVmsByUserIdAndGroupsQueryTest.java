package org.ovirt.engine.core.bll;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.doAnswer;
import static org.powermock.api.mockito.PowerMockito.doNothing;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.verifyStatic;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.queries.GetUserVmsByUserIdAndGroupsParameters;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.VmDAO;
import org.ovirt.engine.core.utils.RandomUtils;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ VmHandler.class, ImagesHandler.class })
public class GetUserVmsByUserIdAndGroupsQueryTest
        extends AbstractUserQueryTest<GetUserVmsByUserIdAndGroupsParameters, GetUserVmsByUserIdAndGroupsQuery<GetUserVmsByUserIdAndGroupsParameters>> {

    /** Tests that executing a query with the same user works when requesting disks */
    @Test
    public void testQueryWithSameUserAndDisks() {
        assertExecuteQueryCommandResult(getUser().getUserId(), true, true);
    }

    /** Tests that executing a query with the same user works when not requesting disks */
    @Test
    public void testQueryWithSameUserNoDisks() {
        assertExecuteQueryCommandResult(getUser().getUserId(), false, true);
    }

    /** Tests that executing a query with a different user returns an empty list when requesting disks */
    @Test
    public void testQueryWithOtherUserWithDisks() {
        assertExecuteQueryCommandResult(Guid.NewGuid(), true, false);
    }

    /** Tests that executing a query with a different user returns an empty list when not requesting disks */
    @Test
    public void testQueryWithOtherUserNoDisks() {
        assertExecuteQueryCommandResult(Guid.NewGuid(), false, false);
    }

    /** Tests that executing a query with a different user works when the query is run in admin mode and disks are requested */
    @Test
    public void testAdminQueryWithOtherUserWithDisks() {
        when(getQueryParameters().isFiltered()).thenReturn(false);
        assertExecuteQueryCommandResult(Guid.NewGuid(), true, true);
    }

    /** Tests that executing a query with a different user works when the query is run in admin mode and disks aren't requested */
    @Test
    public void testAdminQueryWithOtherUserNoDisks() {
        when(getQueryParameters().isFiltered()).thenReturn(false);
        assertExecuteQueryCommandResult(Guid.NewGuid(), false, true);
    }

    @SuppressWarnings("rawtypes")
    public void assertExecuteQueryCommandResult(Guid requestedUser, boolean includeDiskData, boolean expectedResults) {
        mockQueryParameters(requestedUser, includeDiskData);

        // Mock the result of the DAO
        final VM expectedVM = mockVMFromDAO(requestedUser);

        final DiskImage expectedDisk = mockDisk();
        Guid diskGuid = expectedDisk.getId();
        Guid itGuid = expectedDisk.getit_guid();

        ArrayList<DiskImage> snapshots = mockSnapshots();
        DiskImage expectedSnapshot = snapshots.get(0);

        // Mock the disks, if needed
        if (includeDiskData) {
            doAnswer(new Answer() {
                @Override
                public Object answer(InvocationOnMock invocation) throws Throwable {
                    expectedVM.addDriveToImageMap(RandomUtils.instance().nextString(10), expectedDisk);
                    return null;
                }
            }).when(VmHandler.class);
            VmHandler.updateDisksFromDb(expectedVM);

            mockStatic(ImagesHandler.class);
            when(ImagesHandler.getAllImageSnapshots(diskGuid, itGuid)).thenReturn(snapshots);
        }

        getQuery().executeQueryCommand();
        verifyStatic();

        @SuppressWarnings("unchecked")
        List<VM> actualVMs = (List<VM>) getQuery().getQueryReturnValue().getReturnValue();
        if (!expectedResults) {
            assertTrue("no VMs should have been returned", actualVMs.isEmpty());
        } else {
            assertEquals("wrong number of VMs returned", 1, actualVMs.size());
            VM actualVM = actualVMs.get(0);
            assertEquals("wrong VMs returned", expectedVM, actualVM);

            if (includeDiskData) {
                assertEquals("Wrong number of disks on VM", 1, actualVM.getDiskList().size());
                DiskImage actualDisk = actualVM.getDiskList().get(0);
                assertEquals("Wrong disk on VM", expectedDisk, actualDisk);

                assertEquals("Wrong number of snapshots", 1, actualDisk.getSnapshots().size());
                DiskImage actualSnapshot = actualDisk.getSnapshots().get(0);
                assertEquals("Wrong snapshot", expectedSnapshot, actualSnapshot);
            }
        }
    }

    /**
     * Adds additional parameters to the parameters object
     * @param requestedUser The user to get the VMs for
     * @param includeDiskData Whether or not to include disk data
     */
    private void mockQueryParameters(Guid requestedUser, boolean includeDiskData) {
        when(getQueryParameters().getUserId()).thenReturn(requestedUser);
        when(getQueryParameters().getIncludeDiskData()).thenReturn(includeDiskData);
    }

    /**
     * Mocks the DAOs to return a VM
     * @param requestedUser The user on the parameter object to return the VM for
     * @return The VM the mocked DAO will return
     */
    private VM mockVMFromDAO(Guid requestedUser) {
        VM expectedVM = new VM();
        VmDAO vmDaoMock = mock(VmDAO.class);
        when(vmDaoMock.getAllForUserWithGroupsAndUserRoles(requestedUser)).thenReturn(Collections.singletonList(expectedVM));
        when(getDbFacadeMockInstance().getVmDAO()).thenReturn(vmDaoMock);

        // Mock the VmHandler
        mockStatic(VmHandler.class);
        doNothing().when(VmHandler.class);
        VmHandler.UpdateVmGuestAgentVersion(expectedVM);

        return expectedVM;
    }

    /** @return A disk to add to the VM */
    private static DiskImage mockDisk() {
        // Prepare the disks
        Guid diskGuid = Guid.NewGuid();
        Guid itGuid = Guid.NewGuid();
        final DiskImage expectedDisk = new DiskImage();
        expectedDisk.setId(diskGuid);
        expectedDisk.setit_guid(itGuid);

        return expectedDisk;
    }

    /** @return The snapshots to add to the disk */
    private static ArrayList<DiskImage> mockSnapshots() {
        Guid snapshotGuid = Guid.NewGuid();
        DiskImage expectedSnapshot = new DiskImage();
        expectedSnapshot.setId(snapshotGuid);
        ArrayList<DiskImage> snapshots = new ArrayList<DiskImage>(1);
        snapshots.add(expectedSnapshot);

        return snapshots;
    }
}
