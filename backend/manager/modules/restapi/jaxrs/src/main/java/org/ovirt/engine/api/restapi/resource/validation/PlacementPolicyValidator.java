package org.ovirt.engine.api.restapi.resource.validation;

import org.ovirt.engine.api.model.VmAffinity;
import org.ovirt.engine.api.model.VmPlacementPolicy;
import static org.ovirt.engine.api.common.util.EnumValidator.validateEnum;

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
