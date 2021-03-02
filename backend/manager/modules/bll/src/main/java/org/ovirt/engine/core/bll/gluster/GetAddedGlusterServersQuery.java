package org.ovirt.engine.core.bll.gluster;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.QueriesCommandBase;
import org.ovirt.engine.core.bll.context.EngineContext;
import org.ovirt.engine.core.bll.utils.GlusterUtil;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterServerInfo;
import org.ovirt.engine.core.common.businessentities.gluster.PeerStatus;
import org.ovirt.engine.core.common.queries.QueryReturnValue;
import org.ovirt.engine.core.common.queries.QueryType;
import org.ovirt.engine.core.common.queries.ServerParameters;
import org.ovirt.engine.core.common.queries.gluster.AddedGlusterServersParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.common.vdscommands.VdsIdVDSCommandParametersBase;
import org.ovirt.engine.core.dao.gluster.GlusterDBUtils;

/**
 * Query to get Added Gluster Servers with/without server ssh public key
 */
public class GetAddedGlusterServersQuery<P extends AddedGlusterServersParameters> extends QueriesCommandBase<P> {
    @Inject
    private GlusterUtil glusterUtil;

    @Inject
    private GlusterDBUtils glusterDBUtils;

    public GetAddedGlusterServersQuery(P parameters, EngineContext engineContext) {
        super(parameters, engineContext);
    }

    @Override
    @SuppressWarnings("unchecked")
    protected void executeQueryCommand() {
        Map<String, String> glusterServers = new HashMap<>();
        VDS upServer = glusterUtil.getUpServer(getParameters().getClusterId());

        if(upServer != null ) {
            VDSReturnValue returnValue = runVdsCommand(VDSCommandType.GlusterServersList,
                    new VdsIdVDSCommandParametersBase(upServer.getId()));
            glusterServers = getAddedGlusterServers((List<GlusterServerInfo>) returnValue.getReturnValue());
        }

        getQueryReturnValue().setReturnValue(glusterServers);
    }

    private Map<String, String> getAddedGlusterServers(List<GlusterServerInfo> glusterServers) {
        Map<String, String> serversAndPublicKeys = new HashMap<>();

        for (GlusterServerInfo server : glusterServers) {
            if (server.getStatus() == PeerStatus.CONNECTED && !serverExists(server)) {
                String publicKey = null;
                QueryReturnValue returnValue;
                if (getParameters().isServerPublicKeyRequired()) {
                    returnValue = runInternalQuery(QueryType.GetServerSSHPublicKey,
                            new ServerParameters(server.getHostnameOrIp()));
                    if (returnValue != null && returnValue.getSucceeded() && returnValue.getReturnValue() != null) {
                        publicKey = returnValue.getReturnValue().toString();
                    }
                }

                serversAndPublicKeys.put(server.getHostnameOrIp(), publicKey == null ? "" : publicKey);
            }
        }
        return serversAndPublicKeys;
    }

    public boolean serverExists(GlusterServerInfo glusterServer) {
        return glusterDBUtils.serverExists(glusterServer.getUuid());
    }
}
