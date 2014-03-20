package org.ovirt.engine.core.bll.gluster;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.bll.NonTransactiveCommandAttribute;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.LockProperties;
import org.ovirt.engine.core.common.action.LockProperties.Scope;
import org.ovirt.engine.core.common.action.gluster.RemoveGlusterServerParameters;
import org.ovirt.engine.core.common.errors.VdcBllMessages;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.common.vdscommands.gluster.RemoveGlusterServerVDSParameters;

/**
 * BLL command for gluster peer detach
 */
@NonTransactiveCommandAttribute
public class RemoveGlusterServerCommand extends GlusterCommandBase<RemoveGlusterServerParameters> {

    public RemoveGlusterServerCommand(RemoveGlusterServerParameters params) {
        super(params);
        setVdsGroupId(getParameters().getClusterId());
        setVdsName(getParameters().getHostnameOrIp());
    }

    @Override
    protected LockProperties applyLockProperties(LockProperties lockProperties) {
        return lockProperties.withScope(Scope.Execution).withWait(true);
    }

    @Override
    protected void setActionMessageParameters() {
        addCanDoActionMessage(VdcBllMessages.VAR__ACTION__REMOVE);
        addCanDoActionMessage(VdcBllMessages.VAR__TYPE__GLUSTER_SERVER);
    }

    @Override
    protected boolean canDoAction() {
        super.canDoAction();
        if (StringUtils.isEmpty(getParameters().getHostnameOrIp())) {
            addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_SERVER_NAME_REQUIRED);
            return false;
        }
        return true;
    }

    @Override
    protected void executeCommand() {
        VDSReturnValue returnValue =
                runVdsCommand(VDSCommandType.RemoveGlusterServer,
                        new RemoveGlusterServerVDSParameters(upServer.getId(),
                                getParameters().getHostnameOrIp(),
                                getParameters().isForceAction()));
        setSucceeded(returnValue.getSucceeded());
        if (!getSucceeded()) {
            handleVdsError(AuditLogType.GLUSTER_SERVER_REMOVE_FAILED, returnValue.getVdsError().getMessage());
        }
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        if (getSucceeded()) {
            return AuditLogType.GLUSTER_SERVER_REMOVE;
        } else {
            return AuditLogType.GLUSTER_SERVER_REMOVE_FAILED;
        }
    }

}
