package org.ovirt.engine.api.restapi.resource.validation;

import static org.ovirt.engine.api.common.util.EnumValidator.validateEnum;

import org.ovirt.engine.api.model.VnicPassThroughMode;
import org.ovirt.engine.api.model.VnicProfile;

@ValidatedClass(clazz = VnicProfile.class)
public class VnicProfileValidator implements Validator<VnicProfile> {

    @Override
    public void validateEnums(VnicProfile resource) {
        if (resource.isSetPassThrough() && resource.getPassThrough().isSetMode()) {
            validateEnum(VnicPassThroughMode.class, resource.getPassThrough().getMode(), true);
        }
    }
}
