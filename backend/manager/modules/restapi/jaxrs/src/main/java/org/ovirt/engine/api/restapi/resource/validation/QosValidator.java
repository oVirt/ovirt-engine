package org.ovirt.engine.api.restapi.resource.validation;

import static org.ovirt.engine.api.common.util.EnumValidator.validateEnum;

import org.ovirt.engine.api.model.QoS;
import org.ovirt.engine.core.common.businessentities.qos.QosType;

@ValidatedClass(clazz = QoS.class)
public class QosValidator implements Validator<QoS> {

    @Override
    public void validateEnums(QoS qos) {
        validateEnum(QosType.class, qos.getType(), true);
    }
}
