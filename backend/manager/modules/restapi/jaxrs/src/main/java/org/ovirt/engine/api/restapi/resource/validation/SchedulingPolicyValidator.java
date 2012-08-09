package org.ovirt.engine.api.restapi.resource.validation;

import org.ovirt.engine.api.model.SchedulingPolicy;
import org.ovirt.engine.api.model.SchedulingPolicyType;

import static org.ovirt.engine.api.common.util.EnumValidator.validateEnum;

@ValidatedClass(clazz = SchedulingPolicy.class)
public class SchedulingPolicyValidator implements Validator<SchedulingPolicy> {

    @Override
    public void validateEnums(SchedulingPolicy schedulingPolicy) {
        if (schedulingPolicy != null) {
            if (schedulingPolicy.isSetPolicy()) {
                validateEnum(SchedulingPolicyType.class, schedulingPolicy.getPolicy(), true);
            }
        }
    }
}
