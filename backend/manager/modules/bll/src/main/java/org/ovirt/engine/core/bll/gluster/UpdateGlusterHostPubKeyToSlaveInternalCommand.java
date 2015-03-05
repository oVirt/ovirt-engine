package org.ovirt.engine.core.bll.gluster;

import org.ovirt.engine.core.bll.InternalCommandAttribute;
import org.ovirt.engine.core.bll.NonTransactiveCommandAttribute;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.gluster.UpdateGlusterHostPubKeyToSlaveParameters;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.errors.VdcBllMessages;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.common.vdscommands.gluster.UpdateGlusterGeoRepKeysVDSParameters;

@InternalCommandAttribute
@NonTransactiveCommandAttribute
public class UpdateGlusterHostPubKeyToSlaveInternalCommand extends GlusterCommandBase<UpdateGlusterHostPubKeyToSlaveParameters> {

    public UpdateGlusterHostPubKeyToSlaveInternalCommand(UpdateGlusterHostPubKeyToSlaveParameters params) {
        this(params, null);
    }

    public UpdateGlusterHostPubKeyToSlaveInternalCommand(UpdateGlusterHostPubKeyToSlaveParameters params, CommandContext commandContext) {
        super(params, commandContext);
        setVdsId(getParameters().getId());
    }

    @Override
    protected VDS getUpServer() {
        return getVdsDAO().get(getParameters().getId());
    }

    @Override
    protected boolean canDoAction() {
        if (getParameters().getPubKeys().isEmpty()) {
            return failCanDoAction(VdcBllMessages.ACTION_TYPE_FAILED_GLUSTER_NO_PUB_KEYS_PASSED);
        }
        if (getParameters().getId() == null) {
            return failCanDoAction(VdcBllMessages.ACTION_TYPE_FAILED_HOST_NOT_EXIST);
        }
        return super.canDoAction();
    }

    @Override
    protected void setActionMessageParameters() {
        addCanDoActionMessage(VdcBllMessages.VAR__ACTION__WRITE_PUB_KEYS);
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        if (getSucceeded()) {
            return AuditLogType.GLUSTER_WRITE_PUB_KEYS;
        } else {
            return errorType == null ? AuditLogType.GLUSTER_GEOREP_PUBLIC_KEY_WRITE_FAILED : errorType;
        }
    }

    @Override
    protected void executeCommand() {
        VDSReturnValue writePubKeysReturnValue =
                runVdsCommand(VDSCommandType.UpdateGlusterGeoRepKeys,
                        new UpdateGlusterGeoRepKeysVDSParameters(getParameters().getId(),
                                getParameters().getPubKeys(),
                                getParameters().getRemoteUserName()));
        setSucceeded(writePubKeysReturnValue.getSucceeded());
        if (!writePubKeysReturnValue.getSucceeded()) {
            handleVdsError(AuditLogType.GLUSTER_GEOREP_PUBLIC_KEY_WRITE_FAILED, writePubKeysReturnValue.getVdsError()
                    .getMessage());
            return;
        }
    }

}
