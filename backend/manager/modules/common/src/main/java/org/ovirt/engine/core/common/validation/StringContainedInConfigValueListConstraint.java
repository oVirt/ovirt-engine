package org.ovirt.engine.core.common.validation;

import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.validation.annotation.StringContainedInConfigValueList;


public class StringContainedInConfigValueListConstraint
    extends ContainedInConfigValueListConstraint<StringContainedInConfigValueList, String> {

    @Override
    public ConfigValues getConfigValue(StringContainedInConfigValueList constraintAnnotation) {
        return constraintAnnotation.configValue();
    }
}
