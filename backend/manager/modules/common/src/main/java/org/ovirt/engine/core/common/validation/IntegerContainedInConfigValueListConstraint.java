package org.ovirt.engine.core.common.validation;

import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.validation.annotation.IntegerContainedInConfigValueList;

public class IntegerContainedInConfigValueListConstraint
    extends ContainedInConfigValueListConstraint<IntegerContainedInConfigValueList, Integer> {

    @Override
    public ConfigValues getConfigValue(IntegerContainedInConfigValueList constraintAnnotation) {
        return constraintAnnotation.configValue();
    }
}
