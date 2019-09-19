package org.ovirt.engine.core.common.validation;

import java.util.HashSet;
import java.util.Set;

import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.businessentities.VM;

public class VmActionByVmOriginTypeValidator {
    private static Set<ActionType> COMMANDS_ALLOWED_ON_EXTERNAL_VMS = new HashSet<>();
    private static Set<ActionType> COMMANDS_ALLOWED_ON_HOSTED_ENGINE = new HashSet<>();

    static {
        COMMANDS_ALLOWED_ON_EXTERNAL_VMS.add(ActionType.MigrateVm);
        COMMANDS_ALLOWED_ON_EXTERNAL_VMS.add(ActionType.MigrateVmToServer);
        COMMANDS_ALLOWED_ON_EXTERNAL_VMS.add(ActionType.CancelMigrateVm);
        COMMANDS_ALLOWED_ON_EXTERNAL_VMS.add(ActionType.SetVmTicket);
        COMMANDS_ALLOWED_ON_EXTERNAL_VMS.add(ActionType.VmLogon);
        COMMANDS_ALLOWED_ON_EXTERNAL_VMS.add(ActionType.StopVm);
        COMMANDS_ALLOWED_ON_EXTERNAL_VMS.add(ActionType.ShutdownVm);
        COMMANDS_ALLOWED_ON_EXTERNAL_VMS.add(ActionType.RemoveVm);
        COMMANDS_ALLOWED_ON_EXTERNAL_VMS.add(ActionType.RebootVm);

        COMMANDS_ALLOWED_ON_HOSTED_ENGINE.add(ActionType.BalanceVm);
        COMMANDS_ALLOWED_ON_HOSTED_ENGINE.add(ActionType.MigrateVm);
        COMMANDS_ALLOWED_ON_HOSTED_ENGINE.add(ActionType.MigrateVmToServer);
        COMMANDS_ALLOWED_ON_HOSTED_ENGINE.add(ActionType.CancelMigrateVm);
        COMMANDS_ALLOWED_ON_HOSTED_ENGINE.add(ActionType.SetVmTicket);
        COMMANDS_ALLOWED_ON_HOSTED_ENGINE.add(ActionType.VmLogon);
        COMMANDS_ALLOWED_ON_HOSTED_ENGINE.add(ActionType.UpdateVm);
        COMMANDS_ALLOWED_ON_HOSTED_ENGINE.add(ActionType.UpdateDisk);
        COMMANDS_ALLOWED_ON_HOSTED_ENGINE.add(ActionType.RemoveVm);
        /**
         * Needed specifically to move the HE VM out of a cluster in order to increase its cluster
         * compatibility version.
         */
        COMMANDS_ALLOWED_ON_HOSTED_ENGINE.add(ActionType.ChangeVMCluster);

        COMMANDS_ALLOWED_ON_HOSTED_ENGINE.add(ActionType.AddVmInterface);
        COMMANDS_ALLOWED_ON_HOSTED_ENGINE.add(ActionType.RemoveVmInterface);
        COMMANDS_ALLOWED_ON_HOSTED_ENGINE.add(ActionType.UpdateVmInterface);
        COMMANDS_ALLOWED_ON_HOSTED_ENGINE.add(ActionType.ActivateDeactivateVmNic);
    }

    public static boolean isCommandAllowed(VM vm, ActionType actionType) {
        return !( (vm.isHostedEngine() && !COMMANDS_ALLOWED_ON_HOSTED_ENGINE.contains(actionType)) ||
                (vm.isExternalVm() && !COMMANDS_ALLOWED_ON_EXTERNAL_VMS.contains(actionType)) );
    }
}
