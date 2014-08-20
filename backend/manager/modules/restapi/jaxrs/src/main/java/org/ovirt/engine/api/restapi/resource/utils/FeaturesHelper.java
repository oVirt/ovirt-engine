package org.ovirt.engine.api.restapi.resource.utils;

import java.util.Date;

import org.ovirt.engine.api.model.API;
import org.ovirt.engine.api.model.CPU;
import org.ovirt.engine.api.model.CpuTopology;
import org.ovirt.engine.api.model.Feature;
import org.ovirt.engine.api.model.Features;
import org.ovirt.engine.api.model.GlusterVolumes;
import org.ovirt.engine.api.model.Header;
import org.ovirt.engine.api.model.Headers;
import org.ovirt.engine.api.model.Host;
import org.ovirt.engine.api.model.NIC;
import org.ovirt.engine.api.model.Parameter;
import org.ovirt.engine.api.model.ParametersSet;
import org.ovirt.engine.api.model.PortMirroring;
import org.ovirt.engine.api.model.StorageType;
import org.ovirt.engine.api.model.StorageTypes;
import org.ovirt.engine.api.model.TransparentHugePages;
import org.ovirt.engine.api.model.Url;
import org.ovirt.engine.api.model.Version;
import org.ovirt.engine.api.restapi.resource.BackendCapabilitiesResource;
import org.ovirt.engine.api.restapi.types.DateMapper;
import org.ovirt.engine.api.restapi.utils.VersionUtils;

public class FeaturesHelper {

    public Features getFeatures(Version version) {
        Features features = new Features();
        if (VersionUtils.greaterOrEqual(version, BackendCapabilitiesResource.VERSION_3_0)) {
            addFeatureTransparentHugePages(features);
        }
        if (VersionUtils.greaterOrEqual(version, BackendCapabilitiesResource.VERSION_3_1)) {
            addFeatureGluster(features);
            addFeaturePosixDevice(features);
            addFeaturePortMirroring(features);
            addFeatureServerTime(features);
            addFeatureHostMemory(features);
            addFeatureHostSockets(features, version);
            addFeatureIgnoreCase(features);
            addFeatureMaxResults(features);
            addFeatureJSONContentType(features);
            addFeatureCorrelationId(features);
            addFeatureDiskActivation(features);
            addFeatureNicActivation(features);
            addFeatureSnapshotsRefactoring(features);
            addFeatureRemoveTemplateFromSD(features);
            addFeatureFloatingDisks(features);
            addFeatureAsyncDelete(features);
            addFeatureSessionBasedAuthentication(features);
        }
        if (VersionUtils.greaterOrEqual(version, BackendCapabilitiesResource.VERSION_3_3)) {
            addFeatureVmApplications(features);
            addFeatureVirtioScsi(features);
            addFeatureComment(features);
            addFeatureRefreshHostCapabilities(features);
            addFeatureMemorySnapshot(features);
            addWatchdogFeature(features);
            addSshAuthenticationFeature(features);
            addForceSelectSpmFeature(features);
            addConsoleFeature(features);
            addFeatureStorageServerConnections(features);
            addFeatureStorageServerConnectionsForDomain(features);
            addFeatureAttachDetachStorageServerConnectionsForDomain(features);
            addSingleQxlPciFeature(features);
            addFeatureAddVmFromOvf(features);
            addVnicProfilesFeature(features);
            addStorageDomainImageFeature(features);
            addGlusterHooksFeature(features);
            addFeatureReportVmFQDN(features);
            addFeatureAttachDiskSnapshot(features);
            addFeatureCloudInit(features);
            addFeatureSchedulingPolicy(features);
        }
        if (VersionUtils.greaterOrEqual(version, BackendCapabilitiesResource.VERSION_3_4)) {
            addGlusterBricksFeature(features);
            addFeatureCopyMoveDiskInAdditionalContext(features);
            addNetworkLabelsFeature(features);
            addRebootFeature(features);
            addMaintenanceFeature(features);
            addIscsiBondFeature(features);
        }
        if (VersionUtils.greaterOrEqual(version, BackendCapabilitiesResource.VERSION_3_5)) {
            addBookmarksFeature(features);
            addNetworkCustomPropertiesFeature(features);
            addFeatureRemoveDiskFromVmSnapshot(features);
            addFeatureDiskSnapshotsResourceInStorageDomainContext(features);
            addInstanceTypesFeature(features);
            addNumaNodesFeature(features);
            addMacPoolsFeature(features);
        }
        return features;
    }

