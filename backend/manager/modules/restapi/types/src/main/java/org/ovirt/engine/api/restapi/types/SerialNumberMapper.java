package org.ovirt.engine.api.restapi.types;

import org.ovirt.engine.api.model.SerialNumber;
import org.ovirt.engine.api.model.SerialNumberPolicy;
import org.ovirt.engine.core.common.businessentities.HasSerialNumberPolicy;

public class SerialNumberMapper {
    @Mapping(from = HasSerialNumberPolicy.class, to = SerialNumber.class)
    public static SerialNumber map(HasSerialNumberPolicy entity, SerialNumber template) {
        if (template == null) {
            template = new SerialNumber();
        }
        template.setPolicy(map(entity.getSerialNumberPolicy(), null));
        template.setValue(entity.getCustomSerialNumber());

        return template;
    }

    public static void copySerialNumber(SerialNumber serialNumber, HasSerialNumberPolicy entity) {
        if (serialNumber.isSetPolicy()) {
            entity.setSerialNumberPolicy(map(serialNumber.getPolicy(), null));
        }
        if (serialNumber.isSetValue()) {
            String customSerialNumber = "".equals(serialNumber.getValue()) ? null : serialNumber.getValue();
            entity.setCustomSerialNumber(customSerialNumber);
        }
    }

    @Mapping(from = SerialNumberPolicy.class, to = org.ovirt.engine.core.common.businessentities.SerialNumberPolicy.class)
    public static org.ovirt.engine.core.common.businessentities.SerialNumberPolicy map(SerialNumberPolicy serialNumberPolicy, org.ovirt.engine.core.common.businessentities.SerialNumberPolicy template) {
        if (serialNumberPolicy == null) {
            return null;
        }
        switch (serialNumberPolicy) {
        case HOST:
            return org.ovirt.engine.core.common.businessentities.SerialNumberPolicy.HOST_ID;
        case VM:
            return org.ovirt.engine.core.common.businessentities.SerialNumberPolicy.VM_ID;
        case CUSTOM:
            return org.ovirt.engine.core.common.businessentities.SerialNumberPolicy.CUSTOM;
        }
        return null;
    }

    @Mapping(from = org.ovirt.engine.core.common.businessentities.SerialNumberPolicy.class, to = SerialNumberPolicy.class)
    public static SerialNumberPolicy map(org.ovirt.engine.core.common.businessentities.SerialNumberPolicy serialNumberPolicy, SerialNumberPolicy template) {
        if (serialNumberPolicy == null) {
            return null;
        }
        switch (serialNumberPolicy) {
        case HOST_ID:
            return  SerialNumberPolicy.HOST;
        case VM_ID:
            return SerialNumberPolicy.VM;
        case CUSTOM:
            return SerialNumberPolicy.CUSTOM;
        }
        return null;
    }
}
