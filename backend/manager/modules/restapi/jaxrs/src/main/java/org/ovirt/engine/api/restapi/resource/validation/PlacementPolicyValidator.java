package org.ovirt.engine.api.restapi.resource.validation;

import static org.ovirt.engine.api.common.util.EnumValidator.validateEnum;

import org.ovirt.engine.api.model.VmAffinity;
import org.ovirt.engine.api.model.VmPlacementPolicy;

@ValidatedClass(clazz = VmPlacementPolicy.class)
public class PlacementPolicyValidator implements Validator<VmPlacementPolicy> {

    @Override
    public void validateEnums(VmPlacementPolicy placementPolicy) {
        if (placementPolicy != null) {
            if (placementPolicy.isSetAffinity()) {
                validateEnum(VmAffinity.class, placementPolicy.getAffinity(), true);
            }
        }
    }
}
