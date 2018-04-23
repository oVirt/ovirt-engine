package org.ovirt.engine.core.vdsbroker.vdsbroker;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.common.businessentities.storage.LibvirtSecret;
import org.ovirt.engine.core.common.vdscommands.RegisterLibvirtSecretsVDSParameters;

public class RegisterLibvirtSecretsVDSCommand<P extends RegisterLibvirtSecretsVDSParameters> extends VdsBrokerCommand<P> {

    public RegisterLibvirtSecretsVDSCommand(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeVdsBrokerCommand() {
        status = getBroker().registerSecrets(
                buildStructFromLibvirtSecretsList(), getParameters().isClearUnusedSecrets());
        proceedProxyReturnValue();
    }

    @SuppressWarnings({"unchecked", "SuspiciousToArrayCall"})
    protected Map<String, String>[] buildStructFromLibvirtSecretsList() {
        final List<Map<String, String>> structs = new ArrayList<>();
        for (LibvirtSecret libvirtSecret : getParameters().getLibvirtSecrets()) {
            structs.add(createStructFromLibvirtSecret(libvirtSecret));
        }
        return structs.toArray(new HashMap[structs.size()]);
    }

    public static Map<String, String> createStructFromLibvirtSecret(LibvirtSecret libvirtSecret) {
        Map<String, String> con = new HashMap<>();
        con.put("uuid", libvirtSecret.getId().toString());
        con.put("password", libvirtSecret.getValue());
        con.put("description", libvirtSecret.getDescription() != null ? libvirtSecret.getDescription() : StringUtils.EMPTY);
        con.put("usageType", libvirtSecret.getUsageType().name().toLowerCase());
        con.put("usageID", String.format("%s/%s/%s", VdsProperties.Ovirt, libvirtSecret.getProviderId(), libvirtSecret.getId()));
        return con;
    }
}
