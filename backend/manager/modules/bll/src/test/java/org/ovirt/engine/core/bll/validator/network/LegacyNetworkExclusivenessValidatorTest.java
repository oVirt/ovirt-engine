package org.ovirt.engine.core.bll.validator.network;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.ovirt.engine.core.bll.validator.network.NetworkType.NON_VM;
import static org.ovirt.engine.core.bll.validator.network.NetworkType.VLAN;
import static org.ovirt.engine.core.bll.validator.network.NetworkType.VM;
import static org.ovirt.engine.core.common.errors.EngineMessage.NETWORK_INTERFACES_NOT_EXCLUSIVELY_USED_BY_NETWORK;

import java.util.Arrays;
import java.util.Collections;

import org.junit.Before;
import org.junit.Test;

public class LegacyNetworkExclusivenessValidatorTest {

    private NetworkExclusivenessValidator underTest;

    @Before
    public void setUp() throws Exception {
        underTest = new LegacyNetworkExclusivenessValidator();
    }

    @Test
    public void testIsNetworkExclusiveValidSingleVm() {
        assertTrue(underTest.isNetworkExclusive(Collections.singletonList(VM)));
    }

    @Test
    public void testIsNetworkExclusiveValidSingleNonVm() {
        assertTrue(underTest.isNetworkExclusive(Collections.singletonList(NON_VM)));
    }

    @Test
    public void testIsNetworkExclusiveInvalidMultiVm() {
        assertFalse(underTest.isNetworkExclusive(Arrays.asList(VM, VM)));
    }

    @Test
    public void testIsNetworkExclusiveInvalidVmWithNonVm() {
        assertFalse(underTest.isNetworkExclusive(Arrays.asList(VM, NON_VM)));
    }

    @Test
    public void testIsNetworkExclusiveInvalidVmWithVlan() {
        assertFalse(underTest.isNetworkExclusive(Arrays.asList(VM, VLAN)));
    }

    @Test
    public void testIsNetworkExclusiveInvalidMultipleNonVm() {
        assertFalse(underTest.isNetworkExclusive(Arrays.asList(NON_VM, NON_VM)));
    }

    @Test
    public void testIsNetworkExclusiveValidVlan() {
        assertTrue(underTest.isNetworkExclusive(Collections.singletonList(VLAN)));
    }

    @Test
    public void testIsNetworkExclusiveValidNonVlanWithVlan() {
        assertTrue(underTest.isNetworkExclusive(Arrays.asList(NON_VM, VLAN)));
    }

    @Test
    public void testIsNetworkExclusiveValidMultiVlan() {
        assertTrue(underTest.isNetworkExclusive(Arrays.asList(VLAN, VLAN)));
    }

    @Test
    public void testGetViolationMessage() {
        assertThat(underTest.getViolationMessage(), is(NETWORK_INTERFACES_NOT_EXCLUSIVELY_USED_BY_NETWORK));
    }
}
