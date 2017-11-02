package org.ovirt.engine.core.utils.archstrategy;

public interface ArchCommand {
    void runForX86_64();

    void runForPPC64();

    void runForS390X();
}
