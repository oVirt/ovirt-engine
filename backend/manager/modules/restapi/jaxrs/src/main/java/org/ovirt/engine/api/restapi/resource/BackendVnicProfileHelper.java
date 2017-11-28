package org.ovirt.engine.api.restapi.resource;

import org.ovirt.engine.api.model.Action;
import org.ovirt.engine.api.model.VnicProfileMapping;

public class BackendVnicProfileHelper {

    // TODO these validations should be moved to the ovirt-engine-api-model and verified with @InputDetail
    public static void validateVnicMappings(BackendResource bs, Action action) {
        if (action.isSetVnicProfileMappings()) {
            bs.validateParameters(action.getVnicProfileMappings(), "vnicProfileMappings");
            for (VnicProfileMapping mapping: action.getVnicProfileMappings().getVnicProfileMappings()) {
                bs.validateParameters(mapping, "sourceNetworkName");
                bs.validateParameters(mapping, "sourceNetworkProfileName");
                bs.validateParameters(mapping, "targetVnicProfile");
                bs.validateParameters(mapping, "targetVnicProfile.id");
                bs.asGuid(mapping.getTargetVnicProfile().getId());
            }
        }
    }
}
