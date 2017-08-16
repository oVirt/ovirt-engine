package org.ovirt.engine.core.bll.scheduling.policyunits;

import static org.assertj.core.api.Assertions.assertThat;
import static org.ovirt.engine.core.utils.MockConfigRule.mockConfig;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.ovirt.engine.core.bll.scheduling.pending.PendingHugePages;
import org.ovirt.engine.core.bll.scheduling.pending.PendingResourceManager;
import org.ovirt.engine.core.common.businessentities.HugePage;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.scheduling.PerHostMessages;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.utils.MockConfigRule;

public class HugePagesFilterPolicyUnitTest {
    VDS host1;
    VM vm;
    VM otherVm;

    PendingResourceManager pendingResourceManager;

    @ClassRule
    public static MockConfigRule configRule = new MockConfigRule(mockConfig(ConfigValues.MaxSchedulerWeight, 1000));

    @Before
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
    public void testNoHugePages() throws Exception {
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
    public void testHugePagesNotPresentOnHost() throws Exception {
        vm.setCustomProperties("hugepages=1024");

        HugePagesFilterPolicyUnit unit = new HugePagesFilterPolicyUnit(null, pendingResourceManager);
        List<VDS> hosts = unit.filter(null,
                Collections.singletonList(host1),
                vm, Collections.emptyMap(), new PerHostMessages());

        assertThat(hosts)
                .isEmpty();
    }

    @Test
    public void testHugePagesWrongSizeOnHost() throws Exception {
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
    public void testHugePagesGoodAndWrongSizeOnHost() throws Exception {
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
    public void testNotEnoughFreeHugePagesOnHost() throws Exception {
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
    public void testNotEnoughFreeHugePagesOnHostPending() throws Exception {
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
    public void testEnoughFreeHugePagesOnHostBadSizePending() throws Exception {
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
    public void testEnoughFreeHugePagesOnHostBadSizeAvailableAndPending() throws Exception {
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
    public void testEnoughFreeHugePagesOnHostSimple() throws Exception {
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
