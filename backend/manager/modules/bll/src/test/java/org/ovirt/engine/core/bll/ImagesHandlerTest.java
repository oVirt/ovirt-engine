package org.ovirt.engine.core.bll;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mockStatic;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.RefObject;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dao.DiskImageDAO;
import org.ovirt.engine.core.utils.RandomUtils;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

/**
 * Tests for the {@link ImagesHandler} class.
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest(DbFacade.class)
public class ImagesHandlerTest {

    /* --- Tests for getImagesMappedToDrive --- */

    @Test
    public void testGetImagesMappedToDrive() {
        String driveMapping = RandomUtils.instance().nextNumericString(5);
        Guid vmID = new Guid(UUID.randomUUID());

        List<DiskImage> disks = mockDisks(driveMapping);

        // Mock the DbFacade
        DbFacade dbFacadeMock = mock(DbFacade.class);
        mockStatic(DbFacade.class);
        when(DbFacade.getInstance()).thenReturn(dbFacadeMock);

        // Mock the VMDAO
        DiskImageDAO diskImageDAOMock = mock(DiskImageDAO.class);
        when(dbFacadeMock.getDiskImageDAO()).thenReturn(diskImageDAOMock);
        when(diskImageDAOMock.getAllForVm(vmID)).thenReturn(disks);

        RefObject<DiskImage> activeImageRef = new RefObject<DiskImage>();
        RefObject<DiskImage> inactiveImageRef = new RefObject<DiskImage>();

        assertEquals("Only two disks should have been returned",
                2, ImagesHandler.getImagesMappedToDrive(vmID, driveMapping, activeImageRef, inactiveImageRef));
        assertEquals("Wrong active disk", disks.get(0), activeImageRef.argvalue);
        assertEquals("Wrong inactive disk", disks.get(1), inactiveImageRef.argvalue);
    }

    @Test
    public void testGetImagesMappedToDriveFromGivenList() {
        String driveMapping = RandomUtils.instance().nextNumericString(5);

        List<DiskImage> disks = mockDisks(driveMapping);

        RefObject<DiskImage> activeImageRef = new RefObject<DiskImage>();
        RefObject<DiskImage> inactiveImageRef = new RefObject<DiskImage>();

        assertEquals("Only two disks should have been returned",
                2, ImagesHandler.getImagesMappedToDrive(disks, driveMapping, activeImageRef, inactiveImageRef));
        assertEquals("Wrong active disk", disks.get(0), activeImageRef.argvalue);
        assertEquals("Wrong inactive disk", disks.get(1), inactiveImageRef.argvalue);
    }

    /** @return a mock set of disks to be queried. The first id always active, the second inactive, and the third mapped incorrectly */
    private static List<DiskImage> mockDisks(String driveMapping) {
        // An active disk
        DiskImage activeImage = new DiskImage();
        activeImage.setactive(true);
        activeImage.setinternal_drive_mapping(driveMapping);

        // An inactive disk
        DiskImage inactiveImage = new DiskImage();
        inactiveImage.setactive(false);
        inactiveImage.setinternal_drive_mapping(driveMapping);

        // A disk from the wrong drive
        DiskImage wrongDriveImage = new DiskImage();
        wrongDriveImage.setinternal_drive_mapping("11" + driveMapping);

        return Arrays.asList(activeImage, inactiveImage, wrongDriveImage);
    }
}
