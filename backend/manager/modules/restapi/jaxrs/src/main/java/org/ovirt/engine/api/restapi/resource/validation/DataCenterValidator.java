package org.ovirt.engine.api.restapi.resource.validation;

import static org.ovirt.engine.api.common.util.EnumValidator.validateEnum;

import org.ovirt.engine.api.model.DataCenter;
import org.ovirt.engine.api.model.QuotaModeType;
import org.ovirt.engine.api.model.StorageFormat;

@ValidatedClass(clazz = DataCenter.class)
public class DataCenterValidator implements Validator<DataCenter> {

    @Override
    public void validateEnums(DataCenter dataCenter) {
        if (dataCenter != null) {
            if (dataCenter.isSetStorageFormat()) {
                validateEnum(StorageFormat.class, dataCenter.getStorageFormat(), true);
            }
            if (dataCenter.isSetQuotaMode()) {
                validateEnum(QuotaModeType.class, dataCenter.getQuotaMode(), true);
            }
        }
    }
}
