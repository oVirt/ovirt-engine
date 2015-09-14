package org.ovirt.engine.api.restapi.resource.validation;

import static org.ovirt.engine.api.common.util.EnumValidator.validateEnum;

import org.ovirt.engine.api.model.Nic;
import org.ovirt.engine.api.model.NicInterface;

@ValidatedClass(clazz = Nic.class)
public class NicValidator implements Validator<Nic> {
    private NetworkValidator networkValidator = new NetworkValidator();

    @Override
    public void validateEnums(Nic nic) {
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
