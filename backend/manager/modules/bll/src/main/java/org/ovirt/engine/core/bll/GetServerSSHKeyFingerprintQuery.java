package org.ovirt.engine.core.bll;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.ovirt.engine.core.common.queries.ServerParameters;
import org.ovirt.engine.core.utils.hostinstall.VdsInstallerSSH;

/**
 * Query to fetch fingerprint of the given server name
 */
public class GetServerSSHKeyFingerprintQuery<P extends ServerParameters> extends QueriesCommandBase<P> {
    public VdsInstallerSSH wrapper;

    public GetServerSSHKeyFingerprintQuery(P parameters) {
        super(parameters);
    }

    public String getServerFingerprint(String serverName) {
        wrapper = getVdsInstallerSSHInstance();
        String fingerPrint = null;
        try {
            fingerPrint = wrapper.getServerKeyFingerprint(getParameters().getServer());
        } catch (Throwable e) {
            log.errorFormat("Could not fetch fingerprint of host {0} with message: {1}",
                    getParameters().getServer(),
                    ExceptionUtils.getMessage(e));
        } finally {
            wrapper.shutdown();
        }
        return fingerPrint;
    }

    public VdsInstallerSSH getVdsInstallerSSHInstance() {
        return new VdsInstallerSSH();
    }

    @Override
    protected void executeQueryCommand() {
        getQueryReturnValue().setReturnValue(getServerFingerprint(getParameters().getServer()));
    }
}
