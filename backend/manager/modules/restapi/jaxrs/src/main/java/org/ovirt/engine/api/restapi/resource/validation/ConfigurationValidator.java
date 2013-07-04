package org.ovirt.engine.api.restapi.resource.validation;

import org.ovirt.engine.api.model.Configuration;
import org.ovirt.engine.api.model.ConfigurationType;

import static org.ovirt.engine.api.common.util.EnumValidator.validateEnum;

@ValidatedClass(clazz = Configuration.class)
public class ConfigurationValidator implements Validator<Configuration>{
    @Override
    public void validateEnums(Configuration configuration) {
        if (configuration != null && configuration.isSetType()) {
            validateEnum(ConfigurationType.class, configuration.getType(), true);
        }
    }
}
