package org.ovirt.engine.core.bll;

import java.util.ArrayList;

import org.junit.Assert;

import org.junit.Test;

import org.ovirt.engine.core.common.action.VmManagementParametersBase;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VmStatic;
import org.ovirt.engine.core.common.errors.VdcBllMessages;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.Version;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;


public class VmManagementCommandBaseTest {

    @Test
    public void isCpuPinningValidWithoutPinnedHost() {
        VmManagementCommandBase<VmManagementParametersBase> test =
                spy(new VmManagementCommandBase<VmManagementParametersBase>(Guid.Empty));
        VmStatic vmStatic = new VmStatic();
        vmStatic.setNumOfSockets(6);
        vmStatic.setCpuPerSocket(2);
        vmStatic.setDedicatedVmForVds(null);
        Assert.assertFalse(test.isCpuPinningValid("0#0", vmStatic));
        Assert.assertFalse(test.getReturnValue()
                .getCanDoActionMessages()
                .contains(VdcBllMessages.ACTION_TYPE_FAILED_VM_CANNOT_BE_PINNED_TO_CPU_WITH_UNDEFINED_HOST));
    }

    @Test
    public void isCpuPinningValid() {
        VmManagementCommandBase<VmManagementParametersBase> test =
                spy(new VmManagementCommandBase<VmManagementParametersBase>(Guid.Empty));
        VmStatic vmStatic = new VmStatic();
        vmStatic.setNumOfSockets(6);
        vmStatic.setCpuPerSocket(2);
        vmStatic.setDedicatedVmForVds(Guid.Empty);
        final VDS dedicatedVds = new VDS();
        dedicatedVds.setCpuThreads(16);
        dedicatedVds.setOnlineCpus("0,1,2,3,4,5,6,7,8,9,10,11,12,13,14,15");
        dedicatedVds.setVdsGroupCompatibilityVersion(Version.v3_2);

        doReturn(dedicatedVds).when(test).getVds(Guid.Empty);



        Assert.assertTrue("null value must be accepted",
                test.isCpuPinningValid(null, vmStatic));

        Assert.assertTrue("empty string must be accepted",
                test.isCpuPinningValid("", vmStatic));

        Assert.assertFalse(test.isCpuPinningValid("intentionally invalid", vmStatic));

        Assert.assertTrue(test.isCpuPinningValid("0#0", vmStatic));
        Assert.assertTrue(test.isCpuPinningValid("0#1-4", vmStatic));
        Assert.assertFalse(test.isCpuPinningValid("0#^3", vmStatic));
        Assert.assertFalse(test.isCpuPinningValid("0#^3,^2", vmStatic));
        Assert.assertTrue(test.isCpuPinningValid("0#1-8,^6", vmStatic));
        Assert.assertTrue(test.isCpuPinningValid("0#1-8,^6,^7", vmStatic));
        Assert.assertTrue(test.isCpuPinningValid("0#1-8,^6,^7", vmStatic));
        Assert.assertTrue(test.isCpuPinningValid("0#1-8,^5,^6,^7", vmStatic));
        Assert.assertTrue(test.isCpuPinningValid("0#1,2,3", vmStatic));
        Assert.assertFalse(test.isCpuPinningValid("0#^1,^2,^3", vmStatic));
        Assert.assertTrue(test.isCpuPinningValid("0#1-4,6-8", vmStatic));
        Assert.assertTrue(test.isCpuPinningValid("0#1-4,6-8,9-12", vmStatic));
        Assert.assertTrue(test.isCpuPinningValid("0#1-4,^3,9-12,^10", vmStatic));

        Assert.assertTrue(test.isCpuPinningValid("0#0_1#1", vmStatic));
        Assert.assertTrue(test.isCpuPinningValid("0#1-2_1#1-2", vmStatic));
        Assert.assertTrue(test.isCpuPinningValid("0#1,2,3_1#2,3", vmStatic));
        Assert.assertTrue(test.isCpuPinningValid("0#1,2,3_1#1-4,^3", vmStatic));
        //validate vcpus over 9
        Assert.assertTrue(test.isCpuPinningValid("10#1,2,3_11#1-4,^3", vmStatic));

        //negative tests

        Assert.assertFalse("random wrong text",
                test.isCpuPinningValid("lorem ipsum", vmStatic));
        Assert.assertFalse("no cpu id specified, should not pass",
                test.isCpuPinningValid("0", vmStatic));
        Assert.assertFalse("letter instead of vcpu ID",
                test.isCpuPinningValid("A#1", vmStatic));
        Assert.assertFalse("letter instead of cpu ID",
                test.isCpuPinningValid("0#B", vmStatic));
        Assert.assertFalse("A separating _ while only one vcpu pinning",
                test.isCpuPinningValid("0#1_", vmStatic));
        Assert.assertFalse("Trailing _",
                test.isCpuPinningValid("0#1_1#2_", vmStatic));
        Assert.assertFalse("Too many separators",
                test.isCpuPinningValid("0#1__1#2", vmStatic));
        Assert.assertFalse("trailing junk",
                test.isCpuPinningValid("0#1_1#2...", vmStatic));


        // negative logical validation
        ArrayList<String> canDoActionMessages = test.getReturnValue().getCanDoActionMessages();
        canDoActionMessages.clear();
        Assert.assertFalse(test.isCpuPinningValid("10#1,2,3_10#1-4,^3", vmStatic));
        Assert.assertTrue(canDoActionMessages.size() > 0);
        if (canDoActionMessages.size() > 0) {
            Assert.assertEquals(VdcBllMessages.VM_PINNING_DUPLICATE_DEFINITION.toString(), canDoActionMessages.get(0));
        }
        canDoActionMessages.clear();
        Assert.assertFalse(test.isCpuPinningValid("10#1,2,^1,^2", vmStatic));
        Assert.assertTrue(canDoActionMessages.size() > 0);
        if (canDoActionMessages.size() > 0) {
            Assert.assertEquals(VdcBllMessages.VM_PINNING_PINNED_TO_NO_CPU.toString(), canDoActionMessages.get(0));
        }
        canDoActionMessages.clear();
        Assert.assertFalse(test.isCpuPinningValid("10#1,2,3_20#1-4,^3", vmStatic));
        Assert.assertTrue(canDoActionMessages.size() > 0);
        if (canDoActionMessages.size() > 0) {
            Assert.assertEquals(VdcBllMessages.VM_PINNING_VCPU_DOES_NOT_EXIST.toString(), canDoActionMessages.get(0));
        }
        canDoActionMessages.clear();
        Assert.assertFalse(test.isCpuPinningValid("10#1,2,3_11#1-20,^3", vmStatic));
        Assert.assertTrue(canDoActionMessages.size() > 0);
        if (canDoActionMessages.size() > 0) {
            Assert.assertEquals(VdcBllMessages.VM_PINNING_PCPU_DOES_NOT_EXIST.toString(), canDoActionMessages.get(0));
        }

        // additional tests for CPUs disabled on-the-fly
        dedicatedVds.setOnlineCpus("0,1,2,4,5,6,7,8,9,10,11,12,13,14,15");

        Assert.assertFalse("use of disabled cpu", test.isCpuPinningValid("0#3", vmStatic));
        Assert.assertTrue(canDoActionMessages.size() > 0);
        if (canDoActionMessages.size() > 0) {
            Assert.assertEquals(VdcBllMessages.VM_PINNING_PCPU_DOES_NOT_EXIST.toString(), canDoActionMessages.get(0));
        }

        // additional tests for CPUs disabled on-the-fly
        dedicatedVds.setOnlineCpus("0,4,8,16,24,32,40,48,56,64,68,72,76,80,84");

        Assert.assertTrue("use of cpu with a id larger than the number of CPU threads",
                test.isCpuPinningValid("0#84", vmStatic));

        // making sure cluster < 3.2 does not get validated on pCPU as we cant tell the number for sure
        dedicatedVds.setVdsGroupCompatibilityVersion(Version.v3_1);
        Assert.assertTrue(test.isCpuPinningValid("10#1,2,3_11#1-20,^3", vmStatic));

    }
}
