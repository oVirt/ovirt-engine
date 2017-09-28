package org.ovirt.engine.api.restapi.types;

import org.ovirt.engine.api.model.Architecture;
import org.ovirt.engine.api.model.Cpu;
import org.ovirt.engine.core.common.businessentities.ArchitectureType;
import org.ovirt.engine.core.common.businessentities.ServerCpu;

public class CPUMapper {

    @Mapping(from = ServerCpu.class, to = Cpu.class)
    public static Cpu map(ServerCpu entity,
            Cpu template) {
        Cpu model = template != null ? template : new Cpu();

        model.setName(entity.getCpuName());
        model.setLevel(entity.getLevel());
        model.setArchitecture(map(entity.getArchitecture(), null));

        return model;
    }

    @Mapping(from = Architecture.class, to = ArchitectureType.class)
    public static ArchitectureType map(Architecture model,
            ArchitectureType template) {
        if (model != null) {
            switch (model) {
            case UNDEFINED:
                return ArchitectureType.undefined;
            case X86_64:
                return ArchitectureType.x86_64;
            case PPC64:
                return ArchitectureType.ppc64;
            case S390X:
                return ArchitectureType.s390x;
            default:
                return null;
            }
        }
        return null;
    }

    @Mapping(from = ArchitectureType.class, to = Architecture.class)
    public static Architecture map(ArchitectureType model,
            String template) {
        if (model != null) {
            switch (model) {
            case undefined:
                return Architecture.UNDEFINED;
            case x86_64:
                return Architecture.X86_64;
            case ppc64:
                return Architecture.PPC64;
            case s390x:
                return Architecture.S390X;
            default:
                return null;
            }
        }
        return null;
    }

}
