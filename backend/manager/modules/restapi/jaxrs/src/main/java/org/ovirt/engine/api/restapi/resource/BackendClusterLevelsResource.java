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

import static java.util.stream.Collectors.toList;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.ovirt.engine.api.model.ClusterLevel;
import org.ovirt.engine.api.model.ClusterLevels;
import org.ovirt.engine.api.model.CpuType;
import org.ovirt.engine.api.model.CpuTypes;
import org.ovirt.engine.api.model.Permit;
import org.ovirt.engine.api.model.Permits;
import org.ovirt.engine.api.resource.ClusterLevelResource;
import org.ovirt.engine.api.resource.ClusterLevelsResource;
import org.ovirt.engine.api.restapi.types.CPUMapper;
import org.ovirt.engine.api.restapi.types.PermitMapper;
import org.ovirt.engine.api.restapi.util.LinkHelper;
import org.ovirt.engine.core.common.businessentities.ActionGroup;
import org.ovirt.engine.core.common.businessentities.ServerCpu;
import org.ovirt.engine.core.common.queries.ConfigurationValues;
import org.ovirt.engine.core.common.queries.GetAllServerCpuListParameters;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.Version;

public class BackendClusterLevelsResource extends BackendResource implements ClusterLevelsResource {
    @Override
    public ClusterLevels list() {
        ClusterLevels levels = new ClusterLevels();
        for (String version : getSupportedClusterLevels()) {
            levels.getClusterLevels().add(makeClusterLevel(version));
        }
        return levels;
    }

    public ClusterLevel makeClusterLevel(String version) {
        ClusterLevel level = new ClusterLevel();
        level.setId(version);

        // Not exposing CPU list filtered queries:
        if (!isFiltered()) {
            CpuTypes cpuTypes = new CpuTypes();
            for (ServerCpu serverCpu : getServerCpuList(version)) {
                CpuType cpuType = new CpuType();
                cpuType.setName(serverCpu.getCpuName());
                cpuType.setLevel(serverCpu.getLevel());
                cpuType.setArchitecture(CPUMapper.map(serverCpu.getArchitecture(), null));
                cpuTypes.getCpuTypes().add(cpuType);
            }
            level.setCpuTypes(cpuTypes);
        }

        // Add permits:
        Permits permits = new Permits();
        for (ActionGroup actionGroup : getActionGroups()) {
            Permit permit = PermitMapper.map(actionGroup, null);
            permits.getPermits().add(permit);
        }
        level.setPermits(permits);

        return LinkHelper.addLinks(level);
    }

    private List<ServerCpu> getServerCpuList(String version) {
        return getEntity(
           List.class, VdcQueryType.GetAllServerCpuList,
           new GetAllServerCpuListParameters(new Version(version)),
           version
        );
    }

    private List<ActionGroup> getActionGroups() {
        return Arrays.asList(ActionGroup.values());
    }

    public List<String> getSupportedClusterLevels() {
        Set<Version> versions = getConfigurationValueDefault(ConfigurationValues.SupportedClusterLevels);
        if (versions == null) {
            return Collections.emptyList();
        }
        return versions.stream()
            .map(version -> String.format("%s.%s", version.getMajor(), version.getMinor()))
            .collect(toList());
    }

    @Override
    public ClusterLevelResource getLevelResource(String id) {
        return inject(new BackendClusterLevelResource(id, this));
    }
}
