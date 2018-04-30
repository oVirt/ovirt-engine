package org.ovirt.engine.core.dao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.ovirt.engine.core.common.businessentities.VmInit;
import org.ovirt.engine.core.compat.Guid;

public class VmInitDaoTest extends BaseDaoTestCase<VmInitDao> {
    private static final Guid EXISTING_VM = FixturesTool.VM_RHEL5_POOL_57;

    private VmInit vmInit;

    @BeforeEach
    @Override
    public void setUp() throws Exception {
        super.setUp();

        vmInit = new VmInit();
        vmInit.setId(EXISTING_VM);
    }

    /**
     * Ensures that get requires a valid id.
     */
    @Test
    public void testGetWithInvalidId() {
        VmInit result = dao.get(Guid.newGuid());
        assertNull(result);
    }

    @Test
    public void testGet() {
        VmInit result = dao.get(EXISTING_VM);
        assertNotNull(result != null);
    }

    @Test
    public void testSave() {
        addVmInit();

        VmInit result = dao.get(EXISTING_VM);
        assertNotNull(result);
        assertEquals("hostname", result.getHostname());
    }

    private void addVmInit() {
        VmInit init = new VmInit();
        init.setId(EXISTING_VM);
        init.setHostname("hostname");
        dao.save(init);
    }

    @Test
    public void testUpdate() {
        addVmInit();
        VmInit init = dao.get(EXISTING_VM);
        init.setHostname("newhostname");
        dao.update(init);

        VmInit result = dao.get(init.getId());
        assertNotNull(result);
        assertEquals("newhostname", result.getHostname());
    }

    @Test
    public void testRemove() {
        addVmInit();
        VmInit result = dao.get(EXISTING_VM);
        assertNotNull(result);

        dao.remove(EXISTING_VM);
        result = dao.get(EXISTING_VM);
        assertNull(result);
    }
}
