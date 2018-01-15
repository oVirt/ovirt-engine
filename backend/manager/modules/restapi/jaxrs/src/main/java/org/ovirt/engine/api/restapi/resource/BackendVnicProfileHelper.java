package org.ovirt.engine.api.restapi.resource;

import org.ovirt.engine.api.model.Action;
import org.ovirt.engine.api.model.VnicProfileMapping;
public class BackendVnicProfileHelper {

    @Deprecated
    public static void validateVnicMappings(BackendResource br, Action action) {
        if (action.isSetVnicProfileMappings()) {
            br.validateParameters(action.getVnicProfileMappings(), "vnicProfileMappings");
            for (VnicProfileMapping mapping: action.getVnicProfileMappings().getVnicProfileMappings()) {
                br.validateParameters(mapping, "sourceNetworkName");
                br.validateParameters(mapping, "sourceNetworkProfileName");
                // target is optional
                if (mapping.isSetTargetVnicProfile() && mapping.getTargetVnicProfile().isSetId()) {
                    br.asGuid(mapping.getTargetVnicProfile().getId());
                }
            }
        }
    }

    public static boolean foundOnlyDeprecatedVnicProfileMapping(Action action) {
        return action.isSetVnicProfileMappings() &&
                (!action.isSetRegistrationConfiguration() || !action.getRegistrationConfiguration().isSetVnicProfileMappings());
    }
}
