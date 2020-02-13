package org.ovirt.engine.core.bll.storage.utils;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.ovirt.engine.core.bll.CommandBase;
import org.ovirt.engine.core.bll.VdsHandler;
import org.ovirt.engine.core.bll.interfaces.BackendInternal;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.errors.EngineError;
import org.ovirt.engine.core.common.errors.EngineException;
import org.ovirt.engine.core.common.interfaces.VDSBrokerFrontend;
import org.ovirt.engine.core.common.job.Step;
import org.ovirt.engine.core.common.job.StepEnum;
import org.ovirt.engine.core.common.job.StepSubjectEntity;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.common.vdscommands.VdsIdVDSCommandParametersBase;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogDirector;
import org.ovirt.engine.core.dal.job.ExecutionMessageDirector;
import org.ovirt.engine.core.dao.StepDao;
import org.ovirt.engine.core.dao.StepSubjectEntityDao;
import org.ovirt.engine.core.dao.VdsDao;
import org.ovirt.engine.core.di.Injector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class VdsCommandsHelper {

    private static final Logger log = LoggerFactory.getLogger(VdsCommandsHelper.class);

    @Inject
    private BackendInternal backend;

    @Inject
    private VDSBrokerFrontend resourceManager;

    @Inject
    private VdsDao vdsDao;

    @Inject
    private StepDao stepDao;

    @Inject
    private StepSubjectEntityDao stepSubjectEntityDao;

    private VdsCommandsHelper() {
    }

    public VDSReturnValue runVdsCommandWithFailover(VDSCommandType vdsCommandType,
                                                    VdsIdVDSCommandParametersBase params,
                                                    Guid storagePoolId, CommandBase<?> cmd) {
        return runVdsCommand(vdsCommandType, params, storagePoolId, cmd, true);
    }

    public VDSReturnValue runVdsCommandWithoutFailover(VDSCommandType vdsCommandType,
                                                       VdsIdVDSCommandParametersBase parametersBase,
                                                       Guid storagePoolId, CommandBase<?> cmd) {
        return runVdsCommand(vdsCommandType, parametersBase, storagePoolId, cmd, false);
    }

    private VDSReturnValue runVdsCommand(VDSCommandType vdsCommandType, VdsIdVDSCommandParametersBase params,
                                         Guid storagePoolId, CommandBase<?> cmd, boolean performFailover) {
        Set<Guid> executedHosts = new HashSet<>();
        VDSReturnValue returnValue = null;
        if (params.getVdsId() == null) {
            chooseHostForExecution(params, storagePoolId, cmd, Collections.emptyList());
            if (params.getVdsId() == null) {
                throw new EngineException(EngineError.RESOURCE_MANAGER_VDS_NOT_FOUND,
                        "No host was found to perform the operation");
            }
        }

        int attempts = 0;
        while (attempts <= Config.<Integer>getValue(ConfigValues.HsmCommandFailOverRetries)) {
            try {
                attempts++;
                returnValue = resourceManager.runVdsCommand(vdsCommandType, params);
                if (returnValue != null && returnValue.getSucceeded()) {
                    return returnValue;
                }
            } catch (EngineException e) {
                returnValue = e.getVdsReturnValue();
            }

            executedHosts.add(params.getVdsId());

            if (!performFailover || (returnValue != null && !returnValue.isCanTryOnDifferentVds())) {
                break;
            }

            chooseHostForExecution(params, storagePoolId, cmd, executedHosts);

            if (params.getVdsId() == null) {
                break;
            }
        }

        return VdsHandler.handleVdsResult(returnValue);
    }

    private void chooseHostForExecution(VdsIdVDSCommandParametersBase parametersBase, Guid storagePoolId,
                                        CommandBase<?> cmd, Collection<Guid> executedHosts) {
        Guid vdsForExecution = getHostForExecution(storagePoolId, executedHosts);
        parametersBase.setVdsId(vdsForExecution);

        if (cmd != null) {
            if (cmd.getCommandStep() != null && cmd.getExecutionContext().getStep() != null) {
                Guid stepId = cmd.getExecutionContext().getStep().getId();
                if (cmd.getParameters().getVdsRunningOn() != null) {
                    stepSubjectEntityDao.remove(cmd.getParameters().getVdsRunningOn(), stepId);
                }

                if (vdsForExecution != null) {
                    stepSubjectEntityDao.saveAll(Collections.singletonList(
                            new StepSubjectEntity(stepId, VdcObjectType.EXECUTION_HOST, vdsForExecution)));

                    updateStepMessage(cmd, vdsForExecution);
                }
            }

            cmd.getParameters().setVdsRunningOn(vdsForExecution);
            cmd.persistCommand(cmd.getParameters().getParentCommand(), cmd
                    .getCallback() != null);
        }
    }

    public Guid getHostForExecution(Guid poolId) {
        return getHostForExecution(poolId, Collections.emptyList());
    }

    public Guid getHostForExecution(Guid poolId, Collection<Guid> hostsToFilter) {
        return getHostForExecution(poolId, vds -> !hostsToFilter.contains(vds.getId()));
    }

    public Guid getHostForExecution(Guid poolId, Predicate<VDS> predicate) {
        List<Guid> hostsForExecution = vdsDao
                .getAllForStoragePoolAndStatus(poolId, VDSStatus.Up).stream()
                .filter(predicate)
                .map(x -> x.getId()).collect(Collectors.toList());
        if (hostsForExecution.isEmpty()) {
            return null;
        }

        return hostsForExecution.get(new Random().nextInt(hostsForExecution.size()));
    }

    private void updateStepMessage(CommandBase<?> cmd, Guid vdsForExecution) {
        // As an HSM job can run on any host, we want to display the host running the job when it is
        // chosen. To do so, we look for a corresponding command step with an "_ON_HOST" suffix that
        // is supposed to contain a "vds" placeholder.
        StepEnum stepEnum = null;
        try {
            stepEnum = StepEnum.valueOf(getStepWithHostname(cmd));
        } catch (IllegalArgumentException e) {
            // Ignore this exception and do nothing as no corresponding step_ON_HOST found.
            log.debug("No StepEnum found for " + getStepWithHostname(cmd));
            return;
        }

        Step step = cmd.getExecutionContext().getStep();
        Map<String, String> jobProperties = cmd.getJobMessageProperties();
        jobProperties.put(VdcObjectType.VDS.name().toLowerCase(), vdsDao.get(vdsForExecution).getName());
        step.setDescription(ExecutionMessageDirector.resolveStepMessage(stepEnum, jobProperties));
        stepDao.update(step);

        // Add an audit log entry if a corresponding AuditLogType exists. Note that we expect an AuditLogType
        // with name equals to Step_Enum to exist. If an AuditLogType exists, the arguments in the audit
        // message must match these in the StepEnum message.
        AuditLogType logType = null;
        try {
            logType = AuditLogType.valueOf(getAuditLogType(cmd));
        } catch (IllegalArgumentException e) {
            // Ignore this exception and do nothing as no corresponding AuditLogType found.
            log.debug("No AuditLogType found for " + getAuditLogType(cmd));
            return;
        }
        jobProperties.entrySet().forEach(entry -> cmd.addCustomValue(entry.getKey(), entry.getValue()));
        Injector.get(AuditLogDirector.class).log(cmd, logType);
    }

    private static String getStepWithHostname(CommandBase<?> cmd) {
        return cmd.getCommandStep().toString() + "_ON_HOST";
    }

    private static String getAuditLogType(CommandBase<?> cmd) {
        return getStepWithHostname(cmd);
    }
}
