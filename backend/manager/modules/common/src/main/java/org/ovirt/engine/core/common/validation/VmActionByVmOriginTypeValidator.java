package org.ovirt.engine.core.common.validation;

import java.util.HashSet;
import java.util.Set;

import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.businessentities.VM;

public class VmActionByVmOriginTypeValidator {
    private static Set<VdcActionType> COMMANDS_ALLOWED_ON_EXTERNAL_VMS = new HashSet<>();
    private static Set<VdcActionType> COMMANDS_ALLOWED_ON_HOSTED_ENGINE = new HashSet<>();

    static {
        COMMANDS_ALLOWED_ON_EXTERNAL_VMS.add(VdcActionType.MigrateVm);
        COMMANDS_ALLOWED_ON_EXTERNAL_VMS.add(VdcActionType.MigrateVmToServer);
        COMMANDS_ALLOWED_ON_EXTERNAL_VMS.add(VdcActionType.InternalMigrateVm);
        COMMANDS_ALLOWED_ON_EXTERNAL_VMS.add(VdcActionType.CancelMigrateVm);
        COMMANDS_ALLOWED_ON_EXTERNAL_VMS.add(VdcActionType.SetVmTicket);
        COMMANDS_ALLOWED_ON_EXTERNAL_VMS.add(VdcActionType.VmLogon);
        COMMANDS_ALLOWED_ON_EXTERNAL_VMS.add(VdcActionType.StopVm);
        COMMANDS_ALLOWED_ON_EXTERNAL_VMS.add(VdcActionType.ShutdownVm);
        COMMANDS_ALLOWED_ON_EXTERNAL_VMS.add(VdcActionType.RemoveVm);
        COMMANDS_ALLOWED_ON_EXTERNAL_VMS.add(VdcActionType.RebootVm);

        COMMANDS_ALLOWED_ON_HOSTED_ENGINE.add(VdcActionType.MigrateVm);
        COMMANDS_ALLOWED_ON_HOSTED_ENGINE.add(VdcActionType.MigrateVmToServer);
        COMMANDS_ALLOWED_ON_HOSTED_ENGINE.add(VdcActionType.InternalMigrateVm);
        COMMANDS_ALLOWED_ON_HOSTED_ENGINE.add(VdcActionType.CancelMigrateVm);
        COMMANDS_ALLOWED_ON_HOSTED_ENGINE.add(VdcActionType.SetVmTicket);
        COMMANDS_ALLOWED_ON_HOSTED_ENGINE.add(VdcActionType.VmLogon);
        COMMANDS_ALLOWED_ON_HOSTED_ENGINE.add(VdcActionType.UpdateVm);
        COMMANDS_ALLOWED_ON_HOSTED_ENGINE.add(VdcActionType.RemoveVm);
    }

    public static boolean isCommandAllowed(VM vm, VdcActionType actionType) {
        return !( (vm.isHostedEngine() && !COMMANDS_ALLOWED_ON_HOSTED_ENGINE.contains(actionType)) ||
                (vm.isExternalVm() && !COMMANDS_ALLOWED_ON_EXTERNAL_VMS.contains(actionType)) );
    }
}
