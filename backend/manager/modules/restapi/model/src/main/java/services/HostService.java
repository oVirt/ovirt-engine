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
        /**
         * Indicates if the activation should be performed asynchronously.
         */
        @In Boolean async();
    }

    interface Approve {
        @In Cluster cluster();

        /**
         * Indicates if the approval should be performed asynchronously.
         */
        @In Boolean async();
    }

    interface CommitNetConfig {
        /**
         * Indicates if the action should be performed asynchronously.
         */
        @In Boolean async();
    }

    interface Deactivate {
        @In String reason();

        /**
         * Indicates if the deactivation should be performed asynchronously.
         */
        @In Boolean async();
    }

    interface EnrollCertificate {
        /**
         * Indicates if the enrollment should be performed asynchronously.
         */
        @In Boolean async();
    }

    interface Fence {
        @In String fenceType();
        @Out PowerManagement powerManagement();

        /**
         * Indicates if the fencing should be performed asynchronously.
         */
        @In Boolean async();
    }

    interface ForceSelectSpm {
        /**
         * Indicates if the action should be performed asynchronously.
         */
        @In Boolean async();
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

        /**
         * Indicates if the installation should be performed asynchronously.
         */
        @In Boolean async();
    }

    interface IscsiDiscover {
        @In IscsiDetails iscsi();
        @Out String[] iscsiTargets();

        /**
         * Indicates if the discovery should be performed asynchronously.
         */
        @In Boolean async();
    }

    interface IscsiLogin {
        @In IscsiDetails iscsi();

        /**
         * Indicates if the login should be performed asynchronously.
         */
        @In Boolean async();
    }

    interface UnregisteredStorageDomainsDiscover {
        @In IscsiDetails iscsi();
        @Out StorageDomain[] storageDomains();

        /**
         * Indicates if the discovery should be performed asynchronously.
         */
        @In Boolean async();
    }

    interface Update {
        @In @Out Host host();

        /**
         * Indicates if the update should be performed asynchronously.
         */
        @In Boolean async();
    }

    interface Upgrade {
        /**
         * Indicates if the upgrade should be performed asynchronously.
         */
        @In Boolean async();
    }

    interface Refresh {
        /**
         * Indicates if the refresh should be performed asynchronously.
         */
        @In Boolean async();
    }

    interface Remove {
        /**
         * Indicates if the remove should be performed asynchronously.
         */
        @In Boolean async();
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

        /**
         * Indicates if the action should be performed asynchronously.
         */
        @In Boolean async();
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
