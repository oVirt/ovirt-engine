/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.restapi.resource;

import java.util.Objects;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import org.ovirt.engine.api.model.ClusterLevel;
import org.ovirt.engine.api.resource.ClusterFeaturesResource;
import org.ovirt.engine.api.resource.ClusterLevelResource;

public class BackendClusterLevelResource extends BackendResource implements ClusterLevelResource {
    private String id;
    private BackendClusterLevelsResource parent;

    public BackendClusterLevelResource(String id, BackendClusterLevelsResource parent) {
        super();
        this.id = id;
        this.parent = parent;
    }

    @Override
    public ClusterLevel get() {
        for (String version : parent.getSupportedClusterLevels()) {
            if (Objects.equals(version, id)) {
                return parent.makeClusterLevel(version);
            }
        }
        throw new WebApplicationException(Response.Status.NOT_FOUND);
    }

    @Override
    public ClusterFeaturesResource getClusterFeaturesResource() {
        return inject(new BackendClusterFeaturesResource(id));
    }
}
