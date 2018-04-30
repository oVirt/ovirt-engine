package org.ovirt.engine.core.bll.scheduling.policyunits;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.ovirt.engine.core.bll.scheduling.pending.PendingHugePages;
import org.ovirt.engine.core.bll.scheduling.pending.PendingResourceManager;
import org.ovirt.engine.core.common.businessentities.HugePage;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.scheduling.PerHostMessages;
import org.ovirt.engine.core.compat.Guid;

public class HugePagesFilterPolicyUnitTest {
    VDS host1;
    VM vm;
    VM otherVm;

    PendingResourceManager pendingResourceManager;

    @BeforeEach
    public void setUp() {
        host1 = new VDS();
        host1.setId(Guid.newGuid());
        host1.setHugePages(new ArrayList<>());

        vm = new VM();
        vm.setId(Guid.newGuid());
        vm.setVmMemSizeMb(1024);

        otherVm = new VM();
        otherVm.setId(Guid.newGuid());

        pendingResourceManager = new PendingResourceManager();
    }

    @Test
    public void testNoHugePages() {
        HugePagesFilterPolicyUnit unit = new HugePagesFilterPolicyUnit(null, pendingResourceManager);
        List<VDS> hosts = unit.filter(null,
                Collections.singletonList(host1),
                vm, Collections.emptyMap(), new PerHostMessages());

        assertThat(hosts)
                .isNotNull()
                .isNotEmpty()
                .contains(host1);
    }

    @Test
    public void testHugePagesNotPresentOnHost() {
        vm.setCustomProperties("hugepages=1024");

        HugePagesFilterPolicyUnit unit = new HugePagesFilterPolicyUnit(null, pendingResourceManager);
        List<VDS> hosts = unit.filter(null,
                Collections.singletonList(host1),
                vm, Collections.emptyMap(), new PerHostMessages());

        assertThat(hosts)
                .isEmpty();
    }

    @Test
    public void testHugePagesWrongSizeOnHost() {
        vm.setCustomProperties("hugepages=1024");

        host1.setHugePages(Collections.singletonList(new HugePage(2048, 50)));

        HugePagesFilterPolicyUnit unit = new HugePagesFilterPolicyUnit(null, pendingResourceManager);
        List<VDS> hosts = unit.filter(null,
                Collections.singletonList(host1),
                vm, Collections.emptyMap(), new PerHostMessages());

        assertThat(hosts)
                .isEmpty();
    }

    @Test
    public void testHugePagesGoodAndWrongSizeOnHost() {
        vm.setCustomProperties("hugepages=1024");

        host1.setHugePages(Arrays.asList(
                new HugePage(1024, 5),
                new HugePage(2048, 50)));

        HugePagesFilterPolicyUnit unit = new HugePagesFilterPolicyUnit(null, pendingResourceManager);
        List<VDS> hosts = unit.filter(null,
                Collections.singletonList(host1),
                vm, Collections.emptyMap(), new PerHostMessages());

        assertThat(hosts)
                .isEmpty();
    }

    @Test
    public void testNotEnoughFreeHugePagesOnHost() {
        vm.setCustomProperties("hugepages=1024");

        host1.setHugePages(Collections.singletonList(new HugePage(1024, 50)));

        HugePagesFilterPolicyUnit unit = new HugePagesFilterPolicyUnit(null, pendingResourceManager);
        List<VDS> hosts = unit.filter(null,
                Collections.singletonList(host1),
                vm, Collections.emptyMap(), new PerHostMessages());

        assertThat(hosts)
                .isEmpty();
    }

    @Test
    public void testNotEnoughFreeHugePagesOnHostPending() {
        vm.setCustomProperties("hugepages=1024");

        host1.setHugePages(Collections.singletonList(new HugePage(1024, 1050)));


        pendingResourceManager.addPending(new PendingHugePages(host1, otherVm, 1024, 50));

        HugePagesFilterPolicyUnit unit = new HugePagesFilterPolicyUnit(null, pendingResourceManager);
        List<VDS> hosts = unit.filter(null,
                Collections.singletonList(host1),
                vm, Collections.emptyMap(), new PerHostMessages());

        assertThat(hosts)
                .isEmpty();
    }

    @Test
    public void testEnoughFreeHugePagesOnHostBadSizePending() {
        vm.setCustomProperties("hugepages=1024");

        host1.setHugePages(Collections.singletonList(new HugePage(1024, 1050)));


        pendingResourceManager.addPending(new PendingHugePages(host1, otherVm, 2048, 1024));

        HugePagesFilterPolicyUnit unit = new HugePagesFilterPolicyUnit(null, pendingResourceManager);
        List<VDS> hosts = unit.filter(null,
                Collections.singletonList(host1),
                vm, Collections.emptyMap(), new PerHostMessages());

        assertThat(hosts)
                .isNotEmpty()
                .contains(host1);
    }

    @Test
    public void testEnoughFreeHugePagesOnHostBadSizeAvailableAndPending() {
        vm.setCustomProperties("hugepages=1024");

        host1.setHugePages(Arrays.asList(
                new HugePage(1024, 1050),
                new HugePage(2048, 1025)));


        pendingResourceManager.addPending(new PendingHugePages(host1, otherVm, 2048, 1024));

        HugePagesFilterPolicyUnit unit = new HugePagesFilterPolicyUnit(null, pendingResourceManager);
        List<VDS> hosts = unit.filter(null,
                Collections.singletonList(host1),
                vm, Collections.emptyMap(), new PerHostMessages());

        assertThat(hosts)
                .isNotEmpty()
                .contains(host1);
    }

    @Test
    public void testEnoughFreeHugePagesOnHostSimple() {
        vm.setCustomProperties("hugepages=1024");

        host1.setHugePages(Collections.singletonList(new HugePage(1024, 1024)));

        HugePagesFilterPolicyUnit unit = new HugePagesFilterPolicyUnit(null, pendingResourceManager);
        List<VDS> hosts = unit.filter(null,
                Collections.singletonList(host1),
                vm, Collections.emptyMap(), new PerHostMessages());

        assertThat(hosts)
                .isNotEmpty()
                .contains(host1);
    }
}
