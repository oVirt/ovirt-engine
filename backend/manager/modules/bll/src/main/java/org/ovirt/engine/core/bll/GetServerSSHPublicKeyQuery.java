package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.bll.context.EngineContext;
import org.ovirt.engine.core.bll.utils.EngineSSHClient;
import org.ovirt.engine.core.common.queries.ServerParameters;

public class GetServerSSHPublicKeyQuery <P extends ServerParameters> extends QueriesCommandBase<P> {
    public GetServerSSHPublicKeyQuery(P parameters, EngineContext engineContext) {
        super(parameters, engineContext);
    }

    public String getServerPublicKey(String serverName, Integer port) {
        String publicKeyPem = null;
        try (final EngineSSHClient client = getEngineSSHClient()) {
            client.setHost(serverName, port);
            client.setUser("dummy");
            client.connect();
            publicKeyPem = client.getHostPublicKey();
        } catch (Exception e) {
            log.error("Could not fetch fingerprint of host '{}:{}': {}",
                    serverName,
                    port,
                    e.getMessage()
            );
            log.debug("Exception", e);
        }
        return publicKeyPem;
    }

    @Override
    protected void executeQueryCommand() {
        getQueryReturnValue().setReturnValue(
                getServerPublicKey(getParameters().getServer(), getParameters().getPort()));
    }

    //visible for unit test
    protected EngineSSHClient getEngineSSHClient() {
        return new EngineSSHClient();
    }

}
