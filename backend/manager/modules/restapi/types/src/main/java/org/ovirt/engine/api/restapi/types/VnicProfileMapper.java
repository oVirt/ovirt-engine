package org.ovirt.engine.api.restapi.types;

import org.ovirt.engine.api.model.CustomProperties;
import org.ovirt.engine.api.model.Network;
import org.ovirt.engine.api.model.QoS;
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
                    .convertProperties(CustomPropertiesParser.parse(model.getCustomProperties().getCustomProperty())));
        }
        if (model.isSetQos() && model.getQos().isSetId()) {
            entity.setNetworkQosId(GuidUtils.asGuid(model.getQos().getId()));
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
            hooks.getCustomProperty().addAll(CustomPropertiesParser.parse(
                    DevicePropertiesUtils.getInstance().convertProperties(entity.getCustomProperties()), false));
            model.setCustomProperties(hooks);
        }
        if (entity.getNetworkQosId() != null) {
            model.setQos(new QoS());
            model.getQos().setId(entity.getNetworkQosId().toString());
        }
        return model;
    }
}
