package org.ovirt.engine.api.restapi.util;

import java.util.List;

import org.ovirt.engine.api.restapi.resource.BackendResource;
import org.ovirt.engine.core.common.businessentities.VmRngDevice;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.Guid;

public class VmHelper {
    public static List<String> getVirtioScsiControllersForEntity(BackendResource resource, Guid id) {
        return resource.getEntity(List.class,
                VdcQueryType.GetVirtioScsiControllers,
                new IdQueryParameters(id),
                "GetVirtioScsiControllers", true);
    }

    public static List<String> getSoundDevicesForEntity(BackendResource resource, Guid id) {
        return resource.getEntity(List.class,
                VdcQueryType.GetSoundDevices,
                new IdQueryParameters(id),
                "GetSoundDevices", true);
    }

    public static Boolean isMemoryBalloonEnabledForEntity(BackendResource resource, Guid id) {
        return resource.getEntity(Boolean.class,
                VdcQueryType.IsBalloonEnabled,
                new IdQueryParameters(id),
                "IsBalloonEnabled", true);
    }

    public static List<VmRngDevice> getRngDevicesForEntity(BackendResource resource, Guid id) {
        return resource.getEntity(List.class,
                VdcQueryType.GetRngDevice,
                new IdQueryParameters(id),
                "GetRngDevice", true);
    }
}
