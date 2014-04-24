package org.ovirt.engine.core.dao.network;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Test;
import org.ovirt.engine.core.common.businessentities.network.VmNic;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.BaseGenericDaoTestCase;
import org.ovirt.engine.core.dao.FixturesTool;
import org.ovirt.engine.core.utils.RandomUtils;

public class VmNicDaoTest extends BaseGenericDaoTestCase<Guid, VmNic, VmNicDao> {
    private static final Guid TEMPLATE_ID = FixturesTool.VM_TEMPLATE_RHEL5;
    private static final Guid VM_ID = FixturesTool.VM_RHEL5_POOL_57;

    private VmNic existingTemplateNic;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        existingTemplateNic = dao.get(FixturesTool.TEMPLATE_NETWORK_INTERFACE);
    }

    /**
     * Ensures null is returned.
     */
    @Test
    public void testGetWithNonExistingId() {
        VmNic result = dao.get(Guid.newGuid());
        assertNull(result);
    }

    /**
     * Ensures that an empty collection is returned.
     */
    @Test
    public void testGetAllForTemplateWithInvalidTemplate() {
        List<VmNic> result = dao.getAllForTemplate(Guid.newGuid());
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    /**
     * Ensures that interfaces are returned.
     */
    @Test
    public void testGetAllForTemplate() {
        List<VmNic> result = dao.getAllForTemplate(TEMPLATE_ID);
        assertNotNull(result);
        assertFalse(result.isEmpty());
        for (VmNic iface : result) {
            assertEquals(TEMPLATE_ID, iface.getVmTemplateId());
        }
    }

    /**
     * Ensures an empty collection is returned.
     */
    @Test
    public void testGetAllInterfacesForVmWithInvalidVm() {
        List<VmNic> result = dao.getAllForVm(Guid.newGuid());
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    /**
     * Ensures that a collection of interfaces related the specified VM are returned.
     */
    @Test
    public void testGetAllInterfacesForVm() {
        List<VmNic> result = dao.getAllForVm(VM_ID);
        assertNotNull(result);
        assertFalse(result.isEmpty());
        for (VmNic iface : result) {
            assertEquals(VM_ID, iface.getVmId());
        }
    }

    @Test
    public void testGetAllForTemplatesByNetwork() throws Exception {
        List<VmNic> result = dao.getAllForTemplatesByNetwork(FixturesTool.NETWORK_ENGINE);
        assertEquals(existingTemplateNic, result.get(0));
    }

    @Test
    public void testGetAllForNetwork() throws Exception {
        List<VmNic> result = dao.getAllForNetwork(FixturesTool.NETWORK_ENGINE);
        assertEquals(existingEntity, result.get(0));
    }

    @Test
    public void testGetAllMacsByStoragePool() throws Exception {
        List<String> result = dao.getAllMacsByDataCenter(FixturesTool.STORAGE_POOL_MIXED_TYPES);
        assertEquals(FixturesTool.MAC_ADDRESS, result.get(0));
    }

    @Test
    public void testGetPluggedForMac() throws Exception {
        List<VmNic> result = dao.getPluggedForMac(FixturesTool.MAC_ADDRESS);
        for (VmNic vmNetworkInterface : result) {
            assertEquals(FixturesTool.MAC_ADDRESS, vmNetworkInterface.getMacAddress());
        }
    }

    @Override
    protected VmNic generateNewEntity() {
        VmNic vmNic = new VmNic();
        vmNic.setId(Guid.newGuid());
        vmNic.setVnicProfileId(FixturesTool.VM_NETWORK_INTERFACE_PROFILE);
        vmNic.setName("eth77");
        vmNic.setLinked(true);
        vmNic.setSpeed(1000);
        vmNic.setType(3);
        vmNic.setMacAddress("01:C0:81:21:71:17");
        return vmNic;
    }

    @Override
    protected void updateExistingEntity() {
        existingEntity.setName(RandomUtils.instance().nextString(15));

    }

    @Override
    protected Guid getExistingEntityId() {
        return FixturesTool.VM_NETWORK_INTERFACE;
    }

    @Override
    protected VmNicDao prepareDao() {
        return dbFacade.getVmNicDao();
    }

    @Override
    protected Guid generateNonExistingId() {
        return Guid.newGuid();
    }

    @Override
    protected int getEneitiesTotalCount() {
        return FixturesTool.NUMBER_OF_VM_INTERFACES;
    }
}
