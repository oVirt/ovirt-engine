package org.ovirt.engine.api.restapi.types;

import static org.ovirt.engine.api.restapi.types.MappingTestHelper.populate;

import org.junit.Test;
import org.ovirt.engine.api.model.Disk;
import org.ovirt.engine.api.model.DiskFormat;
import org.ovirt.engine.api.model.DiskStatus;
import org.ovirt.engine.api.model.ScsiGenericIO;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;

public class DiskMapperTest extends AbstractInvertibleMappingTest<Disk, DiskImage, DiskImage> {

    public DiskMapperTest() {
        super(Disk.class, DiskImage.class, DiskImage.class);
    }

    @Override
    protected Disk postPopulate(Disk model) {
        model.setFormat(MappingTestHelper.shuffle(DiskFormat.class));
        model.setSgio(MappingTestHelper.shuffle(ScsiGenericIO.class));
        model.setStatus(MappingTestHelper.shuffle(DiskStatus.class));
        model.setLunStorage(null);
        return model;
    }

    @Override
    protected void verify(Disk model, Disk transform) {
        assertNotNull(transform);
        assertEquals(model.getId(), transform.getId());
        assertEquals(model.getImageId(), transform.getImageId());
        assertEquals(model.getFormat(), transform.getFormat());
        assertEquals(model.isReadOnly(), transform.isReadOnly());
        assertEquals(model.getDescription(), transform.getDescription());
        assertEquals(model.getLogicalName(), transform.getLogicalName());
        assertEquals(model.getOpenstackVolumeType().getName(), transform.getOpenstackVolumeType().getName());
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

        Disk model = Disk.class.cast(populate(Disk.class));
        model = postPopulate(model);
        Mapper<Disk, org.ovirt.engine.core.common.businessentities.storage.Disk> out =
                getMappingLocator().getMapper(Disk.class, org.ovirt.engine.core.common.businessentities.storage.Disk.class);
        Mapper<org.ovirt.engine.core.common.businessentities.storage.Disk, Disk> back =
                getMappingLocator().getMapper(org.ovirt.engine.core.common.businessentities.storage.Disk.class, Disk.class);
        DiskImage to = (DiskImage) out.map(model, null);
        DiskImage inverse = getInverse(to);
        Disk transform = back.map(inverse, null);
        verify(model, transform);
    }

    @Test
    public void testReadOnlyMapping() {
        Disk model = new Disk();
        model.setReadOnly(true);

        org.ovirt.engine.core.common.businessentities.storage.Disk entity = DiskMapper.map(model, null);
        assertTrue(entity.getReadOnly());

        model.setReadOnly(false);
        entity = DiskMapper.map(model, null);
        assertFalse(entity.getReadOnly());

        model.setReadOnly(null);
        entity = DiskMapper.map(model, null);
        assertNull(entity.getReadOnly());
    }
}
