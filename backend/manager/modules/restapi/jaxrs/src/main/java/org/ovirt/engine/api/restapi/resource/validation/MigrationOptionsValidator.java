package org.ovirt.engine.api.restapi.resource.validation;

import org.ovirt.engine.api.model.InheritableBoolean;
import org.ovirt.engine.api.model.MigrationOptions;

import static org.ovirt.engine.api.common.util.EnumValidator.validateEnum;

@ValidatedClass(clazz = MigrationOptions.class)
public class MigrationOptionsValidator implements Validator<MigrationOptions> {
    @Override
    public void validateEnums(MigrationOptions options) {
        if (options != null) {
            if (options.isSetAutoConverge()) {
                validateEnum(InheritableBoolean.class, options.getAutoConverge(), true);
            }
            if (options.isSetCompressed()) {
                validateEnum(InheritableBoolean.class, options.getCompressed(), true);
            }
        }
    }
}
