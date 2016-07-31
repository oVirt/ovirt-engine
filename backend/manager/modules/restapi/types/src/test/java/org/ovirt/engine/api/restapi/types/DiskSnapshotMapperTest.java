package org.ovirt.engine.api.restapi.types;

import static org.ovirt.engine.api.restapi.types.MappingTestHelper.populate;

import org.junit.Test;
import org.ovirt.engine.api.model.DiskFormat;
import org.ovirt.engine.api.model.DiskSnapshot;
import org.ovirt.engine.api.model.DiskStatus;
import org.ovirt.engine.api.model.ScsiGenericIO;
import org.ovirt.engine.core.common.businessentities.storage.Disk;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;

public class DiskSnapshotMapperTest extends AbstractInvertibleMappingTest<DiskSnapshot, DiskImage, DiskImage> {

    public DiskSnapshotMapperTest() {
        super(DiskSnapshot.class, DiskImage.class, DiskImage.class);
    }

    @Override
    protected DiskSnapshot postPopulate(DiskSnapshot model) {
        model.setFormat(MappingTestHelper.shuffle(DiskFormat.class));
        model.setSgio(MappingTestHelper.shuffle(ScsiGenericIO.class));
        model.setStatus(MappingTestHelper.shuffle(DiskStatus.class));
        model.setLunStorage(null);
        return model;
    }

    @Override
    protected void verify(DiskSnapshot model, DiskSnapshot transform) {
        assertNotNull(transform);
        assertEquals(model.getId(), transform.getId());
        assertEquals(model.getFormat(), transform.getFormat());
        assertEquals(model.isReadOnly(), transform.isReadOnly());
        assertEquals(model.getDescription(), transform.getDescription());
        assertNotNull(model.getSnapshot());
        assertEquals(model.getSnapshot().getId(), transform.getSnapshot().getId());
        assertEquals("unexpected status", model.getStatus(), transform.getStatus());
        assertEquals("unexpected sparse", model.isSparse(), transform.isSparse());
        assertEquals("unexpected propagate errors", model.isPropagateErrors(), transform.isPropagateErrors());
        assertEquals("unexpected wipe after delete", model.isWipeAfterDelete(), transform.isWipeAfterDelete());
        assertEquals("unexpected shareable", model.isShareable(), transform.isShareable());
    }

    @Test
    @Override
    public void testRoundtrip() throws Exception {
        setUpConfigExpectations();

        DiskSnapshot model = DiskSnapshot.class.cast(populate(DiskSnapshot.class));
        model = postPopulate(model);
        Mapper<DiskSnapshot, Disk> out =
                getMappingLocator().getMapper(DiskSnapshot.class, Disk.class);
        Mapper<Disk, DiskSnapshot> back =
                getMappingLocator().getMapper(Disk.class, DiskSnapshot.class);
        DiskImage to = (DiskImage) out.map(model, null);
        DiskImage inverse = getInverse(to);
        DiskSnapshot transform = back.map(inverse, null);
        verify(model, transform);
    }

}
