package org.ovirt.engine.api.restapi.types;

import static org.ovirt.engine.api.restapi.types.MappingTestHelper.populate;

import org.junit.Test;
import org.ovirt.engine.api.model.Disk;
import org.ovirt.engine.api.model.DiskFormat;
import org.ovirt.engine.api.model.DiskStatus;
import org.ovirt.engine.api.model.HostStorage;
import org.ovirt.engine.core.common.businessentities.storage.LunDisk;

public class LunDiskMapperTest extends AbstractInvertibleMappingTest<Disk, LunDisk, LunDisk> {

    public LunDiskMapperTest() {
        super(Disk.class, LunDisk.class, LunDisk.class);
    }

    @Override
    protected Disk postPopulate(Disk model) {
        model.setFormat(MappingTestHelper.shuffle(DiskFormat.class));
        model.setStatus(MappingTestHelper.shuffle(DiskStatus.class));
        model.setLunStorage(new HostStorage());
        return model;
    }

    @Override
    protected void verify(Disk model, Disk transform) {
        assertNotNull(transform);
        assertEquals(model.getId(), transform.getId());
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
        LunDisk to = (LunDisk) out.map(model, null);
        LunDisk inverse = getInverse(to);
        Disk transform = back.map(inverse, null);
        verify(model, transform);
    }
}
