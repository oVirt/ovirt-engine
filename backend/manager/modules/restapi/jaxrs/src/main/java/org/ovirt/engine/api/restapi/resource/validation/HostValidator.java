package org.ovirt.engine.api.restapi.resource.validation;

import static org.ovirt.engine.api.common.util.EnumValidator.validateEnum;

import org.ovirt.engine.api.model.Host;
import org.ovirt.engine.api.model.PmProxy;
import org.ovirt.engine.api.model.PmProxyType;
import org.ovirt.engine.api.model.HostProtocol;

@ValidatedClass(clazz = Host.class)
public class HostValidator implements Validator<Host> {
    private SSHValidator sshValidator = new SSHValidator();

    @Override
    public void validateEnums(Host host) {
        if (host.isSetPowerManagement()) {
            if (host.getPowerManagement().isSetPmProxies()) {
                for (PmProxy proxy : host.getPowerManagement().getPmProxies().getPmProxy()) {
                    validateEnum(PmProxyType.class, proxy.getType(), true);
                }
            }
        }
        if (host.isSetSsh()) {
            sshValidator.validateEnums(host.getSsh());
        }
        if (host.isSetProtocol()) {
            validateEnum(HostProtocol.class, host.getProtocol(), true);
        }
    }
}
