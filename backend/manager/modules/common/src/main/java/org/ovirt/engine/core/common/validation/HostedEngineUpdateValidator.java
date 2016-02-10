package org.ovirt.engine.core.common.validation;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import org.ovirt.engine.core.common.action.VmManagementParametersBase;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.validation.annotation.HostedEngineUpdate;

public class HostedEngineUpdateValidator implements ConstraintValidator<HostedEngineUpdate, VmManagementParametersBase> {
    @Override
    public void initialize(HostedEngineUpdate constraintAnnotation) {
    }

    @Override
    public boolean isValid(VmManagementParametersBase value, ConstraintValidatorContext context) {
        return !value.getVm().isHostedEngine()
                || Config.<Boolean> getValue(ConfigValues.AllowEditingHostedEngine);
    }
}
