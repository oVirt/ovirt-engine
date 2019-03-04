package org.ovirt.engine.api.restapi.resource;

import java.util.List;

import org.ovirt.engine.api.restapi.util.LinkHelper;
import org.ovirt.engine.core.common.businessentities.Cluster;

public class BackendDataCenterClusterResource extends BackendClusterResource<BackendDataCenterClustersResource> {

    public BackendDataCenterClusterResource(BackendDataCenterClustersResource parent, String id) {
        super(id, parent);
    }

    @Override
    public org.ovirt.engine.api.model.Cluster get() {
        Cluster entity = getCluster();
        if (entity == null) {
            return notFound();
        }
        return addLinks(map(entity), LinkHelper.NO_PARENT);
    }

    private Cluster getCluster() {
        List<Cluster> clusters = parent.getClusters();
        for (Cluster entity : clusters) {
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
