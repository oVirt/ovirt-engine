package org.ovirt.engine.api.restapi.resource.validation;

import org.ovirt.engine.api.common.util.EnumValidator;
import org.ovirt.engine.api.model.WatchDog;
import org.ovirt.engine.api.restapi.types.WatchdogAction;
import org.ovirt.engine.api.restapi.types.WatchdogModel;

@ValidatedClass(clazz = WatchDog.class)
public class WatchdogValidator implements Validator<WatchDog> {

    @Override
    public void validateEnums(WatchDog entity) {
        if (entity != null) {
            EnumValidator.validateEnum(WatchdogAction.class, entity.getAction(), true);
            EnumValidator.validateEnum(WatchdogModel.class, entity.getModel(), true);
        }
    }

}
