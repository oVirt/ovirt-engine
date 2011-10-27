package org.ovirt.engine.core.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.List;

import org.apache.commons.lang.NotImplementedException;
import org.junit.Test;
import org.ovirt.engine.core.common.businessentities.DiskImageTemplate;
import org.ovirt.engine.core.compat.Guid;

public class DiskImageTemplateDAOTest  extends BaseDAOTestCase  {

    private static final Guid EXISTING_IMAGE_DISK_TEMPLATE = new Guid("42058975-3d5e-484a-80c1-01c31207f578");
    private static final Guid EXISTING_VM_TEMPLATE = new Guid("1b85420c-b84c-4f29-997e-0eb674b40b79");

    private DiskImageTemplate existingTemplate;
    private DiskImageTemplate newTemplate;
    private DiskImageTemplateDAO dao;



    @Override
    public void setUp() throws Exception {
        super.setUp();
        dao = prepareDAO(dbFacade.getDiskImageTemplateDAO());
        existingTemplate = dao.get(EXISTING_IMAGE_DISK_TEMPLATE );
        newTemplate = new DiskImageTemplate();
        newTemplate.setId(Guid.NewGuid());
        newTemplate.setvmt_guid(EXISTING_VM_TEMPLATE);
        newTemplate.setvtim_it_guid(newTemplate.getId());
        newTemplate.setinternal_drive_mapping("1");
    }

    /**
     * Ensures that retrieving a template works.
     */
    @Test
    public void testGetTemplate() {
        DiskImageTemplate result = dao.get(EXISTING_IMAGE_DISK_TEMPLATE);
        assertNotNull(result);
        assertEquals(existingTemplate, result);
    }

    /**
     * Ensures that the right template is returned.
     */
    @Test
    public void testGetTemplateByVmTemplateAndId() {
        DiskImageTemplate result = dao.getByVmTemplateAndId(
                EXISTING_VM_TEMPLATE, existingTemplate.getit_guid());

        assertNotNull(result);
        assertEquals(existingTemplate.getit_guid(), result.getit_guid());
    }

    /**
     * Ensures that all templates are returned.
     */
    @Test
    public void testGetAllTemplatesForVmTemplate() {
        List<DiskImageTemplate> result = dao
                .getAllByVmTemplate(existingTemplate.getvmt_guid());

        assertNotNull(result);
        assertFalse(result.isEmpty());
        for (DiskImageTemplate template : result) {
            assertEquals(existingTemplate.getit_guid(), template.getit_guid());
        }
    }

    /**
     * Ensures that saving a disk template works as expected.
     */
    @Test
    public void testSaveTemplate() {
        dao.save(newTemplate);

        DiskImageTemplate result = dao.get(newTemplate.getId());

        assertNotNull(result);
        assertEquals(newTemplate, result);
    }

    /**
     * Ensures that updating a template works.
     */
    @Test
    public void testUpdateTemplate() {
        existingTemplate.setdescription("This is a new description");
        dao.update(existingTemplate);
        DiskImageTemplate result = dao.get(existingTemplate
                .getit_guid());

        assertNotNull(result);
        assertEquals(existingTemplate, result);
    }

    /**
     * Ensures that removing a template works as expected.
     */
    @Test
    public void testRemoveTemplate() {
        dao.remove(existingTemplate.getvmt_guid());
        DiskImageTemplate result = dao.get(existingTemplate
                .getvmt_guid());

        assertNull(result);
    }

    @Test(expected=NotImplementedException.class)
    public void testGetAll() {
        dao.getAll(); //there is no implementation for this method
    }






}