    private void addFeatureSchedulingPolicy(Features features) {
        Feature feature = new Feature();
        feature.setName("Scheduling policy units list");
        feature.setDescription("Internal/External policy units used by VM Scheduler (filter/weight/balance)");
        features.getFeature().add(feature);
        feature = new Feature();
        feature.setName("Scheduling policies collection");
        feature.setDescription("Ability to get a list of oVirt's scheduling policies");
        features.getFeature().add(feature);
    }

    private void addFeatureDiskSnapshotsResourceInStorageDomainContext(Features features) {
        Feature feature = new Feature();
        feature.setName("Disk Snapshots list in Storage Domain context");
        feature.setDescription("Ability to get a list of Disk Snapshots by a Storage Domain");
        features.getFeature().add(feature);
    }

    private void addFeatureRemoveDiskFromVmSnapshot(Features features) {
        Feature feature = new Feature();
        feature.setName("Remove disk from VM snapshot");
        feature.setDescription("Ability to remove a disk from a VM snapshot");
        features.getFeature().add(feature);
    }

    private void addRebootFeature(Features features) {
        Feature feature = new Feature();
        feature.setName("Reboot VM");
        feature.setDescription("Ability to reboot VM");
        features.getFeature().add(feature);
    }

    private void addSingleQxlPciFeature(Features features) {
        Feature feature = new Feature();
        feature.setName("Single PCI for Qxl");
        feature.setDescription("Ability to view multiple video devices via single PCI guest device");
        features.getFeature().add(feature);
    }

    private void addSshAuthenticationFeature(Features features) {
        Feature feature = new Feature();
        feature.setName("SSH Authentication Method");
        feature.setDescription("Ability to authenticate by SSH to host using privileged user password or SSH public key");
        features.getFeature().add(feature);
    }

    private void addForceSelectSpmFeature(Features features) {
        Feature feature = new Feature();
        feature.setName("Force Select SPM");
        feature.setDescription("Ability to force select a host as SPM");
        features.getFeature().add(feature);
    }

    private void addWatchdogFeature(Features features) {
        Feature feature = new Feature();
        feature.setName("Watchdog Device");
        feature.setDescription("Ability to create watchdog devices in VM's");
        features.getFeature().add(feature);
    }

    private void addFeatureSessionBasedAuthentication(Features features) {
        Feature feature = new Feature();
        feature.setName("Session Based Authentication");
        feature.setDescription("Ability to maintain client-server session, to avoid login per request. Done by providing a header");
        features.getFeature().add(feature);
    }

    private void addFeatureAsyncDelete(Features features) {
        Feature feature = new Feature();
        feature.setName("Async Delete");
        feature.setDescription("Ability to specify that DELETE request will be performed asynchronously, using the 'async' url parameter.");
        Parameter async = new Parameter();
        async.setName("async");
        async.setValue("true|false");
        async.setType("boolean");
        async.setContext("matrix");
        feature.setUrl(new Url());
        feature.getUrl().getParametersSets().add(new ParametersSet());
        feature.getUrl().getParametersSets().get(0).getParameters().add(async);
        features.getFeature().add(feature);
    }

    private void addFeatureVmApplications(Features features) {
        Feature feature = new Feature();
        feature.setName("VM Applications");
        feature.setDescription("List of Applications installed on a VM. VM Applications appear under VM: .../api/vms/xxx/applications.");
        features.getFeature().add(feature);
    }

    private void addFeatureFloatingDisks(Features features) {
        Feature feature = new Feature();
        feature.setName("Floating Disks");
        feature.setDescription("A disk may be attached to 0 VMs. Such a disk is 'floating'. Therefore disks now also appear in the root collection, no only under VM: .../api/disks. Attaching a disk to VM is done by adding a disk with an existing ID to the VM.  Detaching a disk from a VM is done by deleting the Disk from the VM, supplying 'detach=true'");
        features.getFeature().add(feature);
    }

