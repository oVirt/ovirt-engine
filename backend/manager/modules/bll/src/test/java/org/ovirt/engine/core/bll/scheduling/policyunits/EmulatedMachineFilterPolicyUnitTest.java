package org.ovirt.engine.core.bll.scheduling.policyunits;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.ovirt.engine.core.bll.scheduling.SchedulingContext;
import org.ovirt.engine.core.common.businessentities.BiosType;
import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.scheduling.PerHostMessages;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.Version;

@ExtendWith(MockitoExtension.class)
class EmulatedMachineFilterPolicyUnitTest {

    @InjectMocks
    EmulatedMachineFilterPolicyUnit unit = new EmulatedMachineFilterPolicyUnit(null, null);

    private Cluster cluster;
    private SchedulingContext context;
    private VM vm;
    private VDS el9Host;
    private VDS el10Host;
    private List<VDS> hosts;

    @BeforeEach
    public void setUp() {
        cluster = new Cluster();
        cluster.setId(Guid.newGuid());
        // these tests exercise the strict >= 4.8 rules (Q35-only default, unpinned i440fx rejected)
        cluster.setCompatibilityVersion(Version.v4_8);
        cluster.setEmulatedMachine("pc-q35-rhel9.6.0");

        context = new SchedulingContext(cluster, Collections.emptyMap());

        vm = new VM();
        vm.setId(Guid.newGuid());
        vm.setClusterId(cluster.getId());

        el9Host = new VDS();
        el9Host.setId(Guid.newGuid());
        el9Host.setClusterId(cluster.getId());
        el9Host.setSupportedEmulatedMachines("pc-i440fx-rhel7.6.0,pc-q35-rhel9.6.0");

        el10Host = new VDS();
        el10Host.setId(Guid.newGuid());
        el10Host.setClusterId(cluster.getId());
        el10Host.setSupportedEmulatedMachines("pc-q35-rhel9.6.0,pc-q35-rhel10.0.0,pc-i440fx-rhel10.0.0");

        hosts = Arrays.asList(el9Host, el10Host);
    }

    @Test
    public void testFilterCustomEmulatedMachineMatchesEl9HostOnly() {
        vm.setCustomEmulatedMachine("pc-i440fx-rhel7.6.0");

        List<VDS> result = unit.filter(context, hosts, vm, new PerHostMessages());

        assertThat(result).containsExactly(el9Host);
    }

    @Test
    public void testFilterCustomEmulatedMachineMatchesEl10HostOnly() {
        vm.setCustomEmulatedMachine("pc-i440fx-rhel10.0.0");

        List<VDS> result = unit.filter(context, hosts, vm, new PerHostMessages());

        assertThat(result).containsExactly(el10Host);
    }

    @Test
    public void testFilterQ35VmMatchesBothHosts() {
        vm.setCustomEmulatedMachine("pc-q35-rhel9.6.0");

        List<VDS> result = unit.filter(context, hosts, vm, new PerHostMessages());

        assertThat(result).containsExactlyInAnyOrder(el9Host, el10Host);
    }

    @Test
    public void testFilterQ35VmNullCustomEmulatedMachineUsesClusterDefault() {
        // Q35 VM without customEmulatedMachine inherits from cluster default
        vm.setBiosType(BiosType.Q35_SEA_BIOS);

        List<VDS> result = unit.filter(context, hosts, vm, new PerHostMessages());

        assertThat(result).containsExactlyInAnyOrder(el9Host, el10Host);
    }

    @Test
    public void testFilterI440fxVmNullCustomEmulatedMachineNoHosts() {
        // i440fx VM without customEmulatedMachine cannot run in Q35-only cluster
        vm.setBiosType(BiosType.I440FX_SEA_BIOS);

        List<VDS> result = unit.filter(context, hosts, vm, new PerHostMessages());

        assertThat(result).isEmpty();
    }

    @Test
    public void testFilterI440fxVmWithCustomEmulatedMachineMatchesEl9Only() {
        // i440fx VM with explicit customEmulatedMachine matching EL9 host
        vm.setBiosType(BiosType.I440FX_SEA_BIOS);
        vm.setCustomEmulatedMachine("pc-i440fx-rhel7.6.0");

        List<VDS> result = unit.filter(context, hosts, vm, new PerHostMessages());

        assertThat(result).containsExactly(el9Host);
    }

