package org.ovirt.engine.api.restapi.resource.validation;

import static org.ovirt.engine.api.common.util.EnumValidator.validateEnum;

import org.ovirt.engine.api.model.Network;
import org.ovirt.engine.api.restapi.types.NetworkUsage;

@ValidatedClass(clazz = Network.class)
public class NetworkValidator implements Validator<Network> {

    @Override
    public void validateEnums(Network network) {
        if (network != null) {
            if (network.isSetUsages()) {
                for (String usage : network.getUsages().getUsages()) {
                    validateEnum(NetworkUsage.class, usage, true);
                }
            }
        }
    }
}
