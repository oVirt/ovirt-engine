package org.ovirt.engine.core.bll.network.predicate;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.when;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.ovirt.engine.core.bll.network.macpool.MacPool;
import org.ovirt.engine.core.common.businessentities.network.VmNetworkInterface;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class VnicWithBadMacPredicateTest {

    private static final String MAC_ADDRESS = "mac address";
    private VnicWithBadMacPredicate underTest;

    @Mock
    private MacPool mockMacPool;

    private VmNetworkInterface vnic;

    @BeforeEach
    public void setUp() {
        vnic = new VmNetworkInterface();
        vnic.setMacAddress(MAC_ADDRESS);
        underTest = new VnicWithBadMacPredicate(mockMacPool);
    }

    @Test
    public void emptyMacIsNotBad() {
        vnic.setMacAddress(null);
        assertFalse(underTest.test(vnic));
    }

    @Test
    public void validMac() {
        doTest(false, false, true, false);
    }

    @Test
    public void macInUse() {
        doTest(false, true, true, true);
    }

    @Test
    public void macInUseAndDuplicatesAllowed() {
        doTest(true, true, true, false);
    }

    @Test
    public void macOutOfRange() {
        doTest(true, true, false, true);
    }

    private void doTest(boolean duplicatesAllowed, boolean macInUse, boolean macInRange, boolean result) {
        prepareMacPool(duplicatesAllowed, macInUse, macInRange);

        assertThat(underTest.test(vnic), Matchers.is(result));
    }

    private void prepareMacPool(boolean duplicatesAllowed, boolean macInUse, boolean macInRange) {
        when(mockMacPool.isDuplicateMacAddressesAllowed()).thenReturn(duplicatesAllowed);
        when(mockMacPool.isMacInUse(MAC_ADDRESS)).thenReturn(macInUse);
        when(mockMacPool.isMacInRange(MAC_ADDRESS)).thenReturn(macInRange);
    }

}
