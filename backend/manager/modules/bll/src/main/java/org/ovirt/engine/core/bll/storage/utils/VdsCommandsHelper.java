package org.ovirt.engine.core.bll.storage.utils;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

import org.ovirt.engine.core.bll.Backend;
import org.ovirt.engine.core.bll.CommandBase;
import org.ovirt.engine.core.bll.VdsHandler;
import org.ovirt.engine.core.bll.interfaces.BackendInternal;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.errors.EngineError;
import org.ovirt.engine.core.common.errors.EngineException;
import org.ovirt.engine.core.common.job.StepSubjectEntity;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.common.vdscommands.VdsIdVDSCommandParametersBase;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dao.StepSubjectEntityDao;
import org.ovirt.engine.core.dao.VdsDao;

public class VdsCommandsHelper {

    private VdsCommandsHelper() {
    }

    public static VDSReturnValue runVdsCommandWithFailover(VDSCommandType vdsCommandType,
                                                           VdsIdVDSCommandParametersBase params,
                                                           Guid storagePoolId, CommandBase<?> cmd) {
        return runVdsCommand(vdsCommandType, params, storagePoolId, cmd, true);
    }

    public static VDSReturnValue runVdsCommandWithoutFailover(VDSCommandType vdsCommandType,
                                                              VdsIdVDSCommandParametersBase parametersBase, Guid
                                                                      storagePoolId, CommandBase<?> cmd) {
        return runVdsCommand(vdsCommandType, parametersBase, storagePoolId, cmd, false);
    }

    private static VDSReturnValue runVdsCommand(VDSCommandType vdsCommandType,
                                                            VdsIdVDSCommandParametersBase params, Guid
                                                                    storagePoolId, CommandBase<?> cmd,
                                                            boolean performFailover) {
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
                returnValue = getBackend().getResourceManager().runVdsCommand(vdsCommandType, params);
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

    private static void chooseHostForExecution(VdsIdVDSCommandParametersBase parametersBase, Guid storagePoolId,
                                               CommandBase<?> cmd, Collection<Guid> executedHosts) {
        Guid vdsForExecution = getHostForExecution(storagePoolId, executedHosts);
        parametersBase.setVdsId(vdsForExecution);

        if (cmd != null) {
            if (cmd.getCommandStep() != null && cmd.getExecutionContext().getStep() != null) {
                Guid stepId = cmd.getExecutionContext().getStep().getId();
                if (cmd.getParameters().getVdsRunningOn() != null) {
                    getStepSubjectEntityDao().remove(cmd.getParameters().getVdsRunningOn(), stepId);
                }

                if (vdsForExecution != null) {
                    getStepSubjectEntityDao().saveAll(Collections.singletonList(
                            new StepSubjectEntity(stepId, VdcObjectType.EXECUTION_HOST, vdsForExecution)));
                }
            }

            cmd.getParameters().setVdsRunningOn(vdsForExecution);
            cmd.persistCommand(cmd.getParameters().getParentCommand(), cmd
                    .getCallback() != null);
        }
    }

    public static Guid getHostForExecution(Guid poolId, Collection<Guid> hostsToFilter) {
        List<Guid> hostsForExecution = getVdsDao()
                .getAllForStoragePoolAndStatus(poolId, VDSStatus.Up).stream()
                .filter(x -> !hostsToFilter.contains(x.getId()))
                .map(x -> x.getId()).collect(Collectors.toList());
        if (hostsForExecution.isEmpty()) {
            return null;
        }

        return hostsForExecution.get(new Random().nextInt(hostsForExecution.size()));
    }

    protected static BackendInternal getBackend() {
        return Backend.getInstance();
    }

    protected static VdsDao getVdsDao() {
        return DbFacade.getInstance().getVdsDao();
    }

    protected static StepSubjectEntityDao getStepSubjectEntityDao() {
        return DbFacade.getInstance().getStepSubjectEntityDao();
    }
}
