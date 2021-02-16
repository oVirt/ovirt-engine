package org.ovirt.engine.api.restapi.types;

import org.ovirt.engine.api.model.CustomProperties;
import org.ovirt.engine.api.model.Network;
import org.ovirt.engine.api.model.NetworkFilter;
import org.ovirt.engine.api.model.Qos;
import org.ovirt.engine.api.model.VnicPassThrough;
import org.ovirt.engine.api.model.VnicPassThroughMode;
import org.ovirt.engine.api.model.VnicProfile;
import org.ovirt.engine.api.restapi.utils.CustomPropertiesParser;
import org.ovirt.engine.api.restapi.utils.GuidUtils;
import org.ovirt.engine.core.utils.customprop.DevicePropertiesUtils;

public class VnicProfileMapper {
    @Mapping(from = VnicProfile.class, to = org.ovirt.engine.core.common.businessentities.network.VnicProfile.class)
    public static org.ovirt.engine.core.common.businessentities.network.VnicProfile map(VnicProfile model,
            org.ovirt.engine.core.common.businessentities.network.VnicProfile template) {
        org.ovirt.engine.core.common.businessentities.network.VnicProfile entity =
                template != null ? template : new org.ovirt.engine.core.common.businessentities.network.VnicProfile();
        if (model.isSetId()) {
            entity.setId(GuidUtils.asGuid(model.getId()));
        }
        if (model.isSetName()) {
            entity.setName(model.getName());
        }
        if (model.isSetDescription()) {
            entity.setDescription(model.getDescription());
        }
        if (model.isSetNetwork() && model.getNetwork().isSetId()) {
            entity.setNetworkId(GuidUtils.asGuid(model.getNetwork().getId()));
        }
        if (model.isSetPortMirroring()) {
            entity.setPortMirroring(model.isPortMirroring());
        }
        if (model.isSetCustomProperties()) {
            entity.setCustomProperties(DevicePropertiesUtils.getInstance()
                    .convertProperties(CustomPropertiesParser.parse(model.getCustomProperties().getCustomProperties())));
        }
        if (model.isSetQos()) {
            if (model.getQos().isSetId()) {
                entity.setNetworkQosId(GuidUtils.asGuid(model.getQos().getId()));
            } else {
                entity.setNetworkQosId(null);
            }
        }
        if (model.isSetPassThrough() && model.getPassThrough().isSetMode()) {
            entity.setPassthrough(map(model.getPassThrough().getMode()));
        }

        if (entity.isPassthrough() && model.isSetMigratable()) {
            entity.setMigratable(model.isMigratable());
        }

        if (model.isSetFailover() && model.getFailover().isSetId()) {
            entity.setFailoverVnicProfileId(GuidUtils.asGuid(model.getFailover().getId()));
        }

        if (model.isSetNetworkFilter()) {
            if (model.getNetworkFilter().isSetId()) {
                entity.setNetworkFilterId(GuidUtils.asGuid(model.getNetworkFilter().getId()));
            } else {
                entity.setNetworkFilterId(null);
            }
        }
        return entity;
    }

    @Mapping(from = org.ovirt.engine.core.common.businessentities.network.VnicProfile.class, to = VnicProfile.class)
    public static VnicProfile map(org.ovirt.engine.core.common.businessentities.network.VnicProfile entity,
            VnicProfile template) {
        VnicProfile model = template != null ? template : new VnicProfile();
        if (entity.getId() != null) {
            model.setId(entity.getId().toString());
        }
        if (entity.getName() != null) {
            model.setName(entity.getName());
        }
        if (entity.getDescription() != null) {
            model.setDescription(entity.getDescription());
        }
        if (entity.getNetworkId() != null) {
            model.setNetwork(new Network());
            model.getNetwork().setId(entity.getNetworkId().toString());
        }
        model.setPortMirroring(entity.isPortMirroring());
        if (entity.getCustomProperties() != null && !entity.getCustomProperties().isEmpty()) {
            CustomProperties hooks = new CustomProperties();
            hooks.getCustomProperties().addAll(CustomPropertiesParser.parse(
                    DevicePropertiesUtils.getInstance().convertProperties(entity.getCustomProperties()), false));
            model.setCustomProperties(hooks);
        }
        if (entity.getNetworkQosId() != null) {
            model.setQos(new Qos());
            model.getQos().setId(entity.getNetworkQosId().toString());
        }
        final VnicPassThrough vnicPassThrough = new VnicPassThrough();
        vnicPassThrough.setMode(map(entity.isPassthrough()));
        model.setPassThrough(vnicPassThrough);
        if (entity.getNetworkFilterId() != null){
            model.setNetworkFilter(new NetworkFilter());
            model.getNetworkFilter().setId(entity.getNetworkFilterId().toString());
        }

        if (entity.isPassthrough()) {
            model.setMigratable(entity.isMigratable());
        }

        if (entity.getFailoverVnicProfileId() != null) {
            model.setFailover(new VnicProfile());
            model.getFailover().setId(entity.getFailoverVnicProfileId().toString());
        }

        return model;
    }

    private static boolean map(VnicPassThroughMode vnicPassThroughMode) {
        return VnicPassThroughMode.ENABLED == vnicPassThroughMode;
    }

    private static VnicPassThroughMode map(boolean value) {
        return value ? VnicPassThroughMode.ENABLED : VnicPassThroughMode.DISABLED;
    }
}
