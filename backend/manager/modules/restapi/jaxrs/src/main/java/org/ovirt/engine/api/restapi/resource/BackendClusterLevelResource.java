/*
Copyright (c) 2016 Red Hat, Inc.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

  http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/

package org.ovirt.engine.api.restapi.resource;

import java.util.Objects;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import org.ovirt.engine.api.model.ClusterLevel;
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
}
