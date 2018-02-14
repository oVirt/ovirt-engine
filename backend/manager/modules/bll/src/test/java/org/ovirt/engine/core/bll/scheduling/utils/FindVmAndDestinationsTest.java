package org.ovirt.engine.core.bll.scheduling.utils;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doReturn;
import static org.mockito.MockitoAnnotations.initMocks;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.ovirt.engine.core.common.businessentities.MigrationSupport;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.VmDao;

public class FindVmAndDestinationsTest {
    @Mock
    VmDao vmDao;

    @InjectMocks
    FindVmAndDestinations findVmAndDestinations = new FindVmAndDestinations(null, 80, 1024);

    @Before
    public void setUp() {
        initMocks(this);
    }

    /**
     * This test verifies that VMs can be selected as sources when they support migration
     * and that migration support is not tied to host pinning.
     */
    @Test
    public void testGetMigratableVmsFromHost() throws Exception {
        List<VDS> hosts = new ArrayList<>();
        hosts.add(createHost());
        hosts.add(createHost());
        hosts.add(createHost());

        /*
         * Prepare all combinations of migration support and affinity.
         * There will be hosts.size() + 1 VMs for each migration support value.
         * The VMs will have 0, 1, 2 ... hosts.size() hosts in their dedicatedVmForVds list.
         */
        List<VM> vms = new ArrayList<>();
        for (MigrationSupport m: EnumSet.allOf(MigrationSupport.class)) {
            for (int i = 0; i <= hosts.size(); i++) {
                vms.add(createVm(m, hosts.subList(0, i)));
            }
        }

        doReturn(vms).when(vmDao).getAllRunningForVds(null);
        List<VM> selected = findVmAndDestinations.getMigratableVmsRunningOnVds(vmDao, null);
        assertEquals(hosts.size() + 1, selected.size());
        for (VM vm: selected) {
            assertEquals(MigrationSupport.MIGRATABLE, vm.getMigrationSupport());
        }
    }

    private VDS createHost() {
        VDS host = new VDS();
        host.setId(Guid.newGuid());
        host.setVdsName(host.getId().toString());
        return host;
    }

    private VM createVm(MigrationSupport migrationSupport, Iterable<VDS> pinToHosts) {
        VM vm = new VM();
        vm.setId(Guid.newGuid());
        vm.setName(vm.getId().toString());
        vm.setMigrationSupport(migrationSupport);
        vm.setDedicatedVmForVdsList(new ArrayList<Guid>());
        for (VDS host: pinToHosts) {
            vm.getDedicatedVmForVdsList().add(host.getId());
        }
        return vm;
    }
}
