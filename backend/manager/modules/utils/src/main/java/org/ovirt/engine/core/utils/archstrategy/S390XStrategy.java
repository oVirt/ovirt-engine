package org.ovirt.engine.core.utils.archstrategy;

import org.ovirt.engine.core.common.businessentities.ArchitectureType;

public class S390XStrategy implements ArchStrategy {

    @Override
    public ArchitectureType getArchitecture() {
        return ArchitectureType.s390x;
    }

    @Override
    public <T extends ArchCommand> T run(T c) {
        c.runForS390X();
        return c;
    }

}
