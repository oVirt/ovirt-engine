package org.ovirt.engine.core.dao.network;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.ovirt.engine.core.common.businessentities.VmDevice;
import org.ovirt.engine.core.common.businessentities.VmDeviceGeneralType;
import org.ovirt.engine.core.common.businessentities.network.VmNetworkInterface;
import org.ovirt.engine.core.common.businessentities.network.VmNetworkStatistics;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.BaseDaoTestCase;
import org.ovirt.engine.core.dao.FixturesTool;

public class VmNetworkInterfaceDaoTest extends BaseDaoTestCase<VmNetworkInterfaceDao> {

    @Inject
    private VmNicDao vmNicDao;

    private static final Guid TEMPLATE_ID = FixturesTool.VM_TEMPLATE_RHEL5;
    private static final Guid VM_ID = FixturesTool.VM_RHEL5_POOL_57;

    protected static final Guid PRIVILEGED_USER_ID   = new Guid("9bf7c640-b620-456f-a550-0348f366544b");
    protected static final Guid UNPRIVILEGED_USER_ID = new Guid("9bf7c640-b620-456f-a550-0348f366544a");

    private VmNetworkInterface existingVmInterface;
    private VmNetworkInterface existingTemplateInterface;
    private VmNetworkInterface newVmInterface;
    private VmDevice newVmDevice = new VmDevice();

    @BeforeEach
    @Override
    public void setUp() throws Exception {
        super.setUp();

        existingVmInterface = dao.get(FixturesTool.VM_NETWORK_INTERFACE);
        existingTemplateInterface = dao.get(FixturesTool.TEMPLATE_NETWORK_INTERFACE);

        newVmInterface = new VmNetworkInterface();
        newVmInterface.setStatistics(new VmNetworkStatistics());
        newVmInterface.setId(Guid.newGuid());
        newVmInterface.setVmId(VM_ID);
        newVmInterface.setVnicProfileId(FixturesTool.VM_NETWORK_INTERFACE_PROFILE);
        newVmInterface.setName("eth77");
        newVmInterface.setNetworkName("enginet");
        newVmInterface.setLinked(true);
        newVmInterface.setSpeed(1000);
        newVmInterface.setType(3);
        newVmInterface.setMacAddress("01:C0:81:21:71:17");

        newVmDevice.setType(VmDeviceGeneralType.INTERFACE);
        newVmDevice.setDevice("bridge");
        newVmDevice.setAddress("sample");
        newVmDevice.setManaged(true);
        newVmDevice.setPlugged(true);
        newVmDevice.setReadOnly(false);
        Map<String, String> customProp = new LinkedHashMap<>();
        customProp.put("prop1", "val1");
        newVmDevice.setCustomProperties(customProp);
    }

    /**
     * Ensures null is returned.
     */
    @Test
    public void testGetWithNonExistingId() {
        VmNetworkInterface result = dao.get(Guid.newGuid());

        assertNull(result);
    }

    /**
     * Ensures that the network interface is returned.
     */
    @Test
    public void testGet() {
        VmNetworkInterface result = dao.get(FixturesTool.VM_NETWORK_INTERFACE);

        assertNotNull(result);
        assertEquals(FixturesTool.VM_NETWORK_INTERFACE, result.getId());
    }

