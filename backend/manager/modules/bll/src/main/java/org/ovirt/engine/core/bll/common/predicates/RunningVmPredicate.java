package org.ovirt.engine.core.bll.common.predicates;

import java.util.function.Predicate;

import org.ovirt.engine.core.common.businessentities.VM;

public final class RunningVmPredicate implements Predicate<VM> {
    @Override
    public boolean test(VM vm) {
        return vm.isRunning();
    }
}