    @Test
    public void testFilterI440fxVmWithCustomEmulatedMachineMatchesEl10Only() {
        // i440fx VM with explicit customEmulatedMachine matching EL10 host
        vm.setBiosType(BiosType.I440FX_SEA_BIOS);
        vm.setCustomEmulatedMachine("pc-i440fx-rhel10.0.0");

        List<VDS> result = unit.filter(context, hosts, vm, new PerHostMessages());

        assertThat(result).containsExactly(el10Host);
    }

    // --- below 4.8: the dual cluster default is a legitimate state ---

    private VDS hostWithMachines(String machines) {
        VDS host = new VDS();
        host.setId(Guid.newGuid());
        host.setClusterId(cluster.getId());
        host.setSupportedEmulatedMachines(machines);
        return host;
    }

    @Test
    public void testFilterBelow48UnpinnedI440fxVmResolvesDualPart() {
        cluster.setCompatibilityVersion(Version.v4_7);
        cluster.setEmulatedMachine("pc-i440fx-rhel7.6.0;pc-q35-rhel9.6.0");
        vm.setBiosType(BiosType.I440FX_SEA_BIOS);

        VDS el9Only = hostWithMachines("pc-i440fx-rhel7.6.0,pc-q35-rhel9.6.0");
        VDS el10Only = hostWithMachines("pc-q35-rhel9.6.0,pc-q35-rhel10.0.0");

        List<VDS> result = unit.filter(context, Arrays.asList(el9Only, el10Only), vm, new PerHostMessages());

        assertThat(result).containsExactly(el9Only);
    }

    @Test
    public void testFilterBelow48UnpinnedQ35VmResolvesDualPart() {
        cluster.setCompatibilityVersion(Version.v4_7);
        cluster.setEmulatedMachine("pc-i440fx-rhel7.6.0;pc-q35-rhel9.6.0");
        vm.setBiosType(BiosType.Q35_SEA_BIOS);

        VDS el9Only = hostWithMachines("pc-i440fx-rhel7.6.0,pc-q35-rhel9.6.0");
        VDS el10Only = hostWithMachines("pc-q35-rhel9.6.0,pc-q35-rhel10.0.0");

        List<VDS> result = unit.filter(context, Arrays.asList(el9Only, el10Only), vm, new PerHostMessages());

        assertThat(result).containsExactlyInAnyOrder(el9Only, el10Only);
    }

    @Test
    public void testFilterBelow48UnpinnedI440fxVmOnQ35OnlyDefaultRejected() {
        // L2: the resolved default is chipset-checked — an unpinned i440fx VM in a
        // single-Q35 default cluster has no machine type to run.
        cluster.setCompatibilityVersion(Version.v4_7);
        cluster.setEmulatedMachine("pc-q35-rhel9.6.0");
        vm.setBiosType(BiosType.I440FX_SEA_BIOS);

        List<VDS> result = unit.filter(context, hosts, vm, new PerHostMessages());

        assertThat(result).isEmpty();
    }

    @Test
    public void testFilterBelow48UnpinnedQ35VmOnI440fxOnlyDefaultRejected() {
        cluster.setCompatibilityVersion(Version.v4_7);
        cluster.setEmulatedMachine("pc-i440fx-rhel7.6.0");
        vm.setBiosType(BiosType.Q35_SEA_BIOS);

        List<VDS> result = unit.filter(context, hosts, vm, new PerHostMessages());

        assertThat(result).isEmpty();
    }

    @Test
    public void testFilterBelow48LegacyVmWithoutBiosTypeResolvesI440fxPart() {
        // Legacy VMs (biosType == null) run i440fx hardware (chipsetMatches convention).
        cluster.setCompatibilityVersion(Version.v4_7);
        cluster.setEmulatedMachine("pc-i440fx-rhel7.6.0;pc-q35-rhel9.6.0");

        VDS el9Only = hostWithMachines("pc-i440fx-rhel7.6.0,pc-q35-rhel9.6.0");
        VDS el10Only = hostWithMachines("pc-q35-rhel9.6.0,pc-q35-rhel10.0.0");

        List<VDS> result = unit.filter(context, Arrays.asList(el9Only, el10Only), vm, new PerHostMessages());

        assertThat(result).containsExactly(el9Only);
    }
}
