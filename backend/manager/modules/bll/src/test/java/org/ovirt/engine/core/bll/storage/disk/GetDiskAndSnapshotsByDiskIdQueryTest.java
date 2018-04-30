package org.ovirt.engine.core.bll.storage.disk;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
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
@MockitoSettings(strictness = Strictness.LENIENT)
public class GetDiskAndSnapshotsByDiskIdQueryTest extends
        AbstractGetDisksAndSnapshotsQueryTest<IdQueryParameters, GetDiskAndSnapshotsByDiskIdQuery<IdQueryParameters>> {

    /**
     * Map to hold the disks by the disk id
     */
    private Map<Guid, Disk> disksMap;

    @Mock
    private DiskDao diskDao;

    @BeforeEach
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
        assertEquals(3, disk.getSnapshots().size(), "wrong number of snapshots");
        assertTrue(disk.getActive(), "disk should be active");
    }

    @Test
    public void testQueryWithDiskWithoutSnapshots() {
        DiskImage disk = executeQuery(diskWithoutSnapshots);
        assertEquals(0, disk.getSnapshots().size(), "disk should not have any snapshots");
    }

    @Test
    public void testQueryWithOvfDisk() {
        DiskImage disk = executeQuery(ovfImage);
        assertEquals(0, disk.getSnapshots().size(), "disk should not have any snapshots");
        assertEquals(DiskContentType.OVF_STORE, disk.getContentType(), "disk should be OVF_STORE");
    }

    @Test
    public void testQueryWithCinderDisk() {
        DiskImage disk = executeQuery(cinderDisk);
        assertTrue(disk instanceof CinderDisk, "disk should be from type CinderDisk");
    }

    @Test
    public void testQueryWithLunDisk() {
        Disk disk = executeQuery(lunDisk);
        assertTrue(disk instanceof LunDisk, "disk should be from type LunDisk");
    }

    private <T extends Disk> T executeQuery(Disk disk) {
        when(params.getId()).thenReturn(disk.getId());
        getQuery().executeQueryCommand();
        return getQuery().getQueryReturnValue().getReturnValue();
    }

}
