package org.ovirt.engine.core.utils.archstrategy;

import org.ovirt.engine.core.common.businessentities.ArchitectureType;

public class X86_64Strategy implements ArchStrategy {

    @Override
    public ArchitectureType getArchitecture() {
        return ArchitectureType.x86_64;
    }

    @Override
    public <T extends ArchCommand> T run(T c) {
        c.runForX86_64();
        return c;
    }

}
