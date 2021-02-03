package org.ovirt.engine.core.bll.hostdeploy;

import java.util.Collections;

import javax.enterprise.inject.Instance;
import javax.enterprise.inject.Typed;
import javax.inject.Inject;

import org.ovirt.engine.core.bll.NonTransactiveCommandAttribute;
import org.ovirt.engine.core.bll.VdsCommand;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.tasks.CommandCoordinatorUtil;
import org.ovirt.engine.core.bll.tasks.interfaces.CommandCallback;
import org.ovirt.engine.core.bll.validator.UpgradeHostValidator;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.MaintenanceNumberOfVdssParameters;
import org.ovirt.engine.core.common.action.hostdeploy.UpgradeHostParameters;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.core.common.errors.EngineMessage;

@NonTransactiveCommandAttribute
public class UpgradeHostCommand<T extends UpgradeHostParameters> extends VdsCommand<T> {

    @Inject
    private CommandCoordinatorUtil commandCoordinatorUtil;
    @Inject
    @Typed(HostUpgradeCallback.class)
    private Instance<HostUpgradeCallback> callbackProvider;

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
        UpgradeHostValidator validator = new UpgradeHostValidator(getVds(), getCluster());

        return validate(validator.hostExists())
                && validate(validator.statusSupportedForHostUpgrade())
                && validate(validator.updatesAvailable())
                && validate(validator.clusterCpuSecureAndNotAffectedByTsxRemoval());
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
            commandCoordinatorUtil.executeAsyncCommand(ActionType.MaintenanceNumberOfVdss,
                    createMaintenanceParams(),
                    cloneContext());
        }

        setSucceeded(true);
    }

    public MaintenanceNumberOfVdssParameters createMaintenanceParams() {
        MaintenanceNumberOfVdssParameters params =
                new MaintenanceNumberOfVdssParameters(Collections.singletonList(getVdsId()),
                         true, "", getVds().getClusterSupportsGlusterService());
        return withRootCommandInfo(params);
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        return getSucceeded() ? AuditLogType.HOST_UPGRADE_STARTED : AuditLogType.HOST_UPGRADE_FAILED;
    }

    @Override
    public CommandCallback getCallback() {
        return callbackProvider.get();
    }
}
