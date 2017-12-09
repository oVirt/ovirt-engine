package org.ovirt.engine.api.restapi.resource;

import static org.ovirt.engine.api.restapi.utils.GuidUtils.asGuid;

import org.ovirt.engine.api.model.Action;
import org.ovirt.engine.api.model.RegistrationVnicProfileMapping;
import org.ovirt.engine.api.model.RegistrationVnicProfileMappings;
import org.ovirt.engine.api.model.VnicProfileMapping;

public class BackendVnicProfileHelper {

    // TODO these validations should be moved to the ovirt-engine-api-model and verified with @InputDetail
    @Deprecated
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

    /**
     * <pre>
     * Validation notes:
     *
     * - the validation uses {@link BackendResource} for consistent localized error messages
     * - source network profile name (from.name) and source network name (from.network.name) must
     *   be specified in the input
     * - source network profile name (from.name) and source network name (from.network.name) may
     *   be specified as empty to denote an {@code <empty>} profile:
     *   {@literal
     *    <from>
     *        <name></name>
     *        <network>
     *            <name></name>
     *        </network>
     *    </from>
     *   }
     *   or:
     *   {@literal
     *    <from>
     *        <name />
     *        <network>
     *            <name />
     *        </network>
     *    </from>
     *   }
     *   in this case the empty strings are transformed into nulls which is the internal representation of
     *   the engine for 'no profile'
     * - a target (to) profile id is optional. if it is not specified the {@code <empty>} profile will be assigned
     * </pre>
     */
    public static void validateRegistrationVnicMappings(BackendResource br, Action action) {
        if (!action.isSetRegistrationConfiguration()) {
            return;
        }
        if (!action.getRegistrationConfiguration().isSetVnicProfileMappings()) {
            return;
        }
        RegistrationVnicProfileMappings mappings = action.getRegistrationConfiguration().getVnicProfileMappings();
        br.validateParameters(mappings, "registrationVnicProfileMappings");
        for (RegistrationVnicProfileMapping mapping: mappings.getRegistrationVnicProfileMappings()) {
            br.validateParameters(mapping, "from");
            // backendResource treats entries with empty strings as missing entries, so has to be used here under
            // condition of null which means the entries themselves are missing
            if (mapping.getFrom().getName() == null) {
                br.validateParameters(mapping, "from.name");
            }
            br.validateParameters(mapping, "from.network");
            if (mapping.getFrom().getNetwork().getName() == null) {
                br.validateParameters(mapping, "from.network.name");
            }
            // check for 'no profile'
            if ("".equalsIgnoreCase(mapping.getFrom().getName())) {
                // transform empty string to null because 'null' is the internal
                // representation of the engine for 'no profile'
                mapping.getFrom().setName(null);
            }
            if ("".equalsIgnoreCase(mapping.getFrom().getNetwork().getName())) {
                // transform empty string to null because 'null' is the internal
                // representation of the engine for 'no profile'
                mapping.getFrom().getNetwork().setName(null);
            }
            // target is optional
            if (mapping.isSetTo() && mapping.getTo().isSetId()) {
                //validate guid
                asGuid(mapping.getTo().getId());
            }
        }
    }

    public static boolean foundDeprecatedVnicProfileMapping(Action action) {
        return action.isSetVnicProfileMappings() &&
                (!action.isSetRegistrationConfiguration() || !action.getRegistrationConfiguration().isSetVnicProfileMappings());
    }
}
