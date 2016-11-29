package org.ovirt.engine.core.bll.network.vm.mac;

import java.util.Collection;
import java.util.Objects;

import org.ovirt.engine.core.bll.ValidationResult;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.utils.ReplacementUtils;

public class VmMacsValidation {
    private final EngineMessage violationMessage;
    private final ProblematicVmMacsFinder problematicVmMacsFinder;

    VmMacsValidation(EngineMessage violationMessage, ProblematicVmMacsFinder problematicVmMacsFinder) {
        this.violationMessage = Objects.requireNonNull(violationMessage);
        this.problematicVmMacsFinder = Objects.requireNonNull(problematicVmMacsFinder);
    }

    public ValidationResult validate(VM vm) {
        Collection<String> problematicMacs = problematicVmMacsFinder.findProblematicMacs(vm);
        return ValidationResult.failWith(violationMessage,
                ReplacementUtils.getListVariableAssignmentString(violationMessage, problematicMacs))
                .unless(problematicMacs.isEmpty());
    }
}
