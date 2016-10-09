package org.ovirt.engine.core.bll.network.predicate;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.ovirt.engine.core.bll.network.macpool.MacPool;
import org.ovirt.engine.core.common.businessentities.network.VmNetworkInterface;

@RunWith(MockitoJUnitRunner.class)
public class VnicWithBadMacPredicateTest {

    private static final String MAC_ADDRESS = "mac address";
    private VnicWithBadMacPredicate underTest;

    @Mock
    private MacPool mockMacPool;

    private VmNetworkInterface vnic;

    @Before
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
