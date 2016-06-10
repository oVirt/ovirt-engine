package org.ovirt.engine.core.bll.gluster;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.bll.CommandBase;
import org.ovirt.engine.core.bll.LockMessagesMatchUtil;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.interfaces.BackendInternal;
import org.ovirt.engine.core.bll.utils.GlusterUtil;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;
import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VdsStatic;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterBrickEntity;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.businessentities.network.VdsNetworkInterface;
import org.ovirt.engine.core.common.constants.gluster.GlusterConstants;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.locks.LockingGroup;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.network.InterfaceDao;
import org.ovirt.engine.core.dao.network.NetworkDao;

/**
 * Base class for all Gluster commands
 */
public abstract class GlusterCommandBase<T extends VdcActionParametersBase> extends CommandBase<T> {
    protected AuditLogType errorType;
    protected VDS upServer;
    private Network glusterNetwork;

    public GlusterCommandBase(T params, CommandContext commandContext) {
        super(params, commandContext);
    }

    @Override
    protected BackendInternal getBackend() {
        return super.getBackend();
    }

    @Override
    protected Map<String, Pair<String, String>> getExclusiveLocks() {
        if (!isInternalExecution()) {
            return Collections.singletonMap(getClusterId().toString(),
                    LockMessagesMatchUtil.makeLockingPair(LockingGroup.GLUSTER, EngineMessage.ACTION_TYPE_FAILED_GLUSTER_OPERATION_INPROGRESS));
        }
        return super.getExclusiveLocks();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.ovirt.engine.core.bll.CommandBase#getPermissionCheckSubjects()
     */
    @Override
    public List<PermissionSubject> getPermissionCheckSubjects() {
        // By default, check permissions at cluster level. Commands that need
        // more granular permissions can override this method.
        return Collections.singletonList(new PermissionSubject(getClusterId(),
                VdcObjectType.Cluster,
                getActionType().getActionGroup()));
    }

    @Override
    public Map<String, String> getJobMessageProperties() {
        if (jobProperties == null) {
            jobProperties = new HashMap<>();
            Cluster cluster = getCluster();
            jobProperties.put(GlusterConstants.CLUSTER, cluster == null ? null : cluster.getName());
        }

        return jobProperties;
    }

    /**
     * This server is chosen as random from all the Up servers.
     *
     * @return One of the servers in up status
     */
    protected VDS getUpServer() {
        return getGlusterUtils().getRandomUpServer(getClusterId());
    }

    protected GlusterUtil getGlusterUtils() {
        return GlusterUtil.getInstance();
    }

    @Override
    protected boolean validate() {
        if (!super.validate()) {
            return false;
        }

        upServer = getUpServer();
        if (upServer == null) {
            addValidationMessageVariable("clusterName", getCluster().getName());
            addValidationMessage(EngineMessage.ACTION_TYPE_FAILED_NO_UP_SERVER_FOUND);
            return false;
        }
        return true;
    }

    protected void handleVdsErrors(AuditLogType errType, List<String> errors) {
        propagateFailure(errType, errors);
        // Setting Error to null to make the FrontendErrorHandler to use the Message which is being set above instead of
        // the EngineError.ENGINE(which will get translated to "Internal engine error")
        getReturnValue().getFault().setError(null);
    }

    protected void propagateFailure(AuditLogType errType, List<String> errors) {
        errorType = errType;
        getReturnValue().getExecuteFailedMessages().addAll(errors);
        getReturnValue().getFault().setMessage(StringUtils.join(errors, System.lineSeparator()));
    }

    protected void handleVdsError(AuditLogType errType, String error) {
        errorType = errType;
        getReturnValue().getExecuteFailedMessages().add(error);
        getReturnValue().getFault().setMessage(error);
        // Setting Error to null to make the FrontendErrorHandler to use the Message which is being set above instead of
        // the EngineError.ENGINE(which will get translated to "Internal engine error")
        getReturnValue().getFault().setError(null);
    }

    protected boolean evaluateReturnValue(AuditLogType auditLogType, VdcReturnValueBase returnValue) {
        boolean succeeded = returnValue.isValid();
        if (!succeeded) {
            handleVdsErrors(auditLogType, returnValue.getValidationMessages());
        }
        succeeded = succeeded && returnValue.getSucceeded();
        if (!succeeded) {
            handleVdsErrors(auditLogType, returnValue.getExecuteFailedMessages());
        }
        return succeeded;
    }

    protected boolean evaluateReturnValue(AuditLogType auditLogType, VDSReturnValue returnValue) {
        boolean succeeded = true;
        succeeded = returnValue.getSucceeded();
        if (!succeeded) {
            handleVdsError(auditLogType, returnValue.getVdsError().getMessage());
        }
        return succeeded;
    }

    protected boolean updateBrickServerAndInterfaceNames(List<GlusterBrickEntity> bricks, boolean addValidationMessage) {
        for (GlusterBrickEntity brick : bricks) {
            if (!updateBrickServerAndInterfaceName(brick, addValidationMessage)) {
                return false;
            }
        }
        return true;
    }

    protected boolean updateBrickServerAndInterfaceName(GlusterBrickEntity brick, boolean addValidationMessage) {
        VdsStatic server = getVdsStaticDao().get(brick.getServerId());
        if (server == null || !server.getClusterId().equals(getClusterId())) {
            if (addValidationMessage) {
                addValidationMessage(EngineMessage.ACTION_TYPE_FAILED_INVALID_BRICK_SERVER_ID);
            }
            return false;
        }
        brick.setServerName(server.getHostName());
        // No interface has been selected to use for brick-
        // engine will get the gluster network, if present
        if (brick.getNetworkId() == null) {
            Network network = getGlusterNetwork();
            if (network != null) {
                brick.setNetworkId(network.getId());
                brick.setNetworkAddress(getGlusterNetworkAddress(server.getId(), network.getName()));
            }
        } else {
            // network id has been set, update the address
            Network network = getNetworkDao().get(brick.getNetworkId());
            if (network != null) {
                brick.setNetworkAddress(getGlusterNetworkAddress(server.getId(), network.getName()));
            }
        }

        return true;
    }

    private Network getGlusterNetwork() {
        if (glusterNetwork == null) {
            List<Network> allNetworksInCluster = getNetworkDao().getAllForCluster(getClusterId());

            for (Network network : allNetworksInCluster) {
                if (network.getCluster().isGluster()) {
                    glusterNetwork = network;
                    return glusterNetwork;
                }
            }
        }
        return glusterNetwork;
    }

    private String getGlusterNetworkAddress(Guid hostId, String glusterNetworkName) {
        final List<VdsNetworkInterface> nics =
                getInterfaceDao().getAllInterfacesForVds(hostId);

        for (VdsNetworkInterface nic : nics) {
            if (glusterNetworkName.equals(nic.getNetworkName())) {
                return nic.getIpv4Address();
            }
        }
        return null;
    }

    protected boolean validateDuplicateBricks(List<GlusterBrickEntity> newBricks) {
        Set<String> bricks = new HashSet<>();
        for (GlusterBrickEntity brick : newBricks) {
            if (bricks.contains(brick.getQualifiedName())) {
                addValidationMessage(EngineMessage.ACTION_TYPE_FAILED_DUPLICATE_BRICKS);
                addValidationMessageVariable("brick", brick.getQualifiedName());
                return false;
            }
            bricks.add(brick.getQualifiedName());

            GlusterBrickEntity existingBrick =
                    getGlusterBrickDao().getBrickByServerIdAndDirectory(brick.getServerId(), brick.getBrickDirectory());
            if (existingBrick != null) {
                addValidationMessage(EngineMessage.ACTION_TYPE_FAILED_BRICK_ALREADY_EXISTS_IN_VOLUME);
                addValidationMessageVariable("brick", brick.getQualifiedName());
                addValidationMessageVariable("volumeName",
                        getGlusterVolumeDao().getById(existingBrick.getVolumeId()).getName());
                return false;
            }
        }
        return true;
    }

    @Override
    protected InterfaceDao getInterfaceDao() {
        return getDbFacade().getInterfaceDao();
    }

    @Override
    // overriding for Junit visibility
    protected NetworkDao getNetworkDao() {
        return super.getNetworkDao();
    }

}
