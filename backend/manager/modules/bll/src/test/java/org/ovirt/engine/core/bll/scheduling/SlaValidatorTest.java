package org.ovirt.engine.core.bll.scheduling;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.Version;

public class SlaValidatorTest {

    private VDS makeTestVds(Guid vdsId) {
        VDS newVdsData = new VDS();
        newVdsData.setHostName("BUZZ");
        newVdsData.setVdsName("BAR");
        newVdsData.setVdsGroupCompatibilityVersion(new Version("1.2.3"));
        newVdsData.setVdsGroupId(Guid.newGuid());
        newVdsData.setId(vdsId);
        return newVdsData;
    }

    @Test
    public void validateVmMemoryCanRunOnVds() {
        Guid guid = Guid.newGuid();
        VDS vds = makeTestVds(guid);
        vds.setPhysicalMemMb(10000);
        vds.setReservedMem(1000);
        vds.setMemCommited(100);
        vds.setPendingVmemSize(10);
        vds.setGuestOverhead(1);

        vds.setMaxVdsMemoryOverCommit(200); // 200% mem overcommit

        VM vm = new VM();

        // vmMem < hostMem (pass)
        vm.setMinAllocatedMem(8800);
        vds.setVmCount(0);
        boolean vmPassedMemoryRequirement = SlaValidator.getInstance().hasMemoryToRunVM(vds, vm);
        assertEquals(vmPassedMemoryRequirement, true);

        // vmMem > hostMem (fail)
        vm.setMinAllocatedMem(10000);
        vds.setVmCount(0);
        vmPassedMemoryRequirement = SlaValidator.getInstance().hasMemoryToRunVM(vds, vm);
        assertEquals(vmPassedMemoryRequirement, false);

        // vmMem > hostMem (pass) (2 or more running vms)
        vm.setMinAllocatedMem(10000);
        vds.setVmCount(1);
        vmPassedMemoryRequirement = SlaValidator.getInstance().hasMemoryToRunVM(vds, vm);
        assertEquals(vmPassedMemoryRequirement, true);

        // vmMem >> hostMem (fail) (2 or more running vms)
        vm.setMinAllocatedMem(20000);
        vds.setVmCount(1);
        vmPassedMemoryRequirement = SlaValidator.getInstance().hasMemoryToRunVM(vds, vm);
        assertEquals(vmPassedMemoryRequirement, false);
    }

}
