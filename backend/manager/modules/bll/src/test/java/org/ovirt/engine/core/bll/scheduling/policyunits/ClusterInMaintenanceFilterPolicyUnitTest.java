package org.ovirt.engine.core.bll.scheduling.policyunits;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.ovirt.engine.core.bll.scheduling.SchedulingContext;
import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.scheduling.PerHostMessages;
import org.ovirt.engine.core.compat.Guid;

public class ClusterInMaintenanceFilterPolicyUnitTest {
    private VDS host;
    private VM vm;
    private SchedulingContext context = new SchedulingContext(new Cluster(), Collections.emptyMap());

    @BeforeEach
    public void setUp() {
        host = new VDS();
        host.setId(Guid.newGuid());

        vm = new VM();
        vm.setId(Guid.newGuid());
        vm.setAutoStartup(false);
        vm.setRunOnVds(null);
    }

    @Test
    public void testHaCanStart() {
        vm.setAutoStartup(true);

        ClusterInMaintenanceFilterPolicyUnit unit = new ClusterInMaintenanceFilterPolicyUnit(null, null);
        List<VDS> result = unit.filter(context, Collections.singletonList(host), vm, new PerHostMessages());
        assertThat(result)
                .isNotNull()
                .isNotEmpty()
                .contains(host);
    }

    @Test
    public void testHaCanMigrate() {
        vm.setRunOnVds(Guid.newGuid());
        vm.setAutoStartup(true);

        ClusterInMaintenanceFilterPolicyUnit unit = new ClusterInMaintenanceFilterPolicyUnit(null, null);
        List<VDS> result = unit.filter(context, Collections.singletonList(host), vm, new PerHostMessages());
        assertThat(result)
                .isNotNull()
                .isNotEmpty()
                .contains(host);
    }

    @Test
    public void testNonHaCannotStart() {
        ClusterInMaintenanceFilterPolicyUnit unit = new ClusterInMaintenanceFilterPolicyUnit(null, null);
        final PerHostMessages messages = new PerHostMessages();
        List<VDS> result = unit.filter(context, Collections.singletonList(host), vm, messages);
        assertThat(result)
                .isNotNull()
                .isEmpty();
        assertThat(messages.getMessages())
                .isNotEmpty()
                .containsKeys(host.getId());
    }

    @Test
    public void testNonHaCanMigrate() {
        vm.setRunOnVds(Guid.newGuid());
        ClusterInMaintenanceFilterPolicyUnit unit = new ClusterInMaintenanceFilterPolicyUnit(null, null);
        List<VDS> result = unit.filter(context, Collections.singletonList(host), vm, new PerHostMessages());
        assertThat(result)
                .isNotNull()
                .isNotEmpty()
                .contains(host);
    }
}
