package org.ovirt.engine.api.restapi.resource.validation;

import static org.ovirt.engine.api.common.util.EnumValidator.validateEnum;

import org.ovirt.engine.api.model.OperatingSystem;
import org.ovirt.engine.api.model.OsType;
import org.ovirt.engine.api.model.OsTypeUtils;

@ValidatedClass(clazz = OperatingSystem.class)
public class OsValidator implements Validator<OperatingSystem> {

    private BootValidator bootValidator = new BootValidator();

    @Override
    public void validateEnums(OperatingSystem os) {
        if (os != null) {
            if (os.isSetType()) {
                validateEnum(OsType.class, OsTypeUtils.getAllValues(),  os.getType(), true);
            }
            if (os.isSetBoot()) {
                bootValidator.validateEnums(os.getBoot());
            }
        }
    }
}
