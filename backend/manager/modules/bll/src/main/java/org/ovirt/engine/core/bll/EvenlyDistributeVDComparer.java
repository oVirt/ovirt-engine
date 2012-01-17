package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.common.businessentities.*;

/**
 * This comparer chose Vds with less vms running regarding if vds will be
 * overcommited after its will run vm.
 */
public class EvenlyDistributeVDComparer extends VdsComparer {
    @Override
    public boolean IsBetter(VDS x, VDS y, VM vm) {
        if (x.getvms_cores_count() == null || y.getvms_cores_count() == null) {
            return false;
        }
        boolean returnValue;
        returnValue = ((double) x.getvms_cores_count() / x.getcpu_cores()) > ((double) y.getvms_cores_count() / y
                .getcpu_cores());
        return returnValue;
    }

    private boolean isVdsGoingToBeOverCommited(VDS vds, VM vm) {
        Integer mem_commited = vds.getmem_commited();
        Integer guest_overhead = vds.getguest_overhead();
        Integer reserved_mem = vds.getreserved_mem();
        Integer physical_mem_mb = vds.getphysical_mem_mb();

        if (mem_commited == null || guest_overhead == null || reserved_mem == null || physical_mem_mb == null) {
            return false;
        }

        return (mem_commited + vm.getvm_mem_size_mb() + guest_overhead + reserved_mem) > physical_mem_mb;
    }

    @Override
    public void BestVdsProcedure(VDS x) {
        x.setvm_count(x.getvm_count() + 1);
    }
}
