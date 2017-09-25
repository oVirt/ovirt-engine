package org.ovirt.engine.api.restapi.types;

import java.util.Map;

import org.ovirt.engine.api.model.SystemOption;
import org.ovirt.engine.api.model.SystemOptionValue;
import org.ovirt.engine.api.model.SystemOptionValues;

public class SystemOptionsMapper {
    public static SystemOption map(Map<String, Object> optionValues, String optionName) {
        SystemOption result = new SystemOption();
        SystemOptionValues values = new SystemOptionValues();
        result.setId(optionName);
        result.setName(optionName);
        result.setValues(values);

        for (Map.Entry<String, Object> optionValue : optionValues.entrySet()) {
            SystemOptionValue value = new SystemOptionValue();
            value.setVersion(optionValue.getKey());
            value.setValue(optionValue.getValue().toString());
            values.getSystemOptionValues().add(value);
        }

        return result;
    }
}
