package org.ovirt.engine.api.restapi.types;

import org.ovirt.engine.api.model.Icon;
import org.ovirt.engine.api.restapi.utils.GuidUtils;
import org.ovirt.engine.core.common.businessentities.VmIcon;
import org.ovirt.engine.core.common.utils.Pair;

public class VmIconMapper {

    @Mapping(from = Icon.class, to = VmIcon.class)
    public static org.ovirt.engine.core.common.businessentities.VmIcon map(Icon model, VmIcon template) {
        final VmIcon entity =
                template != null ? template : new org.ovirt.engine.core.common.businessentities.VmIcon();
        if (model.isSetId()) {
            entity.setId(GuidUtils.asGuid(model.getId()));
        }
        if (model.isSetMediaType() && model.isSetData()) {
            entity.setTypeAndData(model.getMediaType(), model.getData());
        }
        return entity;
    }

    @Mapping(from = VmIcon.class, to = Icon.class)
    public static Icon map(VmIcon entity, Icon template) {
        final Icon model = template != null ? template : new Icon();
        model.setId(entity.getId().toString());
        final Pair<String, String> typeAndData = entity.getTypeAndData();
        model.setMediaType(typeAndData.getFirst());
        model.setData(typeAndData.getSecond());
        return model;
    }
}
