package org.ovirt.engine.api.restapi.types;

import org.ovirt.engine.api.model.DataCenter;
import org.ovirt.engine.api.model.IscsiBond;
import org.ovirt.engine.api.model.Network;
import org.ovirt.engine.api.model.Networks;
import org.ovirt.engine.api.model.StorageConnection;
import org.ovirt.engine.api.model.StorageConnections;
import org.ovirt.engine.core.compat.Guid;

public class IscsiBondMapper {

    @Mapping(from = IscsiBond.class, to = org.ovirt.engine.core.common.businessentities.IscsiBond.class)
    public static org.ovirt.engine.core.common.businessentities.IscsiBond map(IscsiBond from,
                                                                              org.ovirt.engine.core.common.businessentities.IscsiBond to) {
        org.ovirt.engine.core.common.businessentities.IscsiBond iscsiBond = (to != null) ?
                to : new org.ovirt.engine.core.common.businessentities.IscsiBond();

        if (from.isSetId()) {
            iscsiBond.setId(Guid.createGuidFromString(from.getId()));
        }

        if (from.isSetDataCenter() && from.getDataCenter().isSetId()) {
            iscsiBond.setStoragePoolId(Guid.createGuidFromString(from.getDataCenter().getId()));
        }

        if (from.isSetName()) {
            iscsiBond.setName(from.getName());
        }

        if (from.isSetDescription()) {
            iscsiBond.setDescription(from.getDescription());
        }

        if (from.isSetStorageConnections()) {
            for (StorageConnection conn : from.getStorageConnections().getStorageConnections()) {
                iscsiBond.getStorageConnectionIds().add(conn.getId());
            }
        }

        if (from.isSetNetworks()) {
            for (Network network : from.getNetworks().getNetworks()) {
                iscsiBond.getNetworkIds().add(Guid.createGuidFromString(network.getId()));
            }
        }

        return iscsiBond;
    }

    @Mapping(from = org.ovirt.engine.core.common.businessentities.IscsiBond.class, to = IscsiBond.class)
    public static IscsiBond map(org.ovirt.engine.core.common.businessentities.IscsiBond from, IscsiBond to) {

        IscsiBond iscsiBond = (to != null) ? to : new IscsiBond();

        DataCenter dataCenter = new DataCenter();
        dataCenter.setId(from.getStoragePoolId().toString());

        iscsiBond.setDataCenter(dataCenter);
        iscsiBond.setName(from.getName());
        iscsiBond.setDescription(from.getDescription());

        if (from.getId() != null) {
            iscsiBond.setId(from.getId().toString());
        }

        Networks networks = new Networks();
        for (Guid id : from.getNetworkIds()) {
            Network network = new Network();
            network.setId(id.toString());
            networks.getNetworks().add(network);
        }
        iscsiBond.setNetworks(networks);

        StorageConnections connections = new StorageConnections();
        for (String id : from.getStorageConnectionIds()) {
            StorageConnection conn = new StorageConnection();
            conn.setId(id);
            connections.getStorageConnections().add(conn);
        }
        iscsiBond.setStorageConnections(connections);

        return iscsiBond;
    }
}
