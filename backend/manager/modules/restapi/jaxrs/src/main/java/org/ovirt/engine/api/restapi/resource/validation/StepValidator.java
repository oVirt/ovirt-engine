package org.ovirt.engine.api.restapi.resource.validation;

import org.ovirt.engine.api.model.Step;
import org.ovirt.engine.api.model.StepEnum;

import static org.ovirt.engine.api.common.util.EnumValidator.validateEnum;

@ValidatedClass(clazz = Step.class)
public class StepValidator implements Validator<Step> {

    @Override
    public void validateEnums(Step step) {
        if (step.isSetType()) {
            validateEnum(StepEnum.class, step.getType(), true);
        }
    }
}
