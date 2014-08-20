package org.ovirt.engine.api.restapi.resource.validation;

import static org.ovirt.engine.api.common.util.EnumValidator.validateEnum;

import org.ovirt.engine.api.model.Architecture;
import org.ovirt.engine.api.model.CPU;

@ValidatedClass(clazz = CPU.class)
public class CPUValidator implements Validator<CPU> {

    @Override
    public void validateEnums(CPU cpu) {
        if (cpu != null) {
            if (cpu.isSetArchitecture()) {
                validateEnum(Architecture.class, cpu.getArchitecture(), true);
            }
        }
    }
}
