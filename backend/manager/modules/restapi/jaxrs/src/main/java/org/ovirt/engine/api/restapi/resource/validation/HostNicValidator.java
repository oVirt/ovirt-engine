package org.ovirt.engine.api.restapi.resource.validation;

import static org.ovirt.engine.api.common.util.EnumValidator.validateEnum;

import org.ovirt.engine.api.model.BootProtocol;
import org.ovirt.engine.api.model.HostNic;

@ValidatedClass(clazz = HostNic.class)
public class HostNicValidator implements Validator<HostNic> {

    @Override
    public void validateEnums(HostNic hostNic) {
        if (hostNic.isSetBootProtocol()) {
            validateEnum(BootProtocol.class, hostNic.getBootProtocol(), true);
        }
    }
}
