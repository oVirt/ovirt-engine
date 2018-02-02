package org.ovirt.engine.core.bll.common.predicates;

import java.util.List;
import java.util.function.Predicate;

import org.apache.commons.lang.Validate;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.VmDao;

/**
 * Evaluates a cluster with the given id on existence of running VMs attached to it.
 */
public final class ActiveVmAttachedToClusterPredicate implements Predicate<Guid> {

    private static final Predicate<VM> RUNNING_VM_PREDICATE = VM::isRunning;

    private final VmDao vmDao;

    public ActiveVmAttachedToClusterPredicate(VmDao vmDao) {
        Validate.notNull(vmDao, "vmDao can not be null");

        this.vmDao = vmDao;
    }

    @Override
    public boolean test(Guid clusterId) {
        final List<VM> vms = vmDao.getAllForCluster(clusterId);

        return vms.stream().anyMatch(RUNNING_VM_PREDICATE);
    }
}
