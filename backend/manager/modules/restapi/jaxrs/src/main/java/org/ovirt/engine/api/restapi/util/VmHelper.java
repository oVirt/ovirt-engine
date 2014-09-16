package org.ovirt.engine.api.restapi.util;

import org.ovirt.engine.api.restapi.resource.BackendResource;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.Guid;

import java.util.List;

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
}
