package org.ovirt.engine.core.utils.network.predicate;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.function.Predicate;

import org.junit.jupiter.api.Test;
import org.ovirt.engine.core.common.businessentities.network.VdsNetworkInterface;

public abstract class AbstractVdsNetworkInterfacePredicateTest {

    protected static final String VALID = "VALID";
    private static final String INVALID = "INVALID";

    private Predicate<VdsNetworkInterface> underTest;

    protected void setUnderTest(Predicate<VdsNetworkInterface> underTest) {
        this.underTest = underTest;
    }

    private VdsNetworkInterface generateVdsNetworkInterface(String value) {
        VdsNetworkInterface iface = new VdsNetworkInterface();
        setIfaceProperty(iface, value);
        return iface;
    }

    protected abstract void setIfaceProperty(VdsNetworkInterface iface, String value);

    @Test
    public void checkNullAddress() {
        assertFalse(underTest.test(generateVdsNetworkInterface(null)));
    }

    @Test
    public void checkInvalidAddress() {
        assertFalse(underTest.test(generateVdsNetworkInterface(INVALID)));
    }

    @Test
    public void checkValidAddress() {
        assertTrue(underTest.test(generateVdsNetworkInterface(VALID)));
    }
}
