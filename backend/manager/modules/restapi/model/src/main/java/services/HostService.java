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
import services.externalhostproviders.KatelloErrataService;
import types.Host;

@Service
public interface HostService extends MeasurableService {
    interface Activate {
    }

    interface Approve {
    }

    interface CommitNetConfig {
    }

    interface Deactivate {
    }

    interface EnrollCertificate {
    }

    interface Fence {
    }

    interface ForceSelectSpm {
    }

    interface Get {
        @Out Host host();
    }

    interface Install {
    }

    interface IscsiDiscover {
    }

    interface IscsiLogin {
    }

    interface UnregisteredStorageDomainsDiscover {
    }

    interface Update {
        @In @Out Host host();
    }

    interface Upgrade {
    }

    interface Refresh {
    }

    interface Remove {
    }

    interface SetupNetworks {
    }

    @Service AssignedPermissionsService permissions();
    @Service AssignedTagsService tags();
    @Service FenceAgentsService fenceAgents();
    @Service HostDevicesService devices();
    @Service HostHooksService hooks();
    @Service HostNicsService nics();
    @Service HostNumaNodesService numaNodes();
    @Service HostStorageService storage();
    @Service KatelloErrataService katelloErrata();
    @Service NetworkAttachmentsService networkAttachments();
    @Service StorageServerConnectionExtensionsService storageConnectionExtensions();
    @Service UnmanagedNetworksService unmanagedNetworks();
}
