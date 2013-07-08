package org.ovirt.engine.core.bll.gluster;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.ovirt.engine.core.bll.Backend;
import org.ovirt.engine.core.bll.QueriesCommandBase;
import org.ovirt.engine.core.bll.interfaces.BackendInternal;
import org.ovirt.engine.core.bll.utils.ClusterUtils;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterServerInfo;
import org.ovirt.engine.core.common.businessentities.gluster.PeerStatus;
import org.ovirt.engine.core.common.interfaces.VDSBrokerFrontend;
import org.ovirt.engine.core.common.queries.ServerParameters;
import org.ovirt.engine.core.common.queries.VdcQueryReturnValue;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.common.queries.gluster.AddedGlusterServersParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.common.vdscommands.VdsIdVDSCommandParametersBase;
import org.ovirt.engine.core.dao.gluster.GlusterDBUtils;

/**
 * Query to get Added Gluster Servers with/without server ssh key fingerprint
 */
public class GetAddedGlusterServersQuery<P extends AddedGlusterServersParameters> extends QueriesCommandBase<P> {
    public GetAddedGlusterServersQuery(P params) {
        super(params);
    }

    @Override
    @SuppressWarnings("unchecked")
    protected void executeQueryCommand() {
        Map<String, String> glusterServers = new HashMap<String, String>();
        VDS upServer = getClusterUtils().getUpServer(getParameters().getClusterId());

        if(upServer != null ) {
            VDSReturnValue returnValue = getResourceManager().RunVdsCommand(VDSCommandType.GlusterServersList,
                            new VdsIdVDSCommandParametersBase(upServer.getId()));
            glusterServers = getAddedGlusterServers((List<GlusterServerInfo>) returnValue.getReturnValue());
        }
        getQueryReturnValue().setReturnValue(glusterServers);
    }

    private Map<String, String> getAddedGlusterServers(List<GlusterServerInfo> glusterServers) {
        Map<String, String> serversAndFingerprint = new HashMap<String, String>();

        for (GlusterServerInfo server : glusterServers) {
            if (server.getStatus() == PeerStatus.CONNECTED && (!serverExists(server))) {
                String fingerprint = null;
                VdcQueryReturnValue returnValue;
                if (getParameters().isServerKeyFingerprintRequired()) {
                    returnValue = getBackendInstance().
                            runInternalQuery(VdcQueryType.GetServerSSHKeyFingerprint,
                                             new ServerParameters(server.getHostnameOrIp()));
                    if (returnValue != null && returnValue.getSucceeded()) {
                        fingerprint = returnValue.getReturnValue().toString();
                    }
                }
                serversAndFingerprint.put(
                    server.getHostnameOrIp(),
                    fingerprint == null ? "" : fingerprint
                );
            }
        }
        return serversAndFingerprint;
    }

    protected GlusterDBUtils getDbUtils() {
        return GlusterDBUtils.getInstance();
    }

    private boolean serverExists(GlusterServerInfo glusterServer) {
        return getDbUtils().serverExists(getParameters().getClusterId(), glusterServer.getHostnameOrIp());
    }

    public ClusterUtils getClusterUtils() {
        return ClusterUtils.getInstance();
    }

    public VDSBrokerFrontend getResourceManager() {
        return Backend.getInstance()
                .getResourceManager();
    }

    public BackendInternal getBackendInstance() {
        return Backend.getInstance();
    }
}
