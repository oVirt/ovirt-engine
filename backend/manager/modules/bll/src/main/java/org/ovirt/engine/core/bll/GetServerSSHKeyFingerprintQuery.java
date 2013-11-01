package org.ovirt.engine.core.bll;

import org.apache.commons.lang.exception.ExceptionUtils;

import org.ovirt.engine.core.bll.utils.EngineSSHClient;
import org.ovirt.engine.core.common.queries.ServerParameters;

/**
 * Query to fetch fingerprint of the given server name
 */
public class GetServerSSHKeyFingerprintQuery<P extends ServerParameters> extends QueriesCommandBase<P> {

    protected EngineSSHClient getEngineSSHClient() {
        return new EngineSSHClient();
    }

    public GetServerSSHKeyFingerprintQuery(P parameters) {
        super(parameters);
    }

    public String getServerFingerprint(String serverName) {
        String fingerPrint = null;
        try (final EngineSSHClient client = getEngineSSHClient()) {
            client.setHost(serverName);
            client.connect();
            fingerPrint = client.getHostFingerprint();
        } catch (Throwable e) {
            log.errorFormat("Could not fetch fingerprint of host {0} with message: {1}",
                serverName,
                ExceptionUtils.getMessage(e)
            );
        }
        return fingerPrint;
    }

    @Override
    protected void executeQueryCommand() {
        getQueryReturnValue().setReturnValue(getServerFingerprint(getParameters().getServer()));
    }
}
