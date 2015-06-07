package org.ovirt.engine.api.restapi.resource.validation;

import static org.ovirt.engine.api.common.util.EnumValidator.validateEnum;

import org.ovirt.engine.api.model.Event;
import org.ovirt.engine.api.model.LogSeverity;

@ValidatedClass(clazz = Event.class)
public class EventValidator implements Validator<Event> {

    private HostValidator hostValidator = new HostValidator();
    private StorageDomainValidator storageDomainValidator = new StorageDomainValidator();

    @Override
    public void validateEnums(Event event) {
        if (event.isSetSeverity()) {
            validateEnum(LogSeverity.class, event.getSeverity(), true);
        }
        if (event.isSetHost()) {
            hostValidator.validateEnums(event.getHost());
        }
        if (event.isSetStorageDomain()) {
            storageDomainValidator.validateEnums(event.getStorageDomain());
        }
    }
}
