package org.ovirt.engine.core.bll;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;
import org.ovirt.engine.core.common.businessentities.DiskImage;

/** A test case for {@link ImagesHandler} */
public class ImagesHandlerTest {

    /** The prefix to use for all tests */
    private static final String prefix = "PREFIX";

    /** The disk to use for testing */
    private DiskImage disk;

    @Before
    public void setUp() {
        disk = new DiskImage();
    }

    @Test
    public void testGetSuggestedDiskAliasNullDisk() {
        assertEquals("null disk does not give the default name",
                prefix + ImagesHandler.DISK + ImagesHandler.DefaultDriveName,
                ImagesHandler.getSuggestedDiskAlias(null, prefix, 1));
    }

    @Test
    public void testGetSuggestedDiskAliasNullAliasDisk() {
        disk.setDiskAlias(null);
        assertEquals("disk with null alias does not give the default name",
                prefix + ImagesHandler.DISK + ImagesHandler.DefaultDriveName,
                ImagesHandler.getSuggestedDiskAlias(disk, prefix, 1));
    }

    @Test
    public void testGetSuggestedDiskAliasNotNullAliasDisk() {
        disk.setDiskAlias("someAlias");
        assertEquals("a new alias was generated instead of returning the pre-difined one",
                disk.getDiskAlias(),
                ImagesHandler.getSuggestedDiskAlias(disk, prefix, 1));
    }
}
