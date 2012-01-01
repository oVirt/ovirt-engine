package org.ovirt.engine.core.bll;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;

import org.junit.Test;
import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.utils.RandomUtils;

/**
 * Tests for the {@link ImagesHandler} class.
 */
public class ImagesHandlerTest {

    /* --- Tests for isVmInPreview() --- */

    @Test
    public void isVmInPreviewReturnsFalseForNoDisks() throws Exception {
        assertFalse(ImagesHandler.isVmInPreview(new ArrayList<DiskImage>()));
    }

    @Test
    public void isVmInPreviewReturnsFalseForNoPreview() throws Exception {
        ArrayList<DiskImage> images = new ArrayList<DiskImage>();
        addDiskImage(images, RandomUtils.instance().nextNumericString(5));
        addDiskImage(images, RandomUtils.instance().nextNumericString(5));
        assertFalse(ImagesHandler.isVmInPreview(images));
    }

    @Test
    public void isVmInPreviewReturnsTrueForPreview() throws Exception {
        ArrayList<DiskImage> images = new ArrayList<DiskImage>();
        String internalMapping = RandomUtils.instance().nextNumericString(5);
        addDiskImage(images, internalMapping);
        addDiskImage(images, internalMapping);
        assertTrue(ImagesHandler.isVmInPreview(images));
    }

    /**
     * Add a disk image with the given params to the images list.
     *
     * @param images
     *            The list of disk images.
     * @param internalMapping
     *            The internal mapping property.
     */
    private void addDiskImage(ArrayList<DiskImage> images, String internalMapping) {
        DiskImage diskImage = new DiskImage();
        diskImage.setinternal_drive_mapping(internalMapping);
        images.add(diskImage);
    }
}
