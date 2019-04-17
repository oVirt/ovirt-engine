package org.ovirt.engine.core.bll;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.job.ExecutionContext;
import org.ovirt.engine.core.bll.job.ExecutionHandler;
import org.ovirt.engine.core.bll.scheduling.SchedulingManager;
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
import org.ovirt.engine.core.dao.PermissionDao;
import org.ovirt.engine.core.dao.VmDao;

@NonTransactiveCommandAttribute
public class MigrateMultipleVmsCommand<T extends MigrateMultipleVmsParameters> extends CommandBase<T> {

    @Inject
    private VmDao vmDao;
    @Inject
    private PermissionDao permissionDao;
    @Inject
    private SchedulingManager schedulingManager;

    private List<VM> vms;
    private List<Guid> hostBlackList;
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
        if (getParameters().getVms().isEmpty()) {
            return failValidation(EngineMessage.ACTION_TYPE_FAILED_NO_VMS_SPECIFIED);
        }

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

        if (getParameters().isAddVmsInPositiveHardAffinity()) {
            setVms(computeVmAffinityClosure());

            // The permissions have to be checked for the new VMs.
            if(!hasPermissionToMigrateVms(getVms())) {
                return failValidation(EngineMessage.USER_NOT_AUTHORIZED_TO_PERFORM_ACTION);
            }
        }

        boolean forceMigration = getParameters().isForceMigration();
        if (getVms().stream().anyMatch(vm -> !validate(getVmValidator(vm).canMigrate(forceMigration)))) {
            return false;
        }

        Map<Guid, List<VDS>> possibleVmHosts = schedulingManager.prepareCall(getCluster())
                .hostBlackList(getHostBlackList())
                .hostWhiteList(getHostWhiteList())
                .ignoreHardVmToVmAffinity(getParameters().isCanIgnoreHardVmAffinity())
                // TODO - Use error messages from scheduling
                .canSchedule(getVms());

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

            if (!returnValue.isValid()) {
                getReturnValue().getValidationMessages().addAll(returnValue.getValidationMessages());
                getReturnValue().setValid(false);
            }

            if (!returnValue.getSucceeded()) {
                // The pending resources are added by this command
                // and are not cleared if the child command fails
                schedulingManager.clearPendingVm(vm.getStaticData());

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
        return schedulingManager.prepareCall(getCluster())
                .hostBlackList(getHostBlackList())
                .hostWhiteList(getHostWhiteList())
                .ignoreHardVmToVmAffinity(shouldIgnoreVmAffinity)
                .delay(true)
                .correlationId(getCorrelationId())
                .schedule(vms);
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

    private List<VM> computeVmAffinityClosure() {
        Set<Guid> hosts = getVms().stream()
                .map(VM::getRunOnVds)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        List<Guid> vmIds = getVms().stream().map(VM::getId).collect(Collectors.toList());
        Set<Guid> vmAffinityClosure = schedulingManager.positiveAffinityClosure(getCluster(), vmIds);

        vmAffinityClosure.removeAll(vmIds);

        return Stream.concat(getVms().stream(), vmDao.getVmsByIds(vmAffinityClosure).stream())
                // Do not migrate VMs that run on hosts where no original VM is running
                .filter(vm -> hosts.contains(vm.getRunOnVds()))
                .collect(Collectors.toList());
    }

    private List<Guid> getHostBlackList() {
        if (hostBlackList == null) {
            hostBlackList = computeHostBlackList();
        }
        return hostBlackList;
    }

    private List<Guid> computeHostBlackList() {
        if (!getParameters().getHostBlackList().isEmpty()) {
            return getParameters().getHostBlackList();
        }

        Guid sourceHost = null;
        for (VM vm : getVms()) {
            if (vm.getRunOnVds() == null) {
                continue;
            }

            if (sourceHost == null) {
                sourceHost = vm.getRunOnVds();
                continue;
            }

            // In case VMs run on 2 or more hosts, the blacklist is empty
            if (!sourceHost.equals(vm.getRunOnVds())) {
                return Collections.emptyList();
            }
        }
        return Collections.singletonList(sourceHost);
    }

    private List<Guid> getHostWhiteList() {
        return getParameters().getDestinationHostId() != null ?
                Collections.singletonList(getParameters().getDestinationHostId()) :
                Collections.emptyList();
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

    private boolean hasPermissionToMigrateVms(List<VM> vms) {
        return vms.stream()
                .map(vm -> permissionDao.getEntityPermissions(getUserId(),
                        getActionType().getActionGroup(),
                        vm.getId(),
                        VdcObjectType.VM))
                .allMatch(Objects::nonNull);
    }
}
