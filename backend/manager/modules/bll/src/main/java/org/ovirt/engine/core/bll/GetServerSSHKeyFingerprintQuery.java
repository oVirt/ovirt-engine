package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.bll.context.EngineContext;
import org.ovirt.engine.core.bll.utils.EngineSSHClient;
import org.ovirt.engine.core.common.queries.ServerParameters;

/**
 * Query to fetch fingerprint of the given server name
 */
public class GetServerSSHKeyFingerprintQuery<P extends ServerParameters> extends QueriesCommandBase<P> {

    public GetServerSSHKeyFingerprintQuery(P parameters) {
        super(parameters);
    }

    public GetServerSSHKeyFingerprintQuery(P parameters, EngineContext engineContext) {
        super(parameters, engineContext);
    }

    public String getServerFingerprint(String serverName) {
        String fingerPrint = null;
        try (final EngineSSHClient client = getEngineSSHClient()) {
            client.setHost(serverName);
            client.setUser("dummy");
            client.connect();
            fingerPrint = client.getHostFingerprint();
        } catch (Throwable e) {
            log.error("Could not fetch fingerprint of host '{}': {}",
                serverName,
                e.getMessage()
            );
            log.debug("Exception", e);
        }
        return fingerPrint;
    }

    @Override
    protected void executeQueryCommand() {
        getQueryReturnValue().setReturnValue(getServerFingerprint(getParameters().getServer()));
    }

    protected EngineSSHClient getEngineSSHClient() {
        return new EngineSSHClient();
    }

}
