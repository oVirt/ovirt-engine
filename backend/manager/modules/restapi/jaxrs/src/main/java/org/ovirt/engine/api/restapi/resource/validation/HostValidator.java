package org.ovirt.engine.api.restapi.resource.validation;

import static org.ovirt.engine.api.common.util.EnumValidator.validateEnum;

import org.ovirt.engine.api.model.Host;
import org.ovirt.engine.api.model.PmProxy;
import org.ovirt.engine.api.model.PmProxyType;

@ValidatedClass(clazz = Host.class)
public class HostValidator implements Validator<Host> {

    @Override
    public void validateEnums(Host host) {
        if (host.isSetPowerManagement()) {
            if (host.getPowerManagement().isSetPmProxies()) {
                for (PmProxy proxy : host.getPowerManagement().getPmProxies().getPmProxy()) {
                    validateEnum(PmProxyType.class, proxy.getType(), true);
                }
            }
        }
    }
}
