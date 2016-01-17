package org.ovirt.engine.core.bll.hostdeploy;

import java.util.Collections;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.ovirt.engine.core.bll.NonTransactiveCommandAttribute;
import org.ovirt.engine.core.bll.VdsCommand;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.tasks.CommandCoordinatorUtil;
import org.ovirt.engine.core.bll.tasks.interfaces.CommandCallback;
import org.ovirt.engine.core.bll.validator.UpgradeHostValidator;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.MaintenanceNumberOfVdssParameters;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;
import org.ovirt.engine.core.common.action.hostdeploy.UpgradeHostParameters;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.core.common.errors.EngineMessage;

@NonTransactiveCommandAttribute
public class UpgradeHostCommand<T extends UpgradeHostParameters> extends VdsCommand<T> {

    public UpgradeHostCommand(T parameters, CommandContext cmdContext) {
        super(parameters, cmdContext);
    }

    @Override
    protected void init() {
        if (getParameters().getInitialStatus() == null) {
            if (getVds() != null) {
                getParameters().setInitialStatus(getVds().getStatus());
            }
        }
    }

    @Override
    protected boolean validate() {
        UpgradeHostValidator validator = new UpgradeHostValidator(getVds());

        return validate(validator.hostExists())
                && validate(validator.statusSupportedForHostUpgrade())
                && validate(validator.updatesAvailable())
                && validate(validator.imageProvidedForOvirtNode(getParameters().getoVirtIsoFile()));
    }

    @Override
    protected void setActionMessageParameters() {
        addValidationMessage(EngineMessage.VAR__ACTION__UPGRADE);
        addValidationMessage(EngineMessage.VAR__TYPE__HOST);
    }

    @Override
    protected void executeCommand() {
        VDSStatus statusBeforeUpgrade = getVds().getStatus();
        if (statusBeforeUpgrade != VDSStatus.Maintenance) {
            Future<VdcReturnValueBase> maintenanceCmd =
                    CommandCoordinatorUtil.executeAsyncCommand(VdcActionType.MaintenanceNumberOfVdss,
                            createMaintenanceParams(),
                            cloneContext());

            VdcReturnValueBase result;
            try {
                result = maintenanceCmd.get();
                if (!result.getSucceeded()) {
                    propagateFailure(result);
                    return;
                }
            } catch (InterruptedException | ExecutionException e) {
                log.error("Exception", e);
                return;
            }
        }

        setSucceeded(true);
    }

    public MaintenanceNumberOfVdssParameters createMaintenanceParams() {
        MaintenanceNumberOfVdssParameters params =
                new MaintenanceNumberOfVdssParameters(Collections.singletonList(getVdsId()), true);
        return withRootCommandInfo(params);
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        return getSucceeded() ? AuditLogType.HOST_UPGRADE_STARTED : AuditLogType.HOST_UPGRADE_FAILED;
    }

    @Override
    public CommandCallback getCallback() {
        return new HostUpgradeCallback();
    }
}
