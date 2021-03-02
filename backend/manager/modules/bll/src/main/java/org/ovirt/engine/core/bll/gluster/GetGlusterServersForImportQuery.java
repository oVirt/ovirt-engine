package org.ovirt.engine.core.bll.gluster;

import java.util.Map;
import java.util.Set;

import javax.inject.Inject;
import javax.naming.AuthenticationException;

import org.ovirt.engine.core.bll.context.EngineContext;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.queries.gluster.GlusterServersQueryParameters;
import org.ovirt.engine.core.dao.VdsStaticDao;

/**
 * Query to fetch list of gluster servers via ssh using the given serverName and password.
 *
 * This query will be invoked from Import Gluster Cluster dialog. In the dialog the user will provide the servername,
 * password and ssh public key of any one of the server in the cluster. This Query will validate if the given server is
 * already part of the cluster by checking with the database. If exists the query will return the error message.
 *
 * Since, the importing cluster haven't been bootstrapped yet, we are running the gluster peer status command via ssh.
 *
 */
public class GetGlusterServersForImportQuery<P extends GlusterServersQueryParameters> extends GlusterQueriesCommandBase<P> {

    // Currently we use only root user to authenticate with host
    private static final String USER = "root";

    @Inject
    private VdsStaticDao vdsStaticDao;

    public GetGlusterServersForImportQuery(P parameters, EngineContext engineContext) {
        super(parameters, engineContext);
    }

    @Override
    protected void executeQueryCommand() {
        // Check whether the given server is already part of the cluster
        if (vdsStaticDao.getByHostName(getParameters().getServerName()) != null
                || vdsStaticDao.getAllWithIpAddress(getParameters().getServerName()).size() > 0) {
            throw new RuntimeException(EngineMessage.SERVER_ALREADY_EXISTS_IN_ANOTHER_CLUSTER.toString());
        }

        try {
            Map<String, String> serverPublicKeyMap =
                    glusterUtil.getPeersWithSshPublicKeys(getParameters().getServerName(),
                            USER,
                            getParameters().getPassword()
                    );

            // Keep server details in the map only for the servers which are reachable
            serverPublicKeyMap.entrySet().removeIf(entry -> entry.getValue() == null);

            // Check if any of the server in the map is already part of some other cluster.
            if (!validateServers(serverPublicKeyMap.keySet())) {
                throw new RuntimeException(EngineMessage.SERVER_ALREADY_EXISTS_IN_ANOTHER_CLUSTER.toString());
            }

            // Add the given server with it's public key
            serverPublicKeyMap.put(getParameters().getServerName(), getParameters().getSshPublicKey());

            getQueryReturnValue().setReturnValue(serverPublicKeyMap);
        } catch (AuthenticationException ae) {
            throw new RuntimeException(EngineMessage.SSH_AUTHENTICATION_FAILED.toString());
        } catch (RuntimeException re) {
            throw re;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /*
     * The method will return false, if the given server is already part of the existing cluster, otherwise true.
     */
    private boolean validateServers(Set<String> serverNames) {
        for (String serverName : serverNames) {
            if (vdsStaticDao.getByHostName(serverName) != null
                    || vdsStaticDao.getAllWithIpAddress(serverName).size() > 0) {
                return false;
            }
        }
        return true;
    }
}
