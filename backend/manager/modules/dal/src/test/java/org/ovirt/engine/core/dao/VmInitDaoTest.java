package org.ovirt.engine.core.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.junit.Test;
import org.ovirt.engine.core.common.businessentities.VmInit;
import org.ovirt.engine.core.common.businessentities.VmStatic;
import org.ovirt.engine.core.compat.Guid;

public class VmInitDaoTest extends BaseDaoTestCase {
    private static final Guid EXISTING_VM = FixturesTool.VM_RHEL5_POOL_57;

    private VmInit vmInit;
    private VmInitDao vmInitDao;
    private VmStaticDao vmStaticDao;
    private VmStatic vmStatic;

    @Override
    public void setUp() throws Exception {
        super.setUp();

        vmInitDao = dbFacade.getVmInitDao();
        vmInit = new VmInit();
        vmInit.setId(EXISTING_VM);

        vmStaticDao = dbFacade.getVmStaticDao();
        vmStatic = vmStaticDao.get(EXISTING_VM);
        vmStatic.setVmInit(vmInit);
    }

    /**
     * Ensures that get requires a valid id.
     */
    @Test
    public void testGetWithInvalidId() {
        VmInit result = vmInitDao.get(Guid.newGuid());
        assertNull(result);
    }

    @Test
    public void testGet() {
        VmInit result = vmInitDao.get(EXISTING_VM);
        assertNotNull(result != null);
    }

    @Test
    public void testSave() {
        addVmInit();

        VmInit result = vmInitDao.get(EXISTING_VM);
        assertNotNull(result);
        assertEquals("hostname", result.getHostname());
    }

    private void addVmInit() {
        VmInit init = new VmInit();
        init.setId(EXISTING_VM);
        init.setHostname("hostname");
        vmInitDao.save(init);
    }

    @Test
    public void testUpdate() {
        addVmInit();
        VmInit init = vmInitDao.get(EXISTING_VM);
        init.setHostname("newhostname");
        vmInitDao.update(init);

        VmInit result = vmInitDao.get(init.getId());
        assertNotNull(result);
        assertEquals("newhostname", result.getHostname());
    }

    @Test
    public void testRemove() {
        addVmInit();
        VmInit result = vmInitDao.get(EXISTING_VM);
        assertNotNull(result);

        vmInitDao.remove(EXISTING_VM);
        result = vmInitDao.get(EXISTING_VM);
        assertNull(result);
    }
}
