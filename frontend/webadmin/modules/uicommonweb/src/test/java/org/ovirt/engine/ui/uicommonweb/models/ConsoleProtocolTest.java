package org.ovirt.engine.ui.uicommonweb.models;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;

import org.junit.jupiter.api.Test;

public class ConsoleProtocolTest {

    private ConsoleProtocol.PriorityComparator cpComparator = new ConsoleProtocol.PriorityComparator();

    @Test
    public void testGetProtocolsByPriority() {
        assertThat(ConsoleProtocol.getProtocolsByPriority(),
                is(Arrays.asList(new ConsoleProtocol[]{ConsoleProtocol.RDP, ConsoleProtocol.VNC, ConsoleProtocol.SPICE})));
    }

    @Test
    public void testComparatorSameProtocols() {
        int compared = cpComparator.compare(ConsoleProtocol.SPICE, ConsoleProtocol.SPICE);
        assertThat(compared, is(0));
    }

    @Test
    public void testComparatorLowerPriorityFst() {
        int compared = cpComparator.compare(ConsoleProtocol.RDP, ConsoleProtocol.VNC);
        assertTrue(compared < 0);
    }

    @Test
    public void testComparatorHigherPriorityFst() {
        int compared = cpComparator.compare(ConsoleProtocol.SPICE, ConsoleProtocol.RDP);
        assertTrue(compared > 0);
    }

    @Test
    public void testComparatorNullFst() {
        int compared = cpComparator.compare(null, ConsoleProtocol.RDP);
        assertTrue(compared < 0);
    }

}
