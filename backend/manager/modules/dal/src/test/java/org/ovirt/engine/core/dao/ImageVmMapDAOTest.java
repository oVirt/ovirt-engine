package org.ovirt.engine.core.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.apache.commons.lang.NotImplementedException;
import org.junit.Test;
import org.ovirt.engine.core.common.businessentities.image_vm_map;
import org.ovirt.engine.core.common.businessentities.image_vm_map_id;
import org.ovirt.engine.core.compat.Guid;

/**
 * <code>ImageVmMapDAOTest</code> provides unit tests to validate {@link ImageVmMapDAO}.
 */
public class ImageVmMapDAOTest extends BaseDAOTestCase {
    private static final Guid EXISTING_VM_ID = new Guid("77296e00-0cad-4e5a-9299-008a7b6f4355");
    private static final Guid FREE_VM_ID = new Guid("77296e00-0cad-4e5a-9299-008a7b6f4354");
    private static final Guid EXISTING_IMAGE_ID = new Guid("42058975-3d5e-484a-80c1-01c31207f578");

    private ImageVmMapDAO dao;
    private image_vm_map existingVmMapping;

    @Override
    public void setUp() throws Exception {
        super.setUp();

        dao = prepareDAO(dbFacade.getImageVmMapDAO());

        existingVmMapping = dao.getByImageId(EXISTING_IMAGE_ID);
    }

    @Test
    public void testGetWithNonExistingId() {
        image_vm_map result = dao.get(new image_vm_map_id(Guid.NewGuid(), Guid.NewGuid()));

        assertNull(result);
    }

    @Test
    public void testGet() {
        image_vm_map result = dao.get(new image_vm_map_id(EXISTING_IMAGE_ID, EXISTING_VM_ID));

        assertNotNull(result);
        assertEquals(existingVmMapping, result);
    }

    @Test
    public void testGetByImageId() {
        image_vm_map result = dao.getByImageId(EXISTING_IMAGE_ID);

        assertNotNull(result);
        assertEquals(existingVmMapping, result);
    }

    @Test(expected = NotImplementedException.class)
    public void testGetAll() {
        dao.getAll();
    }

    @Test
    public void testUpdate() {
        existingVmMapping.setactive(existingVmMapping.getactive() == false);

        dao.update(existingVmMapping);

        image_vm_map result = dao.getByImageId(existingVmMapping.getimage_id());

        assertEquals(existingVmMapping, result);
    }

    @Test
    public void testRemove() {
        dao.remove(existingVmMapping.getId());

        image_vm_map result = dao.getByImageId(existingVmMapping.getimage_id());

        assertNull(result);
    }

    @Test
    public void testGetByVmIdForVmWithNoMappings() {
        List<image_vm_map> result = dao.getByVmId(FREE_VM_ID);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    public void testGetByVmId() {
        List<image_vm_map> result = dao.getByVmId(EXISTING_VM_ID);

        assertNotNull(result);
        assertFalse(result.isEmpty());
        for (image_vm_map map : result) {
            assertEquals(EXISTING_VM_ID, map.getvm_id());
        }
    }
}
