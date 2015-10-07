/*
Copyright (c) 2015 Red Hat, Inc.

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

package services;

import org.ovirt.api.metamodel.annotations.In;
import org.ovirt.api.metamodel.annotations.Out;
import org.ovirt.api.metamodel.annotations.Service;
import services.gluster.GlusterHooksService;
import services.gluster.GlusterVolumesService;
import types.Cluster;

@Service
public interface ClusterService {
    interface Get {
        @Out Cluster cluster();
    }

    interface Update {
        @In @Out Cluster cluster();
    }

    interface Remove {
    }

    interface ResetEmulatedMachine {
    }

    @Service AffinityGroupsService affinityGroups();
    @Service AssignedCpuProfilesService cpuProfiles();
    @Service AssignedNetworksService networks();
    @Service AssignedPermissionsService permissions();
    @Service GlusterHooksService glusterHooks();
    @Service GlusterVolumesService glusterVolumes();
}
