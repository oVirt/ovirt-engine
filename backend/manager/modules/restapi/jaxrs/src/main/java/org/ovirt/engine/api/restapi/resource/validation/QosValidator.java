package org.ovirt.engine.api.restapi.resource.validation;

import static org.ovirt.engine.api.common.util.EnumValidator.validateEnum;

import org.ovirt.engine.api.model.Qos;
import org.ovirt.engine.core.common.businessentities.qos.QosType;

@ValidatedClass(clazz = Qos.class)
public class QosValidator implements Validator<Qos> {

    @Override
    public void validateEnums(Qos qos) {
        if (qos.isSetType()) {
            validateEnum(QosType.class, qos.getType(), true);
        }
    }
}
