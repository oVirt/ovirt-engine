package org.ovirt.engine.core.bll.storage.disk;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.ovirt.engine.core.common.businessentities.storage.Disk;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.businessentities.storage.LunDisk;
import org.ovirt.engine.core.common.queries.QueryParametersBase;

/**
 * A test case for {@link GetAllDisksWithSnapshotsQuery}.
 * This test mocks away all the Daos, and just tests the flow of the query itself.
 */
public class GetAllDisksWithSnapshotsQueryTest
        extends AbstractGetDisksAndSnapshotsQueryTest<QueryParametersBase, GetAllDisksWithSnapshotsQuery<QueryParametersBase>> {

    /**
     * Test GetAllDisksWithSnapshotsQuery#aggregateDisksSnapshots method logic.
     * Given Images from various types, some of them are snapshots,
     * Verify that the snapshot disks aggregated under their active image and all the active disks return in a list.
     */
    @Test
    public void testAggregateDisksSnapshots() {
        List<Disk> disks = new ArrayList<>(snapshotsList);
        Collections.addAll(disks, diskWithSnapshots, diskWithoutSnapshots, ovfImage, cinderDisk, lunDisk);
        Collection<Disk> result = getQuery().aggregateDisksSnapshots(disks);

        // Assert each return disk is active
        assertAllDisksAreActive(result);
        // Verify the number of returned disks
        assertEquals(5, result.size(), "wrong number of disks returned");

    }

    private void assertAllDisksAreActive(Collection<Disk> disks) {
        for (Disk disk : disks) {
            if (disk instanceof LunDisk) {
                continue;
            }
            DiskImage diskImage = (DiskImage) disk;
            assertTrue(diskImage.getActive(), "disk should be active");
        }
    }
}
