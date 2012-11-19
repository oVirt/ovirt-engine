package org.ovirt.engine.core.bll.gluster;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.ovirt.engine.core.bll.Backend;
import org.ovirt.engine.core.bll.QueriesCommandBase;
import org.ovirt.engine.core.bll.utils.ClusterUtils;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterServerInfo;
import org.ovirt.engine.core.common.businessentities.gluster.PeerStatus;
import org.ovirt.engine.core.common.interfaces.VDSBrokerFrontend;
import org.ovirt.engine.core.common.queries.gluster.AddedGlusterServersParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.common.vdscommands.VdsIdVDSCommandParametersBase;
import org.ovirt.engine.core.utils.hostinstall.VdsInstallerSSH;

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
            VDSReturnValue returnValue = getBackendInstance().RunVdsCommand(VDSCommandType.GlusterServersList,
                            new VdsIdVDSCommandParametersBase(upServer.getId()));
            glusterServers = getAddedGlusterServers((List<GlusterServerInfo>) returnValue.getReturnValue());
        }
        getQueryReturnValue().setReturnValue(glusterServers);
    }

    private Map<String, String> getAddedGlusterServers(List<GlusterServerInfo> glusterServers) {
        Map<String, String> serversAndFingerprint = new HashMap<String, String>();
        List<VDS> serversList = getClusterUtils().getVdsDao().getAllForVdsGroup(getParameters().getClusterId());

        for (GlusterServerInfo server : glusterServers) {
            if (server.getStatus() == PeerStatus.CONNECTED && (!serverExists(serversList, server))) {
                serversAndFingerprint.put(server.getHostnameOrIp(), getServerFingerprint(server.getHostnameOrIp()));
            }
        }
        return serversAndFingerprint;
    }

    private boolean serverExists(List<VDS> serversList, GlusterServerInfo glusterServer) {
        for (VDS server : serversList) {
            String serverHostnameOrIp =
                    server.gethost_name().isEmpty() ? server.getManagmentIp() : server.gethost_name();
            if (serverHostnameOrIp.equalsIgnoreCase(glusterServer.getHostnameOrIp())) {
                return true;
            }
        }
        return false;
    }

    private String getServerFingerprint(String hostnameOrIp) {
        String serverKeyFingerprint = "";
        if (getParameters().isServerKeyFingerprintRequired()) {
            VdsInstallerSSH sshWrapper = getVdsInstallerSSHInstance();
            try {
                serverKeyFingerprint = sshWrapper.getServerKeyFingerprint(hostnameOrIp);
            } catch (Throwable e) {
                log.errorFormat("Could not fetch fingerprint of host {0} with message: {1}",
                        hostnameOrIp,
                        ExceptionUtils.getMessage(e));
            } finally {
                sshWrapper.shutdown();
            }
        }
        return serverKeyFingerprint;
    }

    public VdsInstallerSSH getVdsInstallerSSHInstance() {
        return new VdsInstallerSSH();
    }

    public ClusterUtils getClusterUtils() {
        return ClusterUtils.getInstance();
    }

    public VDSBrokerFrontend getBackendInstance() {
        return Backend.getInstance()
                .getResourceManager();
    }

}
