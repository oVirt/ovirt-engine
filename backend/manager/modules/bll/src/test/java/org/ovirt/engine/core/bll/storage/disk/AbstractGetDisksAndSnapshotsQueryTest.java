package org.ovirt.engine.core.bll.storage.disk;

import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mock;
import org.ovirt.engine.core.bll.AbstractUserQueryTest;
import org.ovirt.engine.core.bll.QueriesCommandBase;
import org.ovirt.engine.core.bll.storage.disk.image.ImagesHandler;
import org.ovirt.engine.core.common.businessentities.storage.CinderDisk;
import org.ovirt.engine.core.common.businessentities.storage.DiskContentType;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.businessentities.storage.LunDisk;
import org.ovirt.engine.core.common.queries.QueryParametersBase;
import org.ovirt.engine.core.compat.Guid;

public abstract class AbstractGetDisksAndSnapshotsQueryTest<P extends QueryParametersBase, Q extends QueriesCommandBase<P>>
        extends AbstractUserQueryTest<P, Q> {
        private static final int NUM_OF_SNAPSHOTS_TO_CREATE = 3;

        /**
         * Active disk with snapshots for the test
         */
        protected DiskImage diskWithSnapshots;

        /**
         * List of all the snapshots for {@link AbstractGetDisksAndSnapshotsQueryTest#diskWithSnapshots}
         */
        protected List<DiskImage> snapshotsList;

        /**
         * Active disk without snapshots for the test
         */
        protected DiskImage diskWithoutSnapshots;

        /**
         * OVF image for the test
         */
        protected DiskImage ovfImage;

        /**
         * Cinder disk for the test
         */
        protected CinderDisk cinderDisk;

        /**
         * LUN disk for the test
         */
        protected LunDisk lunDisk;

        @Mock
        protected ImagesHandler imagesHandler;

        @BeforeEach
        @Override
        public void setUp() throws Exception {
            super.setUp();
            diskWithSnapshots = createDiskImage(null);
            snapshotsList = createSnapshotsForDiskImage(diskWithSnapshots.getId());
            diskWithoutSnapshots = createDiskImage(null);
            ovfImage = createDiskImage(null);
            ovfImage.setContentType(DiskContentType.OVF_STORE);
            cinderDisk = createCinderDisk();
            lunDisk = createLunDisk();
            setUpImagesHandlerMocks();
        }

    protected void setUpImagesHandlerMocks() {
        List<DiskImage> diskImagesTestParam = new ArrayList<>(snapshotsList);
        Collections.addAll(diskImagesTestParam, diskWithSnapshots, diskWithoutSnapshots, ovfImage, cinderDisk);
        when(imagesHandler.aggregateDiskImagesSnapshots(diskImagesTestParam)).thenReturn(manualAggregateSnapshots());
    }

    protected List<DiskImage> manualAggregateSnapshots() {
        diskWithSnapshots.getSnapshots().addAll(snapshotsList);
        return Arrays.asList(diskWithSnapshots, diskWithoutSnapshots, ovfImage, cinderDisk);
    }

    private DiskImage createDiskImage(Guid diskId) {
        DiskImage di = new DiskImage();
        if (diskId != null) {
            di.setActive(false);
            di.setId(diskId);
        } else {
            di.setId(Guid.newGuid());
            di.setActive(true);
        }
        return di;
    }

    private List<DiskImage> createSnapshotsForDiskImage(Guid diskId) {
        return IntStream.range(0, NUM_OF_SNAPSHOTS_TO_CREATE).mapToObj(i -> createDiskImage(diskId)).collect(
                Collectors.toList());
    }

    private CinderDisk createCinderDisk() {
        CinderDisk cd = new CinderDisk();
        cd.setId(Guid.newGuid());
        cd.setActive(true);
        return cd;
    }

    private LunDisk createLunDisk() {
        LunDisk lun = new LunDisk();
        lun.setId(Guid.newGuid());
        return lun;
    }
}
