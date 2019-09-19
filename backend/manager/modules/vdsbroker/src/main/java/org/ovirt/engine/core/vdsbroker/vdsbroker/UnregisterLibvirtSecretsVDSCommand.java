package org.ovirt.engine.core.vdsbroker.vdsbroker;

import java.util.ArrayList;
import java.util.List;

import org.ovirt.engine.core.common.vdscommands.UnregisterLibvirtSecretsVDSParameters;
import org.ovirt.engine.core.compat.Guid;

public class UnregisterLibvirtSecretsVDSCommand<P extends UnregisterLibvirtSecretsVDSParameters> extends VdsBrokerCommand<P> {

    public UnregisterLibvirtSecretsVDSCommand(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeVdsBrokerCommand() {
        status = getBroker().unregisterSecrets(buildStructFromLibvirtSecretsList());
        proceedProxyReturnValue();
    }

    protected String[] buildStructFromLibvirtSecretsList() {
        List<String> libvirtSecretsUuids = new ArrayList<>();
        for (Guid libvirtSecretUuid : getParameters().getLibvirtSecretsUuids()) {
            libvirtSecretsUuids.add(libvirtSecretUuid.toString());
        }
        return libvirtSecretsUuids.toArray(new String[libvirtSecretsUuids.size()]);
    }
}
