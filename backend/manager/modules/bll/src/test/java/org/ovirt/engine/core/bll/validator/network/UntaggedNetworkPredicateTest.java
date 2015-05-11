package org.ovirt.engine.core.bll.validator.network;

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;

import org.apache.commons.collections.Predicate;
import org.junit.Before;
import org.junit.Test;

public class UntaggedNetworkPredicateTest {

    private Predicate underTest;

    @Before
    public void setUp() throws Exception {
        underTest = new UntaggedNetworkPredicate();
    }

    @Test
    public void testEvaluateVm() {
        assertTrue(underTest.evaluate(NetworkType.VM));
    }

    @Test
    public void testEvaluateNonVm() {
        assertTrue(underTest.evaluate(NetworkType.NON_VM));
    }

    @Test
    public void testEvaluateVlan() {
        assertFalse(underTest.evaluate(NetworkType.VLAN));
    }
}
