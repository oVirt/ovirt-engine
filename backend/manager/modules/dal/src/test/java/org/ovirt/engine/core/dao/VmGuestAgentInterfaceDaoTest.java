package org.ovirt.engine.core.dao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.ovirt.engine.core.common.businessentities.VmGuestAgentInterface;
import org.ovirt.engine.core.compat.Guid;

public class VmGuestAgentInterfaceDaoTest extends BaseDaoTestCase<VmGuestAgentInterfaceDao> {

    private static final int VM_GUEST_AGENT_INTERFACES_SIZE = 2;

    @Test
    public void getAllForVm() {
        List<VmGuestAgentInterface> interfaces = dao.getAllForVm(FixturesTool.VM_RHEL5_POOL_50);
        assertVmGuestAgentInterfaceForVm(interfaces);
    }

    @Test
    public void getAllForVmWithNonExistingVm() {
        List<VmGuestAgentInterface> interfaces = dao.getAllForVm(Guid.Empty);
        assertTrue(interfaces.isEmpty());
    }

    @Test
    public void getAllForVmForPrivilegedUser() {
        List<VmGuestAgentInterface> interfaces = dao.getAllForVm(FixturesTool.VM_RHEL5_POOL_50, PRIVILEGED_USER_ID, true);
        assertVmGuestAgentInterfaceForVm(interfaces);
    }

    @Test
    public void getAllForVmForUnprivilegedUser() {
        List<VmGuestAgentInterface> interfaces = dao.getAllForVm(FixturesTool.VM_RHEL5_POOL_50, UNPRIVILEGED_USER_ID, true);
        assertTrue(interfaces.isEmpty());
    }

    @Test
    public void getAllForVmForUnprivilegedUserWithNoFilter() {
        List<VmGuestAgentInterface> interfaces = dao.getAllForVm(FixturesTool.VM_RHEL5_POOL_50, UNPRIVILEGED_USER_ID, false);
        assertVmGuestAgentInterfaceForVm(interfaces);
    }

    @Test
    public void removeAllForVms() {
        List<VmGuestAgentInterface> interfaces = dao.getAllForVm(FixturesTool.VM_RHEL5_POOL_50);
        assertFalse(interfaces.isEmpty());
        dao.removeAllForVms(Collections.singletonList(FixturesTool.VM_RHEL5_POOL_50));
        interfaces = dao.getAllForVm(FixturesTool.VM_RHEL5_POOL_50);
        assertTrue(interfaces.isEmpty());
    }

    @Test
    public void save() {
        VmGuestAgentInterface guestAgentInterface = createVmGuestAgentInterface();
        assertFalse(dao.getAllForVm(FixturesTool.VM_RHEL5_POOL_50).contains(guestAgentInterface));
        dao.save(guestAgentInterface);
        assertTrue(dao.getAllForVm(FixturesTool.VM_RHEL5_POOL_50).contains(guestAgentInterface));
    }

    private VmGuestAgentInterface createVmGuestAgentInterface() {
        VmGuestAgentInterface guestAgentInterface = new VmGuestAgentInterface();
        guestAgentInterface.setVmId(FixturesTool.VM_RHEL5_POOL_50);
        guestAgentInterface.setMacAddress("AA:AA:AA:AA:AA:AA");
        guestAgentInterface.setInterfaceName("p2p3");
        guestAgentInterface.setIpv4Addresses(Arrays.asList("1.1.1.1", "2.2.2.2", "3.3.3.3"));
        guestAgentInterface.setIpv6Addresses(Collections.emptyList());
        return guestAgentInterface;
    }

    private void assertVmGuestAgentInterfaceForVm(List<VmGuestAgentInterface> interfaces) {
        assertEquals(VM_GUEST_AGENT_INTERFACES_SIZE, interfaces.size());
        for (VmGuestAgentInterface vmGuestAgentInterface : interfaces) {
            assertEquals(FixturesTool.VM_RHEL5_POOL_50, vmGuestAgentInterface.getVmId());
        }
    }
}
