package org.ovirt.engine.core.bll.common.predicates;

import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.utils.linq.Predicate;

public final class RunningVmPredicate implements Predicate<VM> {
    @Override
    public boolean eval(VM vm) {
        return vm.isRunning();
    }
}
