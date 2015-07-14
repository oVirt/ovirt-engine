package org.ovirt.engine.api.restapi.resource.validation;

import static org.ovirt.engine.api.common.util.EnumValidator.validateEnum;

import org.ovirt.engine.api.model.DataCenter;
import org.ovirt.engine.api.model.QuotaModeType;
import org.ovirt.engine.api.model.StorageFormat;
import org.ovirt.engine.api.model.StorageType;

@ValidatedClass(clazz = DataCenter.class)
public class DataCenterValidator implements Validator<DataCenter> {

    @Override
    public void validateEnums(DataCenter dataCenter) {
        if (dataCenter != null) {
            if (dataCenter.isSetStorageType()) {
                validateEnum(StorageType.class, dataCenter.getStorageType(), true);
            }
            if (dataCenter.isSetStorageFormat()) {
                validateEnum(StorageFormat.class, dataCenter.getStorageFormat(), true);
            }
            if (dataCenter.isSetQuotaMode()) {
                validateEnum(QuotaModeType.class, dataCenter.getQuotaMode(), true);
            }
        }
    }
}
