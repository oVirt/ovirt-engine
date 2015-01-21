package org.ovirt.engine.core.bll.validator;

import java.util.EnumMap;
import java.util.Map;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.errors.VdcBllMessages;

public class LocalizedVmStatus {

    private static final Map<VMStatus, VdcBllMessages> statusToMessage;

    static {
        statusToMessage = new EnumMap(VMStatus.class);
        statusToMessage.put(VMStatus.Unassigned, VdcBllMessages.VAR__VM_STATUS__UNASSIGNED);
        statusToMessage.put(VMStatus.Down, VdcBllMessages.VAR__VM_STATUS__DOWN);
        statusToMessage.put(VMStatus.Up, VdcBllMessages.VAR__VM_STATUS__UP);
        statusToMessage.put(VMStatus.PoweringUp, VdcBllMessages.VAR__VM_STATUS__POWERING_UP);
        statusToMessage.put(VMStatus.Paused, VdcBllMessages.VAR__VM_STATUS__PAUSED);
        statusToMessage.put(VMStatus.MigratingFrom, VdcBllMessages.VAR__VM_STATUS__MIGRATING);
        statusToMessage.put(VMStatus.MigratingTo, VdcBllMessages.VAR__VM_STATUS__MIGRATING);
        statusToMessage.put(VMStatus.Unknown, VdcBllMessages.VAR__VM_STATUS__UNKNOWN);
        statusToMessage.put(VMStatus.NotResponding, VdcBllMessages.VAR__VM_STATUS__NOT_RESPONDING);
        statusToMessage.put(VMStatus.WaitForLaunch, VdcBllMessages.VAR__VM_STATUS__WAIT_FOR_LAUNCH);
        statusToMessage.put(VMStatus.RebootInProgress, VdcBllMessages.VAR__VM_STATUS__REBOOT_IN_PROGRESS);
        statusToMessage.put(VMStatus.SavingState, VdcBllMessages.VAR__VM_STATUS__SAVING_STATE);
        statusToMessage.put(VMStatus.RestoringState, VdcBllMessages.VAR__VM_STATUS__RESTORING_STATE);
        statusToMessage.put(VMStatus.Suspended, VdcBllMessages.VAR__VM_STATUS__SUSPENDED);
        statusToMessage.put(VMStatus.ImageIllegal, VdcBllMessages.VAR__VM_STATUS__IMAGE_ILLEGAL);
        statusToMessage.put(VMStatus.ImageLocked, VdcBllMessages.VAR__VM_STATUS__IMAGE_LOCKED);
        statusToMessage.put(VMStatus.PoweringDown, VdcBllMessages.VAR__VM_STATUS__POWERING_DOWN);
    }

    public static final String from(VMStatus status) {
        if (statusToMessage.containsKey(status)) {
            return statusToMessage.get(status).name();
        }

        return "";
    }

}
