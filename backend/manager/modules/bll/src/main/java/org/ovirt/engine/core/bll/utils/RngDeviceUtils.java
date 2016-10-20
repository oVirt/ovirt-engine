package org.ovirt.engine.core.bll.utils;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.interfaces.BackendInternal;
import org.ovirt.engine.core.common.action.RngDeviceParameters;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.businessentities.VmRngDevice;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.Version;

@Singleton
public class RngDeviceUtils {

    @Inject
    private BackendInternal backend;

    public void handleUrandomRandomChange(Version oldClusterVersion,
            Version newClusterVersion,
            Guid vmBaseId,
            CommandContext commandContext,
            boolean isVm) {
        if (oldClusterVersion == null) {
            return;
        }
        final boolean updatePotentiallyRequired =
                VmRngDevice.Source.urandomRandomUpdateRequired(oldClusterVersion, newClusterVersion);
        if (!updatePotentiallyRequired) {
            return;
        }
        final List<VmRngDevice> rngDevices =
                backend.runInternalQuery(
                        VdcQueryType.GetRngDevice, new IdQueryParameters(vmBaseId), commandContext.getEngineContext())
                        .getReturnValue();
        if (rngDevices.isEmpty()) {
            return;
        }
        final VmRngDevice rngDevice = rngDevices.get(0);
        final VmRngDevice.Source oldSource = rngDevice.getSource();
        rngDevice.updateSourceByVersion(newClusterVersion);
        if (rngDevice.getSource().equals(oldSource)) {
            return;
        }
        final RngDeviceParameters params = new RngDeviceParameters(rngDevice, isVm);
        backend.runInternalAction(VdcActionType.UpdateRngDevice, params, commandContext);
    }
}
