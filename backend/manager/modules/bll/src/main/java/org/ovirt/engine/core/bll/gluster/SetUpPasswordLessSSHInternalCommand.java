package org.ovirt.engine.core.bll.gluster;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;

import org.ovirt.engine.core.bll.InternalCommandAttribute;
import org.ovirt.engine.core.bll.NonTransactiveCommandAttribute;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;
import org.ovirt.engine.core.common.action.gluster.SetUpPasswordLessSSHParameters;
import org.ovirt.engine.core.common.action.gluster.UpdateGlusterHostPubKeyToSlaveParameters;
import org.ovirt.engine.core.common.constants.gluster.GlusterConstants;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryReturnValue;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.utils.threadpool.ThreadPoolUtil;

@InternalCommandAttribute
@NonTransactiveCommandAttribute
public class SetUpPasswordLessSSHInternalCommand extends GlusterCommandBase<SetUpPasswordLessSSHParameters> {

    public SetUpPasswordLessSSHInternalCommand(SetUpPasswordLessSSHParameters params, CommandContext commandContext) {
        super(params, commandContext);
        setClusterId(getParameters().getId());
    }

    @SuppressWarnings("unchecked")
    private List<String> readPubKey(Guid upServerId) {
        VdcQueryReturnValue readPubKeyReturnvalue =
                runInternalQuery(VdcQueryType.GetGlusterHostPublicKeys, new IdQueryParameters(upServerId));
        if (readPubKeyReturnvalue.getSucceeded()) {
            return (List<String>) readPubKeyReturnvalue.getReturnValue();
        } else {
            propagateFailure(readPubKeyReturnvalue);
            return null;
        }
    }

    private List<VdcReturnValueBase> updatePubKeysToRemoteHosts(final List<String> pubKeys,
            Set<Guid> remoteServersSet,
            final String userName) {
        List<Callable<VdcReturnValueBase>> slaveWritePubKeyList = new ArrayList<>();
        for (final Guid currentRemoteHostId : remoteServersSet) {
            slaveWritePubKeyList.add(() -> {
                String currentHostNameToLog = getCustomValue(GlusterConstants.VDS_NAME);
                getCustomValues().remove(currentHostNameToLog);
                addCustomValue(GlusterConstants.VDS_NAME, getVdsDao().get(currentRemoteHostId).getName());
                return getBackend().runInternalAction(VdcActionType.UpdateGlusterHostPubKeyToSlaveInternal,
                        new UpdateGlusterHostPubKeyToSlaveParameters(currentRemoteHostId,
                                pubKeys, userName));
            });
        }
        List<VdcReturnValueBase> returnStatuses = ThreadPoolUtil.invokeAll(slaveWritePubKeyList);
        return returnStatuses;
    }

    @Override
    protected void executeCommand() {
        List<String> pubKeys = readPubKey(upServer.getId());
        boolean succeeded = true;
        boolean canProceed = pubKeys != null && pubKeys.size() > 0;
        if (canProceed) {
            List<String> errors = new ArrayList<>();
            List<VdcReturnValueBase> updateKeyReturnValues = updatePubKeysToRemoteHosts(pubKeys, getParameters().getDestinationHostIds(), getParameters().getUserName());
            for(VdcReturnValueBase currentReturnValue : updateKeyReturnValues) {
                if (!currentReturnValue.getSucceeded()) {
                    errors.addAll(currentReturnValue.getExecuteFailedMessages());
                }
            }
            if (!errors.isEmpty()) {
                succeeded = false;
                propagateFailure(AuditLogType.GLUSTER_GEOREP_PUBLIC_KEY_WRITE_FAILED, errors);
            }
            setSucceeded(succeeded);
        }
    }
}
