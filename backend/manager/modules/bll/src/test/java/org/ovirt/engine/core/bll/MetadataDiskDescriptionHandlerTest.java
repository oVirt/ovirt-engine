package org.ovirt.engine.core.bll;

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import org.apache.commons.lang.StringUtils;
import org.junit.Before;
import org.junit.Test;
import org.ovirt.engine.core.common.businessentities.Disk;
import org.ovirt.engine.core.common.businessentities.DiskImage;

public class MetadataDiskDescriptionHandlerTest {

    private DiskImage disk;
    private MetadataDiskDescriptionHandler metadataDiskDescriptionHandler;

    @Before
    public void setUp() {
        disk = new DiskImage();
        metadataDiskDescriptionHandler = MetadataDiskDescriptionHandler.getInstance();
    }

    @Test
    public void testJsonDiskDescription() throws IOException {
        disk.setDiskAlias("DiskAlias");
        disk.setDiskDescription("DiskDescription");
        assertDiskDescriptionMap(disk);
    }

    @Test
    public void testJsonNullDiskDescription() throws IOException {
        disk.setDiskAlias("DiskAlias");
        disk.setDiskDescription(null);
        assertDiskDescriptionMap(disk);
    }

    @Test
    public void testJsonEmptyDiskDescription() throws IOException {
        disk.setDiskAlias("DiskAlias");
        disk.setDiskDescription("");
        assertDiskDescriptionMap(disk);
    }

    private void assertDiskDescriptionMap(Disk disk) throws IOException {
        assertEquals("Should be map of disk alias and disk description",
                String.format("{\"DiskAlias\":\"%s\"," +
                                "\"DiskDescription\":\"%s\"}",
                        disk.getDiskAlias(), StringUtils.defaultString(disk.getDiskDescription())),
                metadataDiskDescriptionHandler.getJsonDiskDescription(disk));
    }
}
