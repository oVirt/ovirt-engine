package org.ovirt.engine.core.bll.validator.network;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;
import static org.ovirt.engine.core.bll.validator.network.NetworkType.VLAN;
import static org.ovirt.engine.core.bll.validator.network.NetworkType.VM;
import static org.ovirt.engine.core.common.errors.EngineMessage.NETWORK_INTERFACES_NOT_EXCLUSIVELY_USED_BY_UNTAGGED_NETWORK;

import java.util.Arrays;
import java.util.Collections;
import java.util.function.Predicate;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class VlanUntaggedNetworkExclusivenessValidatorTest {

    private NetworkExclusivenessValidator underTest;

    @Mock
    private Predicate<NetworkType> mockUntaggedNetworkPredicate;

    @Before
    public void setUp() throws Exception {
        underTest = new VlanUntaggedNetworkExclusivenessValidator(mockUntaggedNetworkPredicate);

        when(mockUntaggedNetworkPredicate.test(VLAN)).thenReturn(false);
        when(mockUntaggedNetworkPredicate.test(VM)).thenReturn(true);
    }

    @Test
    public void testIsNetworkExclusiveInvalid() {
        assertFalse(underTest.isNetworkExclusive(Arrays.asList(VM, VLAN, VLAN, VM)));
    }

    @Test
    public void testIsNetworkExclusiveValid1() {
        assertTrue(underTest.isNetworkExclusive(Arrays.asList(VLAN, VLAN, VM, VLAN)));
    }

    @Test
    public void testIsNetworkExclusiveValid2() {
        assertTrue(underTest.isNetworkExclusive(Collections.singletonList(VM)));
    }

    @Test
    public void testIsNetworkExclusiveValid3() {
        assertTrue(underTest.isNetworkExclusive(Collections.singletonList(VLAN)));
    }

    @Test
    public void testGetViolationMessage() {
        assertThat(underTest.getViolationMessage(), is(NETWORK_INTERFACES_NOT_EXCLUSIVELY_USED_BY_UNTAGGED_NETWORK));
    }
}
