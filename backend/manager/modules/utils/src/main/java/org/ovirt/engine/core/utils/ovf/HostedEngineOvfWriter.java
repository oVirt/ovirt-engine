package org.ovirt.engine.core.utils.ovf;

import java.util.List;
import java.util.Objects;

import javax.validation.constraints.NotNull;

import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.compat.Version;

/**
 * Specialized writer for hosted engine VM. By design the hosted engine vm is lacking its cluster information because it
 * is always started by the HA agent which doesn't have any knowledge of its cluster topology.
 */
public class HostedEngineOvfWriter extends OvfVmWriter {

    private final String emulatedMachine;
    private final String cpuId;

    public HostedEngineOvfWriter(
            @NotNull VM vm,
            @NotNull List<DiskImage> images,
            @NotNull Version version,
            @NotNull String emulatedMachine,
            @NotNull String cpuId) {
        super(vm, images, version);
        if (!vm.isHostedEngine()) {
            throw new IllegalArgumentException(
                    String.format("The VM %s isn't hosted engine - aborting the export", vm));
        }
        this.emulatedMachine = Objects.requireNonNull(emulatedMachine, "The cluster emulated machine must not be null");
        this.cpuId = Objects.requireNonNull(cpuId, "The cpuId must not be null");
    }

    @Override
    protected void writeCustomEmulatedMachine() {
        _writer.writeElement(OvfProperties.CUSTOM_EMULATED_MACHINE, emulatedMachine);
    }

    @Override
    protected void writeCustomCpuName() {
        _writer.writeElement(OvfProperties.CUSTOM_CPU_NAME, cpuId);
    }
}
