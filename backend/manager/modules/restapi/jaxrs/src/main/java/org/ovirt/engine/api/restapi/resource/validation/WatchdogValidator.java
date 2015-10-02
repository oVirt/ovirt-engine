package org.ovirt.engine.api.restapi.resource.validation;

import org.ovirt.engine.api.common.util.EnumValidator;
import org.ovirt.engine.api.model.Watchdog;
import org.ovirt.engine.api.model.WatchdogAction;
import org.ovirt.engine.api.model.WatchdogModel;

@ValidatedClass(clazz = Watchdog.class)
public class WatchdogValidator implements Validator<Watchdog> {

    @Override
    public void validateEnums(Watchdog entity) {
        if (entity != null) {
            EnumValidator.validateEnum(WatchdogAction.class, entity.getAction(), true);
            EnumValidator.validateEnum(WatchdogModel.class, entity.getModel(), true);
        }
    }

}
