package org.ovirt.engine.core.bll.scheduling.pending;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.compat.Guid;

public class PendingResourceManagerTest {

    @Test
    public void testClearVm() {
        PendingResourceManager manager = new PendingResourceManager();

        VDS host = new VDS();
        host.setId(Guid.newGuid());

        VM vm1 = new VM();
        vm1.setId(Guid.newGuid());

        VM vm2 = new VM();
        vm2.setId(Guid.newGuid());

        manager.addPending(new PendingVM(host, vm1));
        manager.addPending(new PendingVM(host, vm2));
        manager.addPending(new PendingMemory(host, vm2, 1024));
        manager.addPending(new PendingCpuCores(host, vm2, 10));

        manager.clearVm(vm2);

        Set<Guid> pending = PendingVM.collectForHost(manager, host.getId());
        assertEquals(1, pending.size());
        assertEquals(vm1.getId(), pending.iterator().next());
        assertEquals(0, PendingCpuCores.collectForHost(manager, host.getId()));
        assertEquals(0, PendingMemory.collectForHost(manager, host.getId()));

        // test clearing the VM twice to make sure it works..
        manager.clearVm(vm2);
    }

    @Test
    public void testClearHost() {
        PendingResourceManager manager = new PendingResourceManager();

        VDS host = new VDS();
        host.setId(Guid.newGuid());

        VM vm1 = new VM();
        vm1.setId(Guid.newGuid());

        VM vm2 = new VM();
        vm2.setId(Guid.newGuid());

        manager.addPending(new PendingVM(host, vm1));
        manager.addPending(new PendingVM(host, vm2));
        manager.addPending(new PendingMemory(host, vm2, 1024));
        manager.addPending(new PendingCpuCores(host, vm2, 10));

        manager.clearHost(host);

        Set<Guid> pending = PendingVM.collectForHost(manager, host.getId());
        assertEquals(0, pending.size());
        assertEquals(0, PendingCpuCores.collectForHost(manager, host.getId()));
        assertEquals(0, PendingMemory.collectForHost(manager, host.getId()));
    }

    @Test
    public void testAddPending() {
        PendingResourceManager manager = new PendingResourceManager();

        VDS host = new VDS();
        host.setId(Guid.newGuid());

        VM vm1 = new VM();
        vm1.setId(Guid.newGuid());

        VM vm2 = new VM();
        vm2.setId(Guid.newGuid());

        manager.addPending(new PendingVM(host, vm1));
        manager.addPending(new PendingMemory(host, vm1, 768));
        manager.addPending(new PendingCpuCores(host, vm1, 1));

        manager.addPending(new PendingVM(host, vm2));
        manager.addPending(new PendingMemory(host, vm2, 1024));
        manager.addPending(new PendingCpuCores(host, vm2, 10));

        Set<Guid> pending = PendingVM.collectForHost(manager, host.getId());
        assertEquals(2, pending.size());
        assertEquals(11, PendingCpuCores.collectForHost(manager, host.getId()));
        assertEquals(1024+768, PendingMemory.collectForHost(manager, host.getId()));
        assertEquals(host.getId(), PendingVM.getScheduledHost(manager, vm1));
        assertEquals(host.getId(), PendingVM.getScheduledHost(manager, vm2));
    }

    @Test
    public void testGetScheduledHost() {
        PendingResourceManager manager = new PendingResourceManager();

        VDS host1 = new VDS();
        host1.setId(Guid.newGuid());

        VDS host2 = new VDS();
        host2.setId(Guid.newGuid());

        VM vm1 = new VM();
        vm1.setId(Guid.newGuid());

        VM vm2 = new VM();
        vm2.setId(Guid.newGuid());

        manager.addPending(new PendingVM(host1, vm1));
        manager.addPending(new PendingMemory(host1, vm1, 768));
        manager.addPending(new PendingCpuCores(host1, vm1, 1));

        manager.addPending(new PendingVM(host2, vm2));
        manager.addPending(new PendingMemory(host2, vm2, 1024));
        manager.addPending(new PendingCpuCores(host2, vm2, 10));

        assertEquals(host1.getId(), PendingVM.getScheduledHost(manager, vm1));
        assertEquals(host2.getId(), PendingVM.getScheduledHost(manager, vm2));
    }

    @Test
    public void testGetPendingHostResources() {
        PendingResourceManager manager = new PendingResourceManager();

        VDS host1 = new VDS();
        host1.setId(Guid.newGuid());

        VDS host2 = new VDS();
        host2.setId(Guid.newGuid());

        VM vm1 = new VM();
        vm1.setId(Guid.newGuid());

        VM vm2 = new VM();
        vm2.setId(Guid.newGuid());

        VM vm3 = new VM();
        vm3.setId(Guid.newGuid());

        manager.addPending(new PendingVM(host1, vm1));
        manager.addPending(new PendingMemory(host1, vm1, 768));
        manager.addPending(new PendingCpuCores(host1, vm1, 1));

        manager.addPending(new PendingVM(host2, vm2));
        manager.addPending(new PendingMemory(host2, vm2, 1024));
        manager.addPending(new PendingCpuCores(host2, vm2, 10));

        manager.addPending(new PendingVM(host1, vm3));
        manager.addPending(new PendingMemory(host1, vm3, 1024));
        manager.addPending(new PendingCpuCores(host1, vm3, 10));


        List<PendingVM> vms = manager.pendingHostResources(host1.getId(), PendingVM.class);

        assertThat(vms)
                .hasSize(2)
                .extracting(PendingResource::getVm)
                .containsOnly(vm1.getId(), vm3.getId());
    }

    @Test
    public void testGetPendingVmResources() {
        PendingResourceManager manager = new PendingResourceManager();

        VDS host1 = new VDS();
        host1.setId(Guid.newGuid());

        VDS host2 = new VDS();
        host2.setId(Guid.newGuid());

        VM vm1 = new VM();
        vm1.setId(Guid.newGuid());

        VM vm2 = new VM();
        vm2.setId(Guid.newGuid());

        manager.addPending(new PendingVM(host1, vm1));
        manager.addPending(new PendingMemory(host1, vm1, 768));
        manager.addPending(new PendingCpuCores(host1, vm1, 1));

        manager.addPending(new PendingVM(host2, vm2));
        manager.addPending(new PendingMemory(host2, vm2, 1024));
        manager.addPending(new PendingCpuCores(host2, vm2, 10));

        List<PendingMemory> memories = manager.pendingVmResources(vm1.getId(), PendingMemory.class);

        assertThat(memories)
                .hasSize(1)
                .extracting(PendingMemory::getSizeInMb)
                .containsOnly(768L);
    }
}