    /**
     * Ensures that an empty collection is returned.
     */
    @Test
    public void testGetAllForTemplateWithInvalidTemplate() {
        List<VmNetworkInterface> result = dao.getAllForTemplate(Guid.newGuid());

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    /**
     * Ensures that interfaces are returned.
     */
    @Test
    public void testGetAllForTemplate() {
        List<VmNetworkInterface> result = dao.getAllForTemplate(TEMPLATE_ID);

        assertCorrectResultForTemplate(result);
    }

    /**
     * Asserts that the right collection containing the network interfaces is returned for a privileged user with filtering enabled
     */
    @Test
    public void testGetAllForTemplateWithPermissionsForPrivilegedUser() {
        List<VmNetworkInterface> result = dao.getAllForTemplate(TEMPLATE_ID, PRIVILEGED_USER_ID, true);

        assertCorrectResultForTemplate(result);
    }

    /**
     * Asserts that an empty list is returned for a non privileged user with filtering enabled
     */
    @Test
    public void testGetAllForTemplateWithPermissionsForUnprivilegedUser() {
        List<VmNetworkInterface> result = dao.getAllForTemplate(TEMPLATE_ID, UNPRIVILEGED_USER_ID, true);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    /**
     * Asserts that the right collection containing the network interfaces is returned for a non privileged user with filtering disabled
     */
    @Test
    public void testGetAllForTemplateWithPermissionsDisabledForUnprivilegedUser() {
        List<VmNetworkInterface> result = dao.getAllForTemplate(TEMPLATE_ID, UNPRIVILEGED_USER_ID, false);

        assertCorrectResultForTemplate(result);
    }

    /**
     * Ensures an empty collection is returned.
     */
    @Test
    public void testGetAllInterfacesForVmWithInvalidVm() {
        List<VmNetworkInterface> result = dao.getAllForVm(Guid.newGuid());

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    /**
     * Ensures that a collection of interfaces related the specified VM are returned.
     */
    @Test
    public void testGetAllInterfacesForVm() {
        List<VmNetworkInterface> result = dao.getAllForVm(VM_ID);

        assertNotNull(result);
        assertFalse(result.isEmpty());
        for (VmNetworkInterface iface : result) {
            assertEquals(VM_ID, iface.getVmId());
        }
    }

    /**
     * Ensures that the VMs for a privileged user are returned
     */
    @Test
    public void testGetAllInterfacesForVmFilteredWithPermissions() {
        List<VmNetworkInterface> result = dao.getAllForVm(VM_ID, PRIVILEGED_USER_ID, true);

        assertNotNull(result);
        assertFalse(result.isEmpty());
        for (VmNetworkInterface iface : result) {
            assertEquals(VM_ID, iface.getVmId());
        }
    }

    /**
     * Ensures that no VMs are returned for an unprivileged user
     */
    @Test
    public void testGetAllInterfacesForVmFilteredWithoutPermissions() {
        List<VmNetworkInterface> result = dao.getAllForVm(VM_ID, UNPRIVILEGED_USER_ID, true);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    /**
     * Ensures that the VMs for an unprivileged user are returned if no filtering is requested
     */
    @Test
    public void testGetAllInterfacesForVmFilteredWithoutPermissionsAndWithoutFiltering() {
        List<VmNetworkInterface> result = dao.getAllForVm(VM_ID, UNPRIVILEGED_USER_ID, false);

        assertNotNull(result);
        assertFalse(result.isEmpty());
        for (VmNetworkInterface iface : result) {
            assertEquals(VM_ID, iface.getVmId());
        }
    }

    @Test
    public void testGetAll() {
        List<VmNetworkInterface> interfaces = dao.getAll();
        assertNotNull(interfaces);
        assertEquals(FixturesTool.NUMBER_OF_VM_INTERFACE_VIEWS, interfaces.size());
    }

    @Test
    public void testGetAllForTemplatesByNetwork() {
        List<VmNetworkInterface> result = dao.getAllForTemplatesByNetwork(FixturesTool.NETWORK_ENGINE);
        assertEquals(existingTemplateInterface, result.get(0));
    }

    @Test
    public void testGetAllForNetwork() {
        List<VmNetworkInterface> result = dao.getAllForNetwork(FixturesTool.NETWORK_ENGINE);
        assertEquals(existingVmInterface, result.get(0));
    }

    @Test
    public void testGetAllWithVnicOutOfSync() {
        VmNetworkInterface iface = dao.get(FixturesTool.VM_NETWORK_INTERFACE);
        assertNotNull(iface);
        assertEquals(VM_ID, iface.getVmId());
        assertTrue(iface.isSynced());
        List<Guid> result = dao.getAllWithVnicOutOfSync(Set.of(VM_ID));
        assertTrue(result.isEmpty());

        iface.setSynced(false);
        vmNicDao.update(iface);
        result = dao.getAllWithVnicOutOfSync(Set.of(VM_ID));
        assertEquals(1, result.size());

        iface.setSynced(true);
        vmNicDao.update(iface);
        result = dao.getAllWithVnicOutOfSync(Set.of(VM_ID));
        assertTrue(result.isEmpty());
    }

    private void assertCorrectResultForTemplate(List<VmNetworkInterface> result) {
        assertNotNull(result);
        assertFalse(result.isEmpty());
        for (VmNetworkInterface iface : result) {
            assertEquals(TEMPLATE_ID, iface.getVmId());
        }
    }
}
