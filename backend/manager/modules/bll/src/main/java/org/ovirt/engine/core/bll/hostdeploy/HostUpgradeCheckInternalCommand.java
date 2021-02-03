package org.ovirt.engine.core.bll.hostdeploy;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.NonTransactiveCommandAttribute;
import org.ovirt.engine.core.bll.VdsCommand;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.validator.UpgradeHostValidator;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.HostUpgradeManagerResult;
import org.ovirt.engine.core.common.action.VdsActionParameters;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.compat.CommandStatus;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.VdsDao;

@NonTransactiveCommandAttribute
public class HostUpgradeCheckInternalCommand<T extends VdsActionParameters> extends VdsCommand<T> {

    @Inject
    private VdsDao vdsDao;

    @Inject
    private HostUpdatesChecker hostUpdatesChecker;

    /**
     * C'tor for compensation purposes
     *
     * @param commandId
     *            the command id
     */
    public HostUpgradeCheckInternalCommand(Guid commandId) {
        super(commandId);
    }

    public HostUpgradeCheckInternalCommand(T parameters, CommandContext cmdContext) {
        super(parameters, cmdContext);
    }

    @Override
    protected boolean validate() {
        UpgradeHostValidator validator = new UpgradeHostValidator(getVds(), getCluster());

        return validate(validator.hostExists())
                && validate(validator.statusSupportedForHostUpgradeCheck());
    }

    @Override
    protected void executeCommand() {
        HostUpgradeManagerResult hostUpgradeManagerResult = hostUpdatesChecker.checkForUpdates(vdsDao.get(getVdsId()));
        getReturnValue().setActionReturnValue(hostUpgradeManagerResult);
        setSucceeded(hostUpgradeManagerResult != null);
        setCommandStatus(hostUpgradeManagerResult != null ? CommandStatus.SUCCEEDED : CommandStatus.FAILED);
    }

    @Override
    protected void setActionMessageParameters() {
        addValidationMessage(EngineMessage.VAR__ACTION__UPGRADE__CHECK);
        addValidationMessage(EngineMessage.VAR__TYPE__HOST);
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        return AuditLogType.UNASSIGNED;
    }
}
