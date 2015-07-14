package org.ovirt.engine.core.bll.validator;

import java.util.EnumMap;
import java.util.Map;

import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.errors.EngineMessage;

public class LocalizedVmStatus {

    private static final Map<VMStatus, EngineMessage> statusToMessage;

    static {
        statusToMessage = new EnumMap(VMStatus.class);
        statusToMessage.put(VMStatus.Unassigned, EngineMessage.VAR__VM_STATUS__UNASSIGNED);
        statusToMessage.put(VMStatus.Down, EngineMessage.VAR__VM_STATUS__DOWN);
        statusToMessage.put(VMStatus.Up, EngineMessage.VAR__VM_STATUS__UP);
        statusToMessage.put(VMStatus.PoweringUp, EngineMessage.VAR__VM_STATUS__POWERING_UP);
        statusToMessage.put(VMStatus.Paused, EngineMessage.VAR__VM_STATUS__PAUSED);
        statusToMessage.put(VMStatus.MigratingFrom, EngineMessage.VAR__VM_STATUS__MIGRATING);
        statusToMessage.put(VMStatus.MigratingTo, EngineMessage.VAR__VM_STATUS__MIGRATING);
        statusToMessage.put(VMStatus.Unknown, EngineMessage.VAR__VM_STATUS__UNKNOWN);
        statusToMessage.put(VMStatus.NotResponding, EngineMessage.VAR__VM_STATUS__NOT_RESPONDING);
        statusToMessage.put(VMStatus.WaitForLaunch, EngineMessage.VAR__VM_STATUS__WAIT_FOR_LAUNCH);
        statusToMessage.put(VMStatus.RebootInProgress, EngineMessage.VAR__VM_STATUS__REBOOT_IN_PROGRESS);
        statusToMessage.put(VMStatus.SavingState, EngineMessage.VAR__VM_STATUS__SAVING_STATE);
        statusToMessage.put(VMStatus.RestoringState, EngineMessage.VAR__VM_STATUS__RESTORING_STATE);
        statusToMessage.put(VMStatus.Suspended, EngineMessage.VAR__VM_STATUS__SUSPENDED);
        statusToMessage.put(VMStatus.ImageIllegal, EngineMessage.VAR__VM_STATUS__IMAGE_ILLEGAL);
        statusToMessage.put(VMStatus.ImageLocked, EngineMessage.VAR__VM_STATUS__IMAGE_LOCKED);
        statusToMessage.put(VMStatus.PoweringDown, EngineMessage.VAR__VM_STATUS__POWERING_DOWN);
    }

    public static final String from(VMStatus status) {
        if (statusToMessage.containsKey(status)) {
            return statusToMessage.get(status).name();
        }

        return "";
    }

}
