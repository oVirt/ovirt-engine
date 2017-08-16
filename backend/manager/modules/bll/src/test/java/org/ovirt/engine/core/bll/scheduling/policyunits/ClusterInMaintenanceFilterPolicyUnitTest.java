package org.ovirt.engine.core.bll.scheduling.policyunits;

import static org.assertj.core.api.Assertions.assertThat;
import static org.ovirt.engine.core.utils.MockConfigRule.mockConfig;

import java.util.Collections;
import java.util.List;

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.scheduling.PerHostMessages;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.utils.MockConfigRule;

public class ClusterInMaintenanceFilterPolicyUnitTest {
    private VDS host;
    private VM vm;

    @ClassRule
    public static MockConfigRule configRule = new MockConfigRule(mockConfig(ConfigValues.MaxSchedulerWeight, 1000));

    @Before
    public void setUp() {
        host = new VDS();
        host.setId(Guid.newGuid());

        vm = new VM();
        vm.setId(Guid.newGuid());
        vm.setAutoStartup(false);
        vm.setRunOnVds(null);
    }

    @Test
    public void testHaCanStart() throws Exception {
        vm.setAutoStartup(true);

        ClusterInMaintenanceFilterPolicyUnit unit = new ClusterInMaintenanceFilterPolicyUnit(null, null);
        List<VDS> result = unit.filter(null, Collections.singletonList(host), vm, Collections.emptyMap(), new PerHostMessages());
        assertThat(result)
                .isNotNull()
                .isNotEmpty()
                .contains(host);
    }

    @Test
    public void testHaCanMigrate() throws Exception {
        vm.setRunOnVds(Guid.newGuid());
        vm.setAutoStartup(true);

        ClusterInMaintenanceFilterPolicyUnit unit = new ClusterInMaintenanceFilterPolicyUnit(null, null);
        List<VDS> result = unit.filter(null,
                Collections.singletonList(host), vm,
                Collections.emptyMap(), new PerHostMessages());
        assertThat(result)
                .isNotNull()
                .isNotEmpty()
                .contains(host);
    }

    @Test
    public void testNonHaCannotStart() throws Exception {
        ClusterInMaintenanceFilterPolicyUnit unit = new ClusterInMaintenanceFilterPolicyUnit(null, null);
        final PerHostMessages messages = new PerHostMessages();
        List<VDS> result = unit.filter(null,
                Collections.singletonList(host), vm,
                Collections.emptyMap(), messages);
        assertThat(result)
                .isNotNull()
                .isEmpty();
        assertThat(messages.getMessages())
                .isNotEmpty()
                .containsKeys(host.getId());
    }

    @Test
    public void testNonHaCanMigrate() throws Exception {
        vm.setRunOnVds(Guid.newGuid());
        ClusterInMaintenanceFilterPolicyUnit unit = new ClusterInMaintenanceFilterPolicyUnit(null, null);
        List<VDS> result = unit.filter(null,
                Collections.singletonList(host), vm,
                Collections.emptyMap(), new PerHostMessages());
        assertThat(result)
                .isNotNull()
                .isNotEmpty()
                .contains(host);
    }
}