    private void addFeatureRemoveTemplateFromSD(Features features) {
        Feature feature = new Feature();
        feature.setName("Remove Template Disks From Specified Storage-Domain");
        feature.setDescription("Ability to remove template images from a specific storage-domain: DELETE .../api/templates/{template:id}/disks/{disk:id}, supplying <action><storage_domain id=\"xxx\"</storage_domain></action>");
        features.getFeature().add(feature);
    }

    private void addFeatureSnapshotsRefactoring(Features features) {
        Feature feature = new Feature();
        feature.setName("Snapshots Refactoring");
        feature.setDescription("Snapshot is no a point-of-time representation of a VM, including sublollections such as disks, nics, cdroms...");
        features.getFeature().add(feature);
    }

    private void addFeatureNicActivation(Features features) {
        Feature feature = new Feature();
        feature.setName("Activate/Deactivate NIC");
        feature.setDescription("NIC may be activated or deactivated (POST .../nics/{nic:id}/activate, POST .../nics/{nic:id}/deactivate");
        features.getFeature().add(feature);
    }

    private void addFeatureDiskActivation(Features features) {
        Feature feature = new Feature();
        feature.setName("Activate/Deactivate disk");
        feature.setDescription("Disk may be activated or deactivated (POST .../disks/{disk:id}/activate, POST .../disks/{disk:id}/deactivate");
        features.getFeature().add(feature);
    }

    private void addFeatureCorrelationId(Features features) {
        Feature feature = new Feature();
        feature.setName("Correlation-Id");
        feature.setDescription("Enable setting Correlation-Id for POST and PUT commands, using a header.");
        feature.setHeaders(new Headers());
        feature.getHeaders().getHeaders().add(new Header());
        feature.getHeaders().getHeaders().get(0).setName("Correlation-Id");
        feature.getHeaders().getHeaders().get(0).setValue("any string");
        features.getFeature().add(feature);
    }

    private void addFeatureJSONContentType(Features features) {
        Feature feature = new Feature();
        feature.setName("JSON Content-Type");
        feature.setHeaders(new Headers());
        feature.getHeaders().getHeaders().add(new Header());
        feature.getHeaders().getHeaders().get(0).setName("Content-Type");
        feature.getHeaders().getHeaders().get(0).setValue("application/json");
        features.getFeature().add(feature);
    }

    private void addFeatureTransparentHugePages(Features features) {
        Feature feature = new Feature();
        feature.setName("Transparent-Huge-Pages Memory Policy");
        feature.setTransparentHugepages(new TransparentHugePages());
        features.getFeature().add(feature);
    }

    private void addFeatureMaxResults(Features features) {
        Feature feature = new Feature();
        feature.setName("Max Results for GET Request");
        feature.setDescription("Ability to specify max number of results returned from a GET request");
        Parameter maxResults = new Parameter();
        maxResults.setName("max");
        maxResults.setValue("max results");
        maxResults.setType("int");
        maxResults.setContext("matrix");
        feature.setUrl(new Url());
        feature.getUrl().getParametersSets().add(new ParametersSet());
        feature.getUrl().getParametersSets().get(0).getParameters().add(maxResults);
        features.getFeature().add(feature);
    }

    private void addFeatureIgnoreCase(Features features) {
        Feature feature = new Feature();
        feature.setName("Search - Case Sensitivity");
        feature.setDescription("Ability to specify whether a search query should ignore case, by providing a URL parameter");
        Parameter ignoreCase = new Parameter();
        ignoreCase.setName("case_sensitive");
        ignoreCase.setValue("true|false");
        ignoreCase.setType("boolean");
        ignoreCase.setContext("matrix");
        feature.setUrl(new Url());
        feature.getUrl().getParametersSets().add(new ParametersSet());
        feature.getUrl().getParametersSets().get(0).getParameters().add(ignoreCase);
        features.getFeature().add(feature);
    }

