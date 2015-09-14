package org.ovirt.engine.api.restapi.resource.validation;

import static org.ovirt.engine.api.common.util.EnumValidator.validateEnum;

import org.ovirt.engine.api.model.Architecture;
import org.ovirt.engine.api.model.Cpu;

@ValidatedClass(clazz = Cpu.class)
public class CPUValidator implements Validator<Cpu> {

    @Override
    public void validateEnums(Cpu cpu) {
        if (cpu != null) {
            if (cpu.isSetArchitecture()) {
                validateEnum(Architecture.class, cpu.getArchitecture(), true);
            }
        }
    }
}
