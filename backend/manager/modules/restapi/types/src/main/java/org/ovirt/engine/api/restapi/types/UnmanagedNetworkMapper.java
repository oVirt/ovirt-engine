package org.ovirt.engine.api.restapi.types;

import org.ovirt.engine.api.model.HostNIC;
import org.ovirt.engine.api.model.UnmanagedNetwork;
import org.ovirt.engine.api.restapi.utils.GuidUtils;
import org.ovirt.engine.api.restapi.utils.HexUtils;

public class UnmanagedNetworkMapper {
    @Mapping(from = UnmanagedNetwork.class, to = org.ovirt.engine.core.common.businessentities.UnmanagedNetwork.class)
    public static org.ovirt.engine.core.common.businessentities.UnmanagedNetwork map(UnmanagedNetwork model, org.ovirt.engine.core.common.businessentities.UnmanagedNetwork template) {
        if (model == null) {
            return template;
        }

        org.ovirt.engine.core.common.businessentities.UnmanagedNetwork entity = template == null ?
                new org.ovirt.engine.core.common.businessentities.UnmanagedNetwork() : template;

        if (model.isSetId()) {
            entity.setId(HexUtils.hex2string(model.getId()));
        }

        if (model.isSetName()) {
            entity.setNetworkName(model.getName());
        }

        if (model.isSetHostNic()) {
            HostNIC hostNic = model.getHostNic();
            if (hostNic.isSetId()) {
                entity.setNicId(GuidUtils.asGuid(hostNic.getId()));
            }

            if (hostNic.isSetName()) {
                entity.setNicName(hostNic.getName());
            }
        }

        return entity;
    }

    @Mapping(from = org.ovirt.engine.core.common.businessentities.UnmanagedNetwork.class, to = UnmanagedNetwork.class)
    public static UnmanagedNetwork map(org.ovirt.engine.core.common.businessentities.UnmanagedNetwork entity, UnmanagedNetwork template) {
        if (entity == null) {
            return template;
        }

        UnmanagedNetwork model = template == null ? new UnmanagedNetwork() : template;

        model.setId(HexUtils.string2hex(entity.getId()));

        if (entity.getNicId() != null) {
            HostNIC hostNic = new HostNIC();
            hostNic.setId(entity.getNicId().toString());
            model.setHostNic(hostNic);
        }

        model.setName(entity.getNetworkName());

        return model;
    }
}
