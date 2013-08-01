package org.ovirt.engine.api.restapi.resource.validation;

import org.ovirt.engine.api.model.NfsVersion;
import org.ovirt.engine.api.model.StorageConnection;
import org.ovirt.engine.api.model.StorageType;

import static org.ovirt.engine.api.common.util.EnumValidator.validateEnum;


@ValidatedClass(clazz = StorageConnection.class)
public class StorageConnectionValidator implements Validator<StorageConnection> {
    @Override
    public void validateEnums(StorageConnection connection) {
        if (connection.isSetType()) {
                validateEnum(StorageType.class, connection.getType(), true);
        }

        if (connection.isSetNfsVersion()) {
            validateEnum(NfsVersion.class, connection.getNfsVersion(), true);
        }
    }
}
