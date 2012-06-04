package org.ovirt.engine.core.bll;

import junit.framework.Assert;

import org.junit.Test;

public class VmManagementCommandBaseTest {
    @Test
    public void isCpuPinningValid() {
        Assert.assertTrue("null value must be accepted", VmManagementCommandBase.isCpuPinningValid(null));

        Assert.assertTrue("empty string must be accepted", VmManagementCommandBase.isCpuPinningValid(""));

        Assert.assertFalse(VmManagementCommandBase.isCpuPinningValid("intentionally invalid"));

        Assert.assertTrue(VmManagementCommandBase.isCpuPinningValid("0#0"));
        Assert.assertTrue(VmManagementCommandBase.isCpuPinningValid("0#1-4"));
        Assert.assertTrue(VmManagementCommandBase.isCpuPinningValid("0#^3"));
        Assert.assertTrue(VmManagementCommandBase.isCpuPinningValid("0#^3,^2"));
        Assert.assertTrue(VmManagementCommandBase.isCpuPinningValid("0#1-8,^6"));
        Assert.assertTrue(VmManagementCommandBase.isCpuPinningValid("0#1-8,^6,^7"));
        Assert.assertTrue(VmManagementCommandBase.isCpuPinningValid("0#1-8,^6,^7"));
        Assert.assertTrue(VmManagementCommandBase.isCpuPinningValid("0#1-8,^5,^6,^7"));
        Assert.assertTrue(VmManagementCommandBase.isCpuPinningValid("0#1,2,3"));
        Assert.assertTrue(VmManagementCommandBase.isCpuPinningValid("0#^1,^2,^3"));
        Assert.assertTrue(VmManagementCommandBase.isCpuPinningValid("0#1-4,6-8"));
        Assert.assertTrue(VmManagementCommandBase.isCpuPinningValid("0#1-4,6-8,9-12"));
        Assert.assertTrue(VmManagementCommandBase.isCpuPinningValid("0#1-4,^3,9-12,^10"));

        Assert.assertTrue(VmManagementCommandBase.isCpuPinningValid("0#0_1#1"));
        Assert.assertTrue(VmManagementCommandBase.isCpuPinningValid("0#1-2_1#1-2"));
        Assert.assertTrue(VmManagementCommandBase.isCpuPinningValid("0#1,2,3_1#2,3"));
        Assert.assertTrue(VmManagementCommandBase.isCpuPinningValid("0#1,2,3_1#1-4,^3"));
    }
}
