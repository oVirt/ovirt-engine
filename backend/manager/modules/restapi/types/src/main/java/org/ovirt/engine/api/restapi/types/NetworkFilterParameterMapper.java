package org.ovirt.engine.api.restapi.types;

import org.ovirt.engine.api.model.NetworkFilterParameter;
import org.ovirt.engine.api.restapi.utils.GuidUtils;
import org.ovirt.engine.core.common.businessentities.network.VmNicFilterParameter;

public class NetworkFilterParameterMapper {
    @Mapping(from = NetworkFilterParameter.class, to = VmNicFilterParameter.class)
    public static VmNicFilterParameter map(NetworkFilterParameter model, VmNicFilterParameter template) {
        VmNicFilterParameter entity = template != null ? template : new VmNicFilterParameter();
        if (model.isSetId()) {
            entity.setId(GuidUtils.asGuid(model.getId()));
        }
        if (model.isSetName()) {
            entity.setName(model.getName());
        }
        if (model.isSetValue()) {
            entity.setValue(model.getValue());
        }
        return entity;
    }

    @Mapping(from = VmNicFilterParameter.class, to = NetworkFilterParameter.class)
    public static NetworkFilterParameter map(VmNicFilterParameter entity, NetworkFilterParameter template) {
        NetworkFilterParameter model = template != null ? template : new NetworkFilterParameter();
        if (entity.getId() != null) {
            model.setId(entity.getId().toString());
        }
        if (entity.getName() != null) {
            model.setName(entity.getName());
        }
        if (entity.getValue() != null) {
            model.setValue(entity.getValue());
        }
        return model;
    }
}
