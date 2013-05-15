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
            addFeatureVnicCustomProperties(features);
            addFeatureVirtioScsi(features);
        }
        return features;
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

    private void addFeatureVnicCustomProperties(Features features) {
        Feature feature = new Feature();
        feature.setName("VM NIC Custom Properties");
        feature.setDescription("Ability to add custom properties to vm nic.");
        features.getFeature().add(feature);
    }

    private void addFeatureVirtioScsi(Features features) {
        Feature feature = new Feature();
        feature.setName("Virtio-SCSI Support");
        feature.setDescription("Support for paravirtualized SCSI controller device.");
        features.getFeature().add(feature);
    }
}
