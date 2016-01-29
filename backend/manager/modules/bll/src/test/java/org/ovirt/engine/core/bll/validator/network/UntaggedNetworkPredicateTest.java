package org.ovirt.engine.core.bll.validator.network;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.function.Predicate;

import org.junit.Before;
import org.junit.Test;

public class UntaggedNetworkPredicateTest {

    private Predicate<NetworkType> underTest;

    @Before
    public void setUp() throws Exception {
        underTest = new UntaggedNetworkPredicate();
    }

    @Test
    public void testEvaluateVm() {
        assertTrue(underTest.test(NetworkType.VM));
    }

    @Test
    public void testEvaluateNonVm() {
        assertTrue(underTest.test(NetworkType.NON_VM));
    }

    @Test
    public void testEvaluateVlan() {
        assertFalse(underTest.test(NetworkType.VLAN));
    }
}
