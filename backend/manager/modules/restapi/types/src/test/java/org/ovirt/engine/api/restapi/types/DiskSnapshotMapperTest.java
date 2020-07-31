package org.ovirt.engine.api.restapi.types;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.ovirt.engine.api.restapi.types.MappingTestHelper.populate;

import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.ovirt.engine.api.model.DiskFormat;
import org.ovirt.engine.api.model.DiskSnapshot;
import org.ovirt.engine.api.model.DiskStatus;
import org.ovirt.engine.api.model.ScsiGenericIO;
import org.ovirt.engine.core.common.businessentities.storage.Disk;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.utils.MockConfigDescriptor;
import org.ovirt.engine.core.utils.MockedConfig;

public class DiskSnapshotMapperTest extends AbstractInvertibleMappingTest<DiskSnapshot, DiskImage, DiskImage> {

    public DiskSnapshotMapperTest() {
        super(DiskSnapshot.class, DiskImage.class, DiskImage.class);
    }

    public static Stream<MockConfigDescriptor<?>> mockConfiguration() {
        return Stream.of(
                MockConfigDescriptor.of(ConfigValues.PropagateDiskErrors, false)
        );
    }

    @Override
    protected DiskSnapshot postPopulate(DiskSnapshot model) {
        model.setFormat(DiskFormat.COW);
        model.setSgio(ScsiGenericIO.FILTERED);
        model.setStatus(DiskStatus.ILLEGAL);
        model.setLunStorage(null);
        return model;
    }

    @Override
    protected void verify(DiskSnapshot model, DiskSnapshot transform) {
        assertNotNull(transform);
        assertEquals(model.getId(), transform.getId());
        assertEquals(model.getFormat(), transform.getFormat());
        assertEquals(model.getDescription(), transform.getDescription());
        assertNotNull(model.getSnapshot());
        assertEquals(model.getSnapshot().getId(), transform.getSnapshot().getId());
        assertEquals(model.getStatus(), transform.getStatus(), "unexpected status");
        assertEquals(model.isSparse(), transform.isSparse(), "unexpected sparse");
        assertEquals(model.isPropagateErrors(), transform.isPropagateErrors(), "unexpected propagate errors");
        assertEquals(model.isWipeAfterDelete(), transform.isWipeAfterDelete(), "unexpected wipe after delete");
        assertEquals(model.isShareable(), transform.isShareable(), "unexpected shareable");
    }


    @MockedConfig("mockConfiguration")
    @Test
    @Override
    public void testRoundtrip() throws Exception {
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
