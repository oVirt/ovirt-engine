package org.ovirt.engine.core.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Test;
import org.ovirt.engine.core.common.businessentities.VmGuestAgentInterface;
import org.ovirt.engine.core.compat.Guid;

public class VmGuestAgentInterfaceDaoTest extends BaseDaoTestCase {

    private static final Guid EXISTING_VM_ID = new Guid("77296e00-0cad-4e5a-9299-008a7b6f4354");
    private static final int VM_GUEST_AGENT_INTERFACES_SIZE = 2;
    private VmGuestAgentInterfaceDao dao;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        dao = dbFacade.getVmGuestAgentInterfaceDao();
    }

    @Test
    public void getAllForVm() {
        List<VmGuestAgentInterface> interfaces = dao.getAllForVm(EXISTING_VM_ID);
        assertVmGuestAgentInterfaceForVm(interfaces);
    }

    @Test
    public void getAllForVmWithNonExistingVm() {
        List<VmGuestAgentInterface> interfaces = dao.getAllForVm(Guid.Empty);
        assertTrue(interfaces.isEmpty());
    }

    @Test
    public void getAllForVmForPrivilegedUser() {
        List<VmGuestAgentInterface> interfaces = dao.getAllForVm(EXISTING_VM_ID, PRIVILEGED_USER_ID, true);
        assertVmGuestAgentInterfaceForVm(interfaces);
    }

    @Test
    public void getAllForVmForUnprivilegedUser() {
        List<VmGuestAgentInterface> interfaces = dao.getAllForVm(EXISTING_VM_ID, UNPRIVILEGED_USER_ID, true);
        assertTrue(interfaces.isEmpty());
    }

    @Test
    public void getAllForVmForUnprivilegedUserWithNoFilter() {
        List<VmGuestAgentInterface> interfaces = dao.getAllForVm(EXISTING_VM_ID, UNPRIVILEGED_USER_ID, false);
        assertVmGuestAgentInterfaceForVm(interfaces);
    }

    @Test
    public void removeAllForVm() {
        List<VmGuestAgentInterface> interfaces = dao.getAllForVm(EXISTING_VM_ID);
        assertFalse(interfaces.isEmpty());
        dao.removeAllForVm(EXISTING_VM_ID);
        interfaces = dao.getAllForVm(EXISTING_VM_ID);
        assertTrue(interfaces.isEmpty());
    }

    @Test
    public void save() {
        VmGuestAgentInterface guestAgentInterface = createVmGuestAgentInterface();
        assertFalse(dao.getAllForVm(EXISTING_VM_ID).contains(guestAgentInterface));
        dao.save(guestAgentInterface);
        assertTrue(dao.getAllForVm(EXISTING_VM_ID).contains(guestAgentInterface));
    }

    private VmGuestAgentInterface createVmGuestAgentInterface() {
        VmGuestAgentInterface guestAgentInterface = new VmGuestAgentInterface();
        guestAgentInterface.setVmId(EXISTING_VM_ID);
        guestAgentInterface.setMacAddress("AA:AA:AA:AA:AA:AA");
        guestAgentInterface.setInterfaceName("p2p3");
        guestAgentInterface.setIpv4Addresses(Arrays.asList("1.1.1.1", "2.2.2.2", "3.3.3.3"));
        guestAgentInterface.setIpv6Addresses(Collections.emptyList());
        return guestAgentInterface;
    }

    private void assertVmGuestAgentInterfaceForVm(List<VmGuestAgentInterface> interfaces) {
        assertEquals(interfaces.size(), VM_GUEST_AGENT_INTERFACES_SIZE);
        for (VmGuestAgentInterface vmGuestAgentInterface : interfaces) {
            assertEquals(EXISTING_VM_ID, vmGuestAgentInterface.getVmId());
        }
    }
}