    private void addFeatureHostSockets(Features features, Version version) {
        Feature feature = new Feature();
        feature.setHost(new Host());
        feature.setName("Display Host Sockets");
        feature.setDescription("Number of host sockets displayed. 'cores' now show num of cores per socket --> total cores in host is: cores*sockets");
        feature.getHost().setCpu(new CPU());
        feature.getHost().getCpu().setTopology(new CpuTopology());
        feature.getHost().getCpu().getTopology().setSockets(4);
        if (VersionUtils.greaterOrEqual(version, BackendCapabilitiesResource.VERSION_3_2)) {
            feature.getHost().getCpu().getTopology().setThreads(2);
        }
        features.getFeature().add(feature);
    }

    private void addFeatureHostMemory(Features features) {
        Feature feature = new Feature();
        feature.setName("Display Host Memory");
        feature.setHost(new Host());
        feature.getHost().setMemory(107374182400L);
        features.getFeature().add(feature);
    }

    private void addFeatureServerTime(Features features) {
        Feature feature = new Feature();
        feature.setName("Display Server Time");
        feature.setApi(new API());
        feature.getApi().setTime(DateMapper.map(new Date(), null));
        features.getFeature().add(feature);
    }

    private void addFeaturePortMirroring(Features features) {
        Feature feature = new Feature();
        feature.setName("Port Mirroring");
        feature.setNic(new NIC());
        feature.getNic().setPortMirroring(new PortMirroring());
        features.getFeature().add(feature);
    }

    private void addFeaturePosixDevice(Features features) {
        Feature feature = new Feature();
        feature.setName("POSIX-FS Storage Type");
        feature.setStorageTypes(new StorageTypes());
        feature.getStorageTypes().getStorageTypes().add(StorageType.POSIXFS.value());
        features.getFeature().add(feature);
    }

    private void addFeatureGluster(Features features) {
        Feature feature = new Feature();
        feature.setName("Gluster Support");
        feature.setDescription("Support for Gluster Volumes and Bricks");
        feature.setGlusterVolumes(new GlusterVolumes());
        features.getFeature().add(feature);
    }

    private void addFeatureVirtioScsi(Features features) {
        Feature feature = new Feature();
        feature.setName("VirtIO-SCSI Support");
        feature.setDescription("Support for paravirtualized SCSI controller device.");
        features.getFeature().add(feature);
    }

    private void addFeatureComment(Features features) {
        Feature feature = new Feature();
        feature.setName("Custom comment in the resource");
        feature.setDescription("At this point added ability to add custom comment only to the datacenter, in future versions we may allow it in other resources as well.");
        features.getFeature().add(feature);
    }

    private void addFeatureAttachDiskSnapshot(Features features) {
        Feature feature = new Feature();
        feature.setName("Attaching Disk snapshot to a vm");
        feature.setDescription("Support for attaching a disk snapshot to a vm.");
        features.getFeature().add(feature);
    }

    private void addFeatureRefreshHostCapabilities(Features features) {
        Feature feature = new Feature();
        feature.setName("Refresh Host Capabilities");
        feature.setDescription("Getting host data synchronized with getVdsCaps (GET .../hosts/{host:id};force");
        features.getFeature().add(feature);
    }

    private void addFeatureMemorySnapshot(Features features) {
        Feature feature = new Feature();
        feature.setName("Memory Snapshot");
        feature.setDescription("Ability to save memory state as part of snapshot.");
        features.getFeature().add(feature);
    }

    private void addConsoleFeature(Features features) {
        Feature feature = new Feature();
        feature.setName("Console Device");
        feature.setDescription("Ability to control attaching of console devices in VM's");
        features.getFeature().add(feature);
    }

    private void addFeatureStorageServerConnections(Features features) {
        Feature feature = new Feature();
        feature.setName("Storage server connections");
        feature.setDescription("Ability to manage storage server connections");
    }

    private void addFeatureAddVmFromOvf(Features features) {
        Feature feature = new Feature();
        feature.setName("Add VM from OVF configuration");
        feature.setDescription("Ability to add VM from provided OVF configuration.");
        features.getFeature().add(feature);
    }

    private void addFeatureStorageServerConnectionsForDomain(Features features) {
        Feature feature = new Feature();
        feature.setName("Storage server connections of a storage domain");
        feature.setDescription("Ability to view storage server connections of a specific storage domain (GET .../storagedomains/{storagedomain:id}/storageconnections)");
        features.getFeature().add(feature);
    }

