package org.ovirt.engine.core.bll.storage.disk;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.ovirt.engine.core.common.businessentities.storage.CinderDisk;
import org.ovirt.engine.core.common.businessentities.storage.Disk;
import org.ovirt.engine.core.common.businessentities.storage.DiskContentType;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.businessentities.storage.LunDisk;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.DiskDao;

/**
 * A test case for {@link GetDiskAndSnapshotsByDiskIdQuery}.
 * This test mocks away all the Daos, and just tests the flow of the query itself.
 */
public class GetDiskAndSnapshotsByDiskIdQueryTest extends
        AbstractGetDisksAndSnapshotsQueryTest<IdQueryParameters, GetDiskAndSnapshotsByDiskIdQuery<IdQueryParameters>> {

    /**
     * Map to hold the disks by the disk id
     */
    private Map<Guid, Disk> disksMap;

    @Mock
    private DiskDao diskDao;

    @Before
    @Override
    public void setUp() throws Exception {
        super.setUp();
        disksMap = new HashMap<>();
        disksMap.put(diskWithSnapshots.getId(), diskWithSnapshots);
        disksMap.put(diskWithoutSnapshots.getId(), diskWithoutSnapshots);
        disksMap.put(ovfImage.getId(), ovfImage);
        disksMap.put(cinderDisk.getId(), cinderDisk);
        disksMap.put(lunDisk.getId(), lunDisk);
        setUpDaoMocks();
    }

    @Override
    protected void setUpImagesHandlerMocks() {
        List<DiskImage> diskWithSnapshotsParam = new ArrayList<>(snapshotsList);
        diskWithSnapshotsParam.add(diskWithSnapshots);
        when(imagesHandler.aggregateDiskImagesSnapshots(diskWithSnapshotsParam)).thenReturn(manualAggregateSnapshots());

        Stream.of(diskWithoutSnapshots, ovfImage, cinderDisk).forEach(disk ->
                when(imagesHandler.aggregateDiskImagesSnapshots(Collections.singletonList(disk))).thenReturn(
                        Collections.singletonList(disk))
        );
    }

    private void setUpDaoMocks() {
        when(diskDao.getAllFromDisksIncludingSnapshotsByDiskId(diskWithSnapshots.getId(),
                getUser().getId(),
                getQueryParameters().isFiltered())).thenReturn(getDiskWithSnapshotsList());

        disksMap.keySet().stream().filter(diskId -> !diskId.equals(diskWithSnapshots.getId())).forEach(diskId ->
                when(diskDao.getAllFromDisksIncludingSnapshotsByDiskId(diskId,
                        getUser().getId(),
                        getQueryParameters().isFiltered())).thenReturn(Collections.singletonList(disksMap.get(diskId)))
        );
    }

    private List<Disk> getDiskWithSnapshotsList() {
        List<Disk> disks = new ArrayList<>(snapshotsList);
        disks.add(diskWithSnapshots);
        return disks;
    }

    @Override
    protected List<DiskImage> manualAggregateSnapshots() {
        diskWithSnapshots.getSnapshots().addAll(snapshotsList);
        return Collections.singletonList(diskWithSnapshots);
    }

    @Test
    public void testQueryWithDiskWithSnapshots() {
        DiskImage disk = executeQuery(diskWithSnapshots);
        assertEquals("wrong number of snapshots", 3, disk.getSnapshots().size());
        assertTrue("disk should be active", disk.getActive());
    }

    @Test
    public void testQueryWithDiskWithoutSnapshots() {
        DiskImage disk = executeQuery(diskWithoutSnapshots);
        assertEquals("disk should not have any snapshots", 0, disk.getSnapshots().size());
    }

    @Test
    public void testQueryWithOvfDisk() {
        DiskImage disk = executeQuery(ovfImage);
        assertEquals("disk should not have any snapshots", 0, disk.getSnapshots().size());
        assertEquals("disk should be OVF_STORE", DiskContentType.OVF_STORE, disk.getContentType());
    }

    @Test
    public void testQueryWithCinderDisk() {
        DiskImage disk = executeQuery(cinderDisk);
        assertTrue("disk should be from type CinderDisk", disk instanceof CinderDisk);
    }

    @Test
    public void testQueryWithLunDisk() {
        Disk disk = executeQuery(lunDisk);
        assertTrue("disk should be from type LunDisk", disk instanceof LunDisk);
    }

    private <T extends Disk> T executeQuery(Disk disk) {
        when(params.getId()).thenReturn(disk.getId());
        getQuery().executeQueryCommand();
        return getQuery().getQueryReturnValue().getReturnValue();
    }

}
