package org.ovirt.engine.core.bll.scheduling.policyunits;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.ovirt.engine.core.bll.scheduling.SchedulingContext;
import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.compat.Guid;

public class PreferredHostsWeightPolicyUnitTest {
    @Test
    public void testHostPreference() {
        PreferredHostsWeightPolicyUnit unit = new PreferredHostsWeightPolicyUnit(null, null);

        VDS host1 = new VDS();
        host1.setId(Guid.newGuid());

        VDS host2 = new VDS();
        host2.setId(Guid.newGuid());

        VDS host3 = new VDS();
        host3.setId(Guid.newGuid());

        Cluster cluster = new Cluster();
        cluster.setId(Guid.newGuid());

        VM vm = new VM();
        vm.setId(Guid.newGuid());
        vm.setDedicatedVmForVdsList(Collections.singletonList(host2.getId()));

        List<VDS> hosts = new ArrayList<>();
        hosts.add(host1);
        hosts.add(host2);
        hosts.add(host3);

        List<Pair<Guid, Integer>> weights = unit.score(new SchedulingContext(cluster, Collections.emptyMap()), hosts, vm);

        Map<Guid, Integer> results = new HashMap<>();
        for (Pair<Guid, Integer> r: weights) {
            results.put(r.getFirst(), r.getSecond());
        }

        assertEquals(0, (long)results.get(host2.getId()));
        assertNotEquals(0, (long) results.get(host1.getId()));
        assertNotEquals(0, (long) results.get(host3.getId()));
    }
}
