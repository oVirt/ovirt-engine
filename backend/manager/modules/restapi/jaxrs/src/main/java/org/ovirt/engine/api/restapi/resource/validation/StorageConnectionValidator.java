package org.ovirt.engine.api.restapi.resource.validation;

import org.ovirt.engine.api.model.NfsVersion;
import org.ovirt.engine.api.model.Storage;
import org.ovirt.engine.api.model.StorageType;

import static org.ovirt.engine.api.common.util.EnumValidator.validateEnum;


@ValidatedClass(clazz = Storage.class)
public class StorageConnectionValidator implements Validator<Storage> {
    @Override
    public void validateEnums(Storage connection) {
        if (connection.isSetType()) {
                validateEnum(StorageType.class, connection.getType(), true);
        }

        if (connection.isSetNfsVersion()) {
            validateEnum(NfsVersion.class, connection.getNfsVersion(), true);
        }
    }
}