    private void addVnicProfilesFeature(Features features) {
        Feature feature = new Feature();
        feature.setName("Vnic Profiles");
        feature.setDescription("Configuring VM network interface by a profile (for network QoS, custom properties and port mirroring)");
        features.getFeature().add(feature);
    }

    private void addStorageDomainImageFeature(Features features) {
        Feature feature = new Feature();
        feature.setName("Image Storage Domains (Tech Preview)");
        feature.setDescription("Importing and exporting images from and to image storage domain (as for example OpenStack glance)");
        features.getFeature().add(feature);
    }

    private void addGlusterHooksFeature(Features features) {
        Feature feature = new Feature();
        feature.setName("Gluster Hooks management");
        feature.setDescription("Ability to manage gluster hooks in cluster");
        features.getFeature().add(feature);
    }

    private void addFeatureAttachDetachStorageServerConnectionsForDomain(Features features) {
        Feature feature = new Feature();
        feature.setName("Attach/Detach storage server connections (to/from a storage domain)");
        feature.setDescription("Ability to attach/detach storage server connections to/from a specific storage domain (common use case: disaster recovery).");
        features.getFeature().add(feature);
    }

    private void addFeatureReportVmFQDN(Features features) {
        Feature feature = new Feature();
        feature.setName("VM FQDN");
        feature.setDescription("Ability to report the fully qualified domain name (FQDN) of a Virtual Machine");
        features.getFeature().add(feature);
    }

    private void addGlusterBricksFeature(Features features) {
        Feature feature = new Feature();
        feature.setName("Gluster Bricks management");
        feature.setDescription("Ability to delete gluster bricks with data migration using the actions migrate and DELETE. Action migrate in combination with stopmigrate, migrates the data and brick can be reused further.");
        features.getFeature().add(feature);
    }

    private void addFeatureCloudInit(Features features) {
        Feature feature = new Feature();
        feature.setName("Cloud Init");
        feature.setDescription("Support for VM initialization with Cloud Init.");
        features.getFeature().add(feature);
    }

    private void addFeatureCopyMoveDiskInAdditionalContext(Features features) {
        Feature feature = new Feature();
        feature.setName("Copy and Move backend disk");
        feature.setDescription("Support for copy/move disk in additional context.");
        features.getFeature().add(feature);
    }

    private void addNetworkLabelsFeature(Features features) {
        Feature feature = new Feature();
        feature.setName("Network Labels");
        feature.setDescription("Abilitiy to provision networks on hosts via labels.");
        features.getFeature().add(feature);
    }

    private void addMaintenanceFeature(Features features) {
        Feature feature = new Feature();
        feature.setName("Maintenance");
        feature.setDescription("Enable or disable VM maintenance mode.");
        features.getFeature().add(feature);
    }

    private void addBookmarksFeature(Features features) {
        Feature feature = new Feature();
        feature.setName("Bookmarks");
        feature.setDescription("Add/modify/remove bookmarks.");
        features.getFeature().add(feature);
    }

    private void addNetworkCustomPropertiesFeature(Features features) {
        Feature feature = new Feature();
        feature.setName("Network Custom Properties");
        feature.setDescription("Configure custom properties when provisioning networks on hosts.");
        features.getFeature().add(feature);
    }

    private void addInstanceTypesFeature(Features features) {
        Feature feature = new Feature();
        feature.setName("Instance Types");
        feature.setDescription("Add/modify/remove instance types.");
        features.getFeature().add(feature);
    }

    private void addNumaNodesFeature(Features features) {
        Feature feature = new Feature();
        feature.setName("NumaNodes");
        feature.setDescription("Add/modify/remove numanodes.");
        features.getFeature().add(feature);
    }

    private void addMacPoolsFeature(Features features) {
        Feature feature = new Feature();
        feature.setName("Mac Pools");
        feature.setDescription("Configuring MAC address pools for data centers");
        features.getFeature().add(feature);
    }

    private void addIscsiBondFeature(Features features) {
        Feature feature = new Feature();
        feature.setName("Manage iSCSI Bonds");
        feature.setDescription("Add/modify/remove iSCSI Bonds.");
        features.getFeature().add(feature);
    }
}
