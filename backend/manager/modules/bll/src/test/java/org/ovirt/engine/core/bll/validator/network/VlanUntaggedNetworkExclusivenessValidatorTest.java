package org.ovirt.engine.core.bll.validator.network;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;
import static org.ovirt.engine.core.bll.validator.network.NetworkType.VLAN;
import static org.ovirt.engine.core.bll.validator.network.NetworkType.VM;
import static org.ovirt.engine.core.common.errors.EngineMessage.NETWORK_INTERFACES_NOT_EXCLUSIVELY_USED_BY_UNTAGGED_NETWORK;

import java.util.Arrays;
import java.util.Collections;
import java.util.function.Predicate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class VlanUntaggedNetworkExclusivenessValidatorTest {

    private NetworkExclusivenessValidator underTest;

    @Mock
    private Predicate<NetworkType> mockUntaggedNetworkPredicate;

    @BeforeEach
    public void setUp() {
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
