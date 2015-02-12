package org.ovirt.engine.api.restapi.resource;

import java.util.List;

import org.ovirt.engine.api.model.Cluster;
import org.ovirt.engine.core.common.businessentities.VDSGroup;

public class BackendDataCenterClusterResource extends BackendClusterResource<BackendDataCenterClustersResource> {

    public BackendDataCenterClusterResource(BackendDataCenterClustersResource parent, String id) {
        super(id, parent);
    }

    @Override
    public Cluster get() {
        VDSGroup entity = getVdsGroup();
        if (entity == null) {
            return notFound();
        }
        return addLinks(map(entity));
    }

    private VDSGroup getVdsGroup() {
        List<VDSGroup> vdsGroups = parent.getVdsGroups();
        for (VDSGroup entity : vdsGroups) {
            if (entity.getId().toString().equals(id)) {
                return entity;
            }
        }
        return null;
    }

    public BackendDataCenterClustersResource getParent() {
        return parent;
    }

}
