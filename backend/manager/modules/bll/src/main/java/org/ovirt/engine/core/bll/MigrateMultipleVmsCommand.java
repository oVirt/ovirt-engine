package org.ovirt.engine.core.bll;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.job.ExecutionContext;
import org.ovirt.engine.core.bll.job.ExecutionHandler;
import org.ovirt.engine.core.bll.scheduling.SchedulingManager;
import org.ovirt.engine.core.bll.scheduling.SchedulingParameters;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.bll.validator.VmValidator;
import org.ovirt.engine.core.common.FeatureSupported;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.ActionReturnValue;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.MigrateMultipleVmsParameters;
import org.ovirt.engine.core.common.action.MigrateVmToServerParameters;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.comparators.VmsComparer;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.job.Step;
import org.ovirt.engine.core.common.job.StepEnum;
import org.ovirt.engine.core.common.utils.ExecutionMethod;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.job.ExecutionMessageDirector;
import org.ovirt.engine.core.dao.VmDao;

@NonTransactiveCommandAttribute
public class MigrateMultipleVmsCommand<T extends MigrateMultipleVmsParameters> extends CommandBase<T> {

    @Inject
    private VmDao vmDao;
    @Inject
    private SchedulingManager schedulingManager;

    private List<VM> vms;
    private List<VM> possibleVmsToMigrate = Collections.emptyList();

    public MigrateMultipleVmsCommand(T parameters, CommandContext cmdContext) {
        super(parameters, cmdContext);
    }

    @Override
    protected void init() {
        if (!getVms().isEmpty()) {
            setClusterId(getVms().get(0).getClusterId());
        }
    }

    @Override
    protected boolean validate() {
        if (getVms().isEmpty()) {
            return failValidation(EngineMessage.ACTION_TYPE_FAILED_VMS_NOT_FOUND);
        }

        if (!FeatureSupported.isMigrationSupported(getCluster().getArchitecture(), getCluster().getCompatibilityVersion())) {
            return failValidation(EngineMessage.MIGRATION_IS_NOT_SUPPORTED);
        }

        // All VMs should run on the same cluster
        if (getVms().stream().anyMatch(vm -> !getClusterId().equals(vm.getClusterId()))) {
            return failValidation(EngineMessage.ACTION_TYPE_FAILED_VMS_NOT_RUNNING_ON_SINGLE_CLUSTER);
        }

        Set<Guid> existingVmIds = getVms().stream()
                .map(VM::getId)
                .collect(Collectors.toSet());

        if (!existingVmIds.containsAll(getParameters().getVms())) {
            return failValidation(EngineMessage.ACTION_TYPE_FAILED_VMS_NOT_FOUND);
        }

        boolean forceMigration = getParameters().isForceMigration();
        if (getVms().stream().anyMatch(vm -> !validate(getVmValidator(vm).canMigrate(forceMigration)))) {
            return false;
        }

        Map<Guid, List<VDS>> possibleVmHosts = schedulingManager.canSchedule(getCluster(),
                getVms(),
                getParameters().getHostBlackList(),
                Collections.emptyList(),
                new SchedulingParameters(getParameters().isCanIgnoreHardVmAffinity()),
                // TODO - Use error messages from scheduling
                new ArrayList<>());

        possibleVmsToMigrate = new ArrayList<>(getVms().size());
        for (VM vm : getVms()) {
            if (possibleVmHosts.getOrDefault(vm.getId(), Collections.emptyList()).isEmpty()) {
                log.warn("VM '{}' cannot be migrated.", vm.getName());
                continue;
            }
            possibleVmsToMigrate.add(vm);
        }

        if (possibleVmsToMigrate.isEmpty()) {
            return false;
        }

        return true;
    }

    @Override
    protected void executeCommand() {
        // Sort VMs by priority and migrate the most important first
        possibleVmsToMigrate.sort(new VmsComparer().reversed());

        Map<Guid, Guid> assignment = scheduleVms(possibleVmsToMigrate, false);
        if (getParameters().isCanIgnoreHardVmAffinity() && assignment.size() != possibleVmsToMigrate.size()) {
            List<VM> unassignedVms = possibleVmsToMigrate.stream()
                    .filter(vm -> !assignment.containsKey(vm.getId()))
                    .collect(Collectors.toList());

            assignment.putAll(scheduleVms(unassignedVms, true));
        }

        // Fail if not all VMs can be migrated
        setSucceeded(getVms().size() == possibleVmsToMigrate.size());

        for (VM vm : possibleVmsToMigrate) {
            Guid hostId = assignment.get(vm.getId());
            if (hostId == null || hostId.equals(vm.getRunOnVds())) {
                continue;
            }

            MigrateVmToServerParameters parameters = new MigrateVmToServerParameters(
                    getParameters().isForceMigration(),
                    vm.getId(),
                    hostId
            );
            parameters.setSkipScheduling(true);

            ActionReturnValue returnValue = runInternalAction(ActionType.MigrateVmToServer,
                    parameters,
                    createMigrateVmContext(vm));

            if (!returnValue.getSucceeded()) {
                log.warn("VM '{}' failed migration.", vm.getName());
                setSucceeded(false);
            }
        }
    }

    @Override
    public List<PermissionSubject> getPermissionCheckSubjects() {
        return getParameters().getVms().stream()
                .map(vmId -> new PermissionSubject(vmId, VdcObjectType.VM, getActionType().getActionGroup()))
                .collect(Collectors.toList());
    }

    private Map<Guid, Guid> scheduleVms(List<VM> vms, boolean shouldIgnoreVmAffinity) {
        return schedulingManager.schedule(getCluster(),
                vms,
                getParameters().getHostBlackList(),
                Collections.emptyList(),
                Collections.emptyList(),
                new SchedulingParameters(shouldIgnoreVmAffinity),
                new ArrayList<>(),
                true,
                getCorrelationId());
    }

    private List<VM> getVms() {
        if (vms == null) {
            vms = vmDao.getVmsByIds(getParameters().getVms());
        }
        return vms;
    }

    public void setVms(List<VM> vms) {
        this.vms = vms;
    }

    private CommandContext createMigrateVmContext(VM vm) {
        if (!getExecutionContext().isMonitored()) {
            return ExecutionHandler.createInternalJobContext(getContext());
        }

        ExecutionContext ctx = new ExecutionContext();
        try {
            Map<String, String> values = new HashMap<>();
            values.put(VdcObjectType.VM.name().toLowerCase(), vm.getName());
            values.put(VdcObjectType.VDS.name().toLowerCase(), vm.getRunOnVdsName());

            Step currentStep = (getExecutionContext().getExecutionMethod() == ExecutionMethod.AsJob) ?
                    getExecutionContext().getJob().getStep(StepEnum.EXECUTING) :
                    getExecutionContext().getStep().getStep(StepEnum.EXECUTING);

            Step step = executionHandler.addSubStep(getExecutionContext(),
                    currentStep,
                    StepEnum.MIGRATE_VM,
                    ExecutionMessageDirector.resolveStepMessage(StepEnum.MIGRATE_VM, values));

            ctx.setJob(getExecutionContext().getJob());
            ctx.setStep(step);
            ctx.setMonitored(true);
        } catch (RuntimeException e) {
            log.error("Failed to create ExecutionContext for MigrateVmCommand", e);
        }
        return cloneContextAndDetachFromParent().withExecutionContext(ctx);
    }

    protected VmValidator getVmValidator(VM vm) {
        return new VmValidator(vm);
    }
}
