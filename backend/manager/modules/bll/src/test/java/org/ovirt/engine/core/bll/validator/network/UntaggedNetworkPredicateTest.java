package org.ovirt.engine.core.bll.validator.network;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.function.Predicate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class UntaggedNetworkPredicateTest {

    private Predicate<NetworkType> underTest;

    @BeforeEach
    public void setUp() {
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
