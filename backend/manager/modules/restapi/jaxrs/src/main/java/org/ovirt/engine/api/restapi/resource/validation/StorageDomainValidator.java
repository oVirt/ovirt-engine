package org.ovirt.engine.api.restapi.resource.validation;

import org.ovirt.engine.api.model.EntityExternalStatus;
import org.ovirt.engine.api.model.NfsVersion;
import org.ovirt.engine.api.model.StorageDomain;
import org.ovirt.engine.api.model.StorageDomainType;
import org.ovirt.engine.api.model.StorageFormat;
import org.ovirt.engine.api.model.StorageType;

import static org.ovirt.engine.api.common.util.EnumValidator.validateEnum;

@ValidatedClass(clazz = StorageDomain.class)
public class StorageDomainValidator implements Validator<StorageDomain> {

    @Override
    public void validateEnums(StorageDomain storageDomain) {
        if (storageDomain != null) {
            if (storageDomain.isSetType()) {
                validateEnum(StorageDomainType.class, storageDomain.getType(), true);
            }
            if (storageDomain.isSetStorage() && storageDomain.getStorage().isSetType()) {
                validateEnum(StorageType.class, storageDomain.getStorage().getType(), true);
            }
            if (storageDomain.isSetFormat()) {
                validateEnum(StorageFormat.class, storageDomain.getStorageFormat(), true);
            }
            if (storageDomain.isSetStorage() && storageDomain.getStorage().isSetNfsVersion()) {
                validateEnum(NfsVersion.class, storageDomain.getStorage().getNfsVersion(), true);
            }
            if (storageDomain.isSetExternalStatus() && storageDomain.getExternalStatus().getState() != null) {
                validateEnum(EntityExternalStatus.class, storageDomain.getExternalStatus().getState().toUpperCase());
            }
        }
    }
}
