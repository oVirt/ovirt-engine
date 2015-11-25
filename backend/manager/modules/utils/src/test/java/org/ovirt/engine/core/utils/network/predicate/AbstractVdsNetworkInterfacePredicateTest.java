package org.ovirt.engine.core.utils.network.predicate;

import java.util.function.Predicate;

import org.junit.Assert;
import org.junit.Test;
import org.ovirt.engine.core.common.businessentities.network.VdsNetworkInterface;

abstract public class AbstractVdsNetworkInterfacePredicateTest {

    private static final String VALID = "VALID";
    private static final String INVALID = "INVALID";
    private Predicate<VdsNetworkInterface> underTest;

    protected String getVALID() {
        return VALID;
    }

    protected String getINVALID() {
        return INVALID;
    }

    protected void setUnderTest(Predicate<VdsNetworkInterface> underTest) {
        this.underTest = underTest;
    }

    abstract public VdsNetworkInterface generateVdsNetworkInterface(String value);

    @Test
    public void checkNullAddress() {
        Assert.assertFalse(underTest.test(generateVdsNetworkInterface(null)));
    }

    @Test
    public void checkInvalidAddress() {
        Assert.assertFalse(underTest.test(generateVdsNetworkInterface(getINVALID())));
    }

    @Test
    public void checkValidAddress() {
        Assert.assertTrue(underTest.test(generateVdsNetworkInterface(getVALID())));
    }

}
