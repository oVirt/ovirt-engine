package org.ovirt.engine.core.bll.utils;

import java.util.List;
import java.util.Optional;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.interfaces.BackendInternal;
import org.ovirt.engine.core.common.businessentities.VmRngDevice;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.QueryType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.Version;

@Singleton
public class RngDeviceUtils {

    @Inject
    private BackendInternal backend;

    /**
     * It returns rng device of vm-like entity specified by {@code vmBaseId} if the entity already has an rng device
     * and it is necessary to change it in order to accommodate random -> urandom transition (i.e. if effective
     * compatibility havel has changed and the change crossed {@link VmRngDevice.Source#FIRST_URANDOM_VERSION} border).
     * @param oldCompatibilityVersion old effective compatibility version
     * @param newCompatibilityVersion new effective compatibility version
     */
    public Optional<VmRngDevice> createUpdatedRngDeviceIfNecessary(Version oldCompatibilityVersion,
            Version newCompatibilityVersion,
            Guid vmBaseId,
            CommandContext commandContext) {
        if (oldCompatibilityVersion == null) {
            return Optional.empty();
        }
        final boolean updatePotentiallyRequired =
                VmRngDevice.Source.urandomRandomUpdateRequired(oldCompatibilityVersion, newCompatibilityVersion);
        if (!updatePotentiallyRequired) {
            return Optional.empty();
        }
        final List<VmRngDevice> rngDevices =
                backend.runInternalQuery(
                        QueryType.GetRngDevice, new IdQueryParameters(vmBaseId), commandContext.getEngineContext())
                        .getReturnValue();
        if (rngDevices.isEmpty()) {
            return Optional.empty();
        }
        return updateRngDevice(newCompatibilityVersion, rngDevices.get(0));
    }

    public Optional<VmRngDevice> updateRngDevice(Version newCompatibilityVersion, VmRngDevice rngDevice) {
        final VmRngDevice.Source oldSource = rngDevice.getSource();
        rngDevice.updateSourceByVersion(newCompatibilityVersion);
        if (rngDevice.getSource().equals(oldSource)) {
            return Optional.empty();
        }
        return Optional.of(rngDevice);
    }
}
