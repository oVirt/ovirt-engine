package org.ovirt.engine.core.bll.utils;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import org.ovirt.engine.core.bll.CommandBase;
import org.ovirt.engine.core.bll.VdsHandler;
import org.ovirt.engine.core.bll.interfaces.BackendInternal;
import org.ovirt.engine.core.common.businessentities.Entities;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.errors.EngineError;
import org.ovirt.engine.core.common.errors.EngineException;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSParametersBase;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.common.vdscommands.VdsIdVDSCommandParametersBase;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.di.Injector;

public abstract class VdsCommandsHelper {
    public static VDSReturnValue runVdsCommandWithFailover(VDSCommandType vdsCommandType, VdsIdVDSCommandParametersBase parametersBase, Guid storagePoolId, CommandBase<?> executingCommand) {
        return runVdsCommandWithFailover(vdsCommandType, parametersBase, storagePoolId, executingCommand, true);
    }

    public static VDSReturnValue runVdsCommandWithoutFailover(VDSCommandType vdsCommandType, VdsIdVDSCommandParametersBase parametersBase, Guid storagePoolId) {
        return runVdsCommand(vdsCommandType, parametersBase, storagePoolId, null, false);
    }


    public static VDSReturnValue runVdsCommandWithFailover(VDSCommandType vdsCommandType, VDSParametersBase parametersBase, Guid storagePoolId) {
        return runVdsCommand(vdsCommandType, parametersBase, storagePoolId, null, true);
    }

    private static VDSReturnValue runVdsCommand(VDSCommandType vdsCommandType, VDSParametersBase parametersBase, Guid storagePoolId, CommandBase commandBase, boolean perfromFailover) {
        if (parametersBase instanceof VdsIdVDSCommandParametersBase) {
            return runVdsCommandWithFailover(vdsCommandType, (VdsIdVDSCommandParametersBase)parametersBase, storagePoolId, commandBase, perfromFailover);
        }

        throw new IllegalArgumentException("parameters of unsupported type");
    }


    private static VDSReturnValue runVdsCommandWithFailover(VDSCommandType vdsCommandType, VdsIdVDSCommandParametersBase parametersBase, Guid storagePoolId, CommandBase<?> executingCommand, boolean perfromFailover) {
        int attempts = 0;
        List<Guid> executedHosts = new LinkedList<>();
        VDSReturnValue returnValue = null;
        if (parametersBase.getVdsId() == null) {
            chooseHostForExecution(parametersBase, storagePoolId, executingCommand, Collections.<Guid>emptyList(), true);
        }

        while (attempts <= Config.<Integer> getValue(ConfigValues.SpmCommandFailOverRetries)) {
            try {
                attempts++;
                returnValue = getBackend().getResourceManager().RunVdsCommand(vdsCommandType, parametersBase);
                if (returnValue != null && returnValue.getSucceeded()) {
                    return returnValue;
                }
            } catch (EngineException e) {
                returnValue = e.getVdsReturnValue();
            }

            executedHosts.add(parametersBase.getVdsId());

            if (!perfromFailover || (returnValue != null && !returnValue.isCanTryOnDifferentVds())) {
                break;
            }

            chooseHostForExecution(parametersBase, storagePoolId, executingCommand, executedHosts, false);

            if (parametersBase.getVdsId() == null) {
                break;
            }
        }

        return VdsHandler.handleVdsResult(returnValue);
    }

    private static void chooseHostForExecution(VdsIdVDSCommandParametersBase parametersBase, Guid storagePoolId, CommandBase<?> executingCommand, List<Guid> executedHosts, boolean failIfNoHost) {
        Guid vdsForExecution = getHostForExecution(storagePoolId, executedHosts, failIfNoHost);
        parametersBase.setVdsId(vdsForExecution);

        if (executingCommand != null) {
            executingCommand.getParameters().setVdsRunningOn(vdsForExecution);
            executingCommand.persistCommand(executingCommand.getParameters().getParentCommand(), executingCommand.getCallback() != null);
        }
    }

    public static Guid getHostForExecution(Guid poolId, List<Guid> hostsToFilter, boolean failOnNoHost) {
        List<Guid> hostsForExecution = Entities.getIds(DbFacade.getInstance().getVdsDao().getAllForStoragePoolAndStatus(poolId, VDSStatus.Up));
        hostsForExecution.removeAll(hostsToFilter);
        if (hostsForExecution.isEmpty()) {
            if (failOnNoHost) {
                throw new EngineException(EngineError.RESOURCE_MANAGER_VDS_NOT_FOUND,
                        String.format("No host was found to perform the operation"));
            }

            return null;
        }

        return hostsForExecution.get(new Random().nextInt(hostsForExecution.size()));
    }

    protected static BackendInternal getBackend() {
        return Injector.get(BackendInternal.class);
    }
}
