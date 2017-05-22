package org.ovirt.engine.core.bll.network.vm;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.QueriesCommandBase;
import org.ovirt.engine.core.bll.ValidationResult;
import org.ovirt.engine.core.bll.context.EngineContext;
import org.ovirt.engine.core.bll.network.macpool.MacPoolPerCluster;
import org.ovirt.engine.core.bll.network.macpool.ReadMacPool;
import org.ovirt.engine.core.bll.network.vm.mac.VmMacsValidation;
import org.ovirt.engine.core.bll.network.vm.mac.VmMacsValidationsFactory;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.queries.ValidateVmMacsParameters;
import org.ovirt.engine.core.compat.Guid;

/**
 * Validates the MAC addresses of the given VMs.
 * <p/>
 * The result is a Map with a VM id as the key and violation messages (that are related to the VM) as the values.
 *
 * Each value is a group consisting of inner groups, where each inner group holds a violation message and its
 * replacement entries. The use for multiple groups over a single flat group is to make sure there aren't any
 * overlapping replacement entries.
 *
 * @param <P>
 *            the query parameter type
 */
public class ValidateVmMacsQuery<P extends ValidateVmMacsParameters> extends QueriesCommandBase<P> {

    @Inject
    private MacPoolPerCluster macPoolPerCluster;

    @Inject
    private VmMacsValidationsFactory vmMacsValidationsFactory;

    public ValidateVmMacsQuery(P parameters, EngineContext engineContext) {
        super(parameters, engineContext);
    }

    @Override
    protected void executeQueryCommand() {
        // Map with a VM id as the key and violation messages (that are related to the VM) as the values.
        final Map<Guid, List<List<String>>> result = new HashMap<>();
        for (Entry<Guid, List<VM>> clusterEntry : getParameters().getVmsByCluster().entrySet()) {
            final Guid clusterId = clusterEntry.getKey();
            final List<VM> clusterVms = clusterEntry.getValue();
            final ReadMacPool macPool = macPoolPerCluster.getMacPoolForCluster(clusterId);
            final List<VmMacsValidation> vmMacsValidations =
                    vmMacsValidationsFactory.createVmMacsValidationList(clusterId, macPool);
            clusterVms.forEach(vm -> result.put(vm.getId(), validateVm(vm, vmMacsValidations)));
        }
        getQueryReturnValue().setReturnValue(result);
    }

    /**
     * Applies the given validations to the VM.
     *
     * @return a list of violations where each violation consists of the <code>EngineMessage</code> and the details
     *         related to it. In case all validations are valid an empty list would be returned.
     */
    private List<List<String>> validateVm(VM vm, Collection<VmMacsValidation> vmMacsValidations) {
        return vmMacsValidations
                .stream()
                .map(vmMacsValidation -> vmMacsValidation.validate(vm))
                .filter(((Predicate<ValidationResult>) ValidationResult::isValid).negate())
                .map(this::extractViolationDetails)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    private List<String> extractViolationDetails(ValidationResult validationResult) {
        final List<String> violationDetails =
                validationResult.getMessages()
                        .stream()
                        .map(EngineMessage::name)
                        .collect(Collectors.toCollection(ArrayList::new));
        violationDetails.addAll(validationResult.getVariableReplacements());
        return violationDetails;
    }
}
