package org.ovirt.engine.core.utils.archstrategy;

import org.ovirt.engine.core.common.businessentities.ArchitectureType;

public class PPC64Strategy implements ArchStrategy {

    @Override
    public ArchitectureType getArchitecture() {
        return ArchitectureType.ppc64;
    }

    @Override
    public <T extends ArchCommand> T run(T c) {
        c.runForPPC64();
        return c;
    }

}
