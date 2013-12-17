package org.ovirt.engine.core.utils.archstrategy;

import org.ovirt.engine.core.common.businessentities.ArchitectureType;

public interface ArchStrategy {

    public ArchitectureType getArchitecture();

    public <T extends ArchCommand> T run(T c);
}
