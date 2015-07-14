package org.ovirt.engine.api.restapi.resource.validation;

import static org.ovirt.engine.api.common.util.EnumValidator.validateEnum;

import org.ovirt.engine.api.model.NIC;
import org.ovirt.engine.api.model.NicInterface;

@ValidatedClass(clazz = NIC.class)
public class NicValidator implements Validator<NIC> {
    private NetworkValidator networkValidator = new NetworkValidator();

    @Override
    public void validateEnums(NIC nic) {
        if (nic != null) {
            if (nic.isSetInterface()) {
                validateEnum(NicInterface.class, nic.getInterface(), true);
            }
            if (nic.isSetNetwork()) {
                networkValidator.validateEnums(nic.getNetwork());
            }
        }
    }
}
