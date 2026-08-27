package org.ovirt.engine.core.utils.archstrategy;

import org.ovirt.engine.core.common.businessentities.ArchitectureType;

public class AARCH64Strategy implements ArchStrategy {

    @Override
    public ArchitectureType getArchitecture() {
        return ArchitectureType.aarch64;
    }

    @Override
    public <T extends ArchCommand> T run(T c) {
        c.runForAARCH64();
        return c;
    }

}
