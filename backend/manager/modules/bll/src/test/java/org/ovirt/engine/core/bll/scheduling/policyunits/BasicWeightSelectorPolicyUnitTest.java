package org.ovirt.engine.core.bll.scheduling.policyunits;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.compat.Guid;

public class BasicWeightSelectorPolicyUnitTest {
    private Guid unit1;
    private Guid unit2;
    private Guid host1;
    private Guid host2;
    private Guid host3;
    private List<Guid> hosts;

    @BeforeEach
    public void setUp() {
        unit1 = Guid.newGuid();
        unit2 = Guid.newGuid();

        host1 = Guid.newGuid();
        host2 = Guid.newGuid();
        host3 = Guid.newGuid();

        hosts = new ArrayList<>();
        hosts.add(host1);
        hosts.add(host2);
        hosts.add(host3);
    }

    @Test
    public void testRawWeights() {
        BasicWeightSelectorPolicyUnit.Selector selector = new BasicWeightSelectorPolicyUnit.Selector();

        List<Pair<Guid, Integer>> units = new ArrayList<>();
        units.add(new Pair<Guid, Integer>(unit1, 1));
        units.add(new Pair<Guid, Integer>(unit2, 1));

        selector.init(units, hosts);

        selector.record(unit1, host1, 500000);
        selector.record(unit1, host2, 1000);
        selector.record(unit1, host3, 85366814);

        selector.record(unit2, host3, 50);
        selector.record(unit2, host1, 100);

        Guid best = selector.best().get();
        assertEquals(host2, best);
    }

    @Test
    public void testNormalizedWeights() {
        BasicWeightSelectorPolicyUnit.Selector selector = new BasicWeightSelectorPolicyUnit.Selector();

        List<Pair<Guid, Integer>> units = new ArrayList<>();
        units.add(new Pair<Guid, Integer>(unit1, 1));
        units.add(new Pair<Guid, Integer>(unit2, 100));

        selector.init(units, hosts);

        selector.record(unit1, host1, 50);
        selector.record(unit1, host2, 10);
        selector.record(unit1, host3, 200);

        selector.record(unit2, host2, 5);
        selector.record(unit2, host1, 10);

        Guid best = selector.best().get();
        assertEquals(host3, best);
    }

    @Test
    public void testNormalizedWeightsWithExternalScheduler() {
        BasicWeightSelectorPolicyUnit.Selector selector = new BasicWeightSelectorPolicyUnit.Selector();

        List<Pair<Guid, Integer>> units = new ArrayList<>();
        units.add(new Pair<Guid, Integer>(unit1, 1));
        units.add(new Pair<Guid, Integer>(unit2, 100));

        selector.init(units, hosts);

        // External scheduler result does not have unit Guid
        selector.record(null, host1, 50);
        selector.record(unit1, host2, 10);
        selector.record(unit1, host3, 200);

        selector.record(unit2, host2, 5);
        selector.record(unit2, host1, 10);

        Guid best = selector.best().get();
        assertEquals(host3, best);
    }
}
