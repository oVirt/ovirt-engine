package org.ovirt.engine.core.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.ovirt.engine.core.dao.FixturesTool.IMAGE_ID;
import static org.ovirt.engine.core.dao.FixturesTool.TEMPLATE_IMAGE_ID;

import java.util.List;

import org.apache.commons.lang.NotImplementedException;
import org.junit.Test;
import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.compat.Guid;

/**
 * <code>DiskImageDAOTest</code> provides unit tests to validate {@link DiskImageDAO}.
 */
public class DiskImageDAOTest extends BaseReadDaoTestCase<Guid, DiskImage, DiskImageDAO> {
    private static final Guid ANCESTOR_IMAGE_ID = new Guid("c9a559d9-8666-40d1-9967-759502b19f0b");

    private DiskImage existingTemplate;

    private static final int TOTAL_DISK_IMAGES = 7;
    private BaseDiskDao diskDao;

    @Override
    protected Guid generateNonExistingId() {
        return Guid.newGuid();
    }

    @Override
    protected int getEneitiesTotalCount() {
        return TOTAL_DISK_IMAGES;
    }

    @Override
    protected DiskImageDAO prepareDao() {
        return dbFacade.getDiskImageDao();
    }

    @Override
    protected Guid getExistingEntityId() {
        return IMAGE_ID;
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();

        diskDao = dbFacade.getBaseDiskDao();
        existingTemplate = dao.get(TEMPLATE_IMAGE_ID);
    }

    @Test
    @Override
    public void testGet() {
        DiskImage result = dao.get(existingEntity.getImageId());

        assertNotNull(result);
        assertEquals(existingEntity, result);
    }

    @Override
    @Test(expected = NotImplementedException.class)
    public void testGetAll() {
        super.testGetAll();
    }

    @Test
    public void testGetAncestorForSon() {
        DiskImage result = dao.getAncestor(existingEntity.getImageId());

        assertNotNull(result);
        assertEquals(ANCESTOR_IMAGE_ID, result.getImageId());
    }

    @Test
    public void testGetAncestorForFather() {
        DiskImage result = dao.getAncestor(ANCESTOR_IMAGE_ID);

        assertNotNull(result);
        assertEquals(ANCESTOR_IMAGE_ID, result.getImageId());
    }

    @Test
    public void testGetTemplate() {
        DiskImage result = dao.get(TEMPLATE_IMAGE_ID);
        assertNotNull(result);
        assertEquals(existingTemplate, result);
    }

    @Test
    public void testGetImagesWithNoDisk() {
        List<DiskImage> result = dao.getImagesWithNoDisk(FixturesTool.VM_RHEL5_POOL_57);

        assertNotNull(result);
        assertFalse(result.isEmpty());

        for (DiskImage image : result) {
            assertFalse(diskDao.exists(image.getId()));
        }
    }

    @Test
    public void testGetImagesWithNoDiskReturnsEmptyList() {
        List<DiskImage> result = dao.getImagesWithNoDisk(Guid.newGuid());

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }
}
