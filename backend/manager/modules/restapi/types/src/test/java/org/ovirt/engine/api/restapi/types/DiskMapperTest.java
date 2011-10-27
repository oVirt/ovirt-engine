package org.ovirt.engine.api.restapi.types;

import org.ovirt.engine.api.common.util.StatusUtils;
import org.ovirt.engine.api.model.Disk;
import org.ovirt.engine.api.model.DiskFormat;
import org.ovirt.engine.api.model.DiskInterface;
import org.ovirt.engine.api.model.DiskStatus;
import org.ovirt.engine.api.model.DiskType;
import org.ovirt.engine.core.common.businessentities.DiskImage;

public class DiskMapperTest extends AbstractInvertibleMappingTest<Disk, DiskImage, DiskImage> {

    protected DiskMapperTest() {
        super(Disk.class, DiskImage.class, DiskImage.class);
    }

    @Override
    protected Disk postPopulate(Disk model) {
        model.setType(MappingTestHelper.shuffle(DiskType.class).value());
        model.setFormat(MappingTestHelper.shuffle(DiskFormat.class).value());
        model.setInterface(MappingTestHelper.shuffle(DiskInterface.class).value());
        model.setStatus(StatusUtils.create(MappingTestHelper.shuffle(DiskStatus.class)));
        return model;
    }

    @Override
    protected void verify(Disk model, Disk transform) {
        assertNotNull(transform);
        assertEquals(model.getId(), transform.getId());
        assertEquals(model.getSize(), transform.getSize());
        assertEquals(model.getFormat(), transform.getFormat());
        assertEquals(model.getInterface(), transform.getInterface());
        assertEquals(model.getType(), transform.getType());
        assertEquals("unexpected status", model.getStatus().getState(), transform.getStatus().getState());
        assertEquals("unexpected sparse", model.isSparse(), transform.isSparse());
        assertEquals("unexpected bootable", model.isBootable(), transform.isBootable());
        assertEquals("unexpected propagate errors", model.isPropagateErrors(), transform.isPropagateErrors());
        assertEquals("unexpected wipe after delete", model.isWipeAfterDelete(), transform.isWipeAfterDelete());
    }
}
