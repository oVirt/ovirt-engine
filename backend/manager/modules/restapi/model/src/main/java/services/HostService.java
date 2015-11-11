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
import types.Cluster;
import types.Host;
import types.HostNic;
import types.IscsiDetails;
import types.Label;
import types.NetworkAttachment;
import types.PowerManagement;
import types.Ssh;
import types.StorageDomain;

@Service
public interface HostService extends MeasurableService {
    interface Activate {
    }

    interface Approve {
        @In Cluster cluster();
    }

    interface CommitNetConfig {
    }

    interface Deactivate {
        @In String reason();
    }

    interface EnrollCertificate {
    }

    interface Fence {
        @In String fenceType();
        @Out PowerManagement powerManagement();
    }

    interface ForceSelectSpm {
    }

    interface Get {
        @Out Host host();
    }

    interface Install {
        /**
         * The password of of the `root` user, used to connect to the host via SSH.
         */
        @In String rootPassword();

        /**
         * The SSH details used to connect to the host.
         */
        @In Ssh ssh();

        /**
         * This `override_iptables` property is used to indicate if the firewall configuration should be
         * replaced by the default one.
         */
        @In Host host();

        /**
         * When installing an oVirt node a image ISO file is needed.
         */
        @In String image();
    }

    interface IscsiDiscover {
        @In IscsiDetails iscsi();
        @Out String[] iscsiTargets();
    }

    interface IscsiLogin {
        @In IscsiDetails iscsi();
    }

    interface UnregisteredStorageDomainsDiscover {
        @In IscsiDetails iscsi();
        @Out StorageDomain[] storageDomains();
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
        @In NetworkAttachment[] modifiedNetworkAttachments();
        @In NetworkAttachment[] removedNetworkAttachments();
        @In NetworkAttachment[] synchronizedNetworkAttachments();
        @In HostNic[] modifiedBonds();
        @In HostNic[] removedBonds();
        @In Label[] modifiedLabels();
        @In Label[] removedLabels();
        @In Boolean checkConnectivity();
        @In Integer connectivityTimeout();
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
