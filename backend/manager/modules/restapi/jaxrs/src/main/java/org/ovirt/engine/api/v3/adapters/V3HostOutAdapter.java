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

package org.ovirt.engine.api.v3.adapters;

import static org.ovirt.engine.api.v3.adapters.V3OutAdapters.adaptOut;

import org.ovirt.engine.api.model.Host;
import org.ovirt.engine.api.v3.V3Adapter;
import org.ovirt.engine.api.v3.types.V3Hooks;
import org.ovirt.engine.api.v3.types.V3Host;
import org.ovirt.engine.api.v3.types.V3KatelloErrata;
import org.ovirt.engine.api.v3.types.V3Statistics;
import org.ovirt.engine.api.v3.types.V3StorageConnectionExtensions;

public class V3HostOutAdapter implements V3Adapter<Host, V3Host> {
    @Override
    public V3Host adapt(Host from) {
        V3Host to = new V3Host();
        if (from.isSetLinks()) {
            to.getLinks().addAll(adaptOut(from.getLinks()));
        }
        if (from.isSetActions()) {
            to.setActions(adaptOut(from.getActions()));
        }
        if (from.isSetAddress()) {
            to.setAddress(from.getAddress());
        }
        if (from.isSetAutoNumaStatus()) {
            to.setAutoNumaStatus(from.getAutoNumaStatus());
        }
        if (from.isSetCertificate()) {
            to.setCertificate(adaptOut(from.getCertificate()));
        }
        if (from.isSetCluster()) {
            to.setCluster(adaptOut(from.getCluster()));
        }
        if (from.isSetComment()) {
            to.setComment(from.getComment());
        }
        if (from.isSetCpu()) {
            to.setCpu(adaptOut(from.getCpu()));
        }
        if (from.isSetDescription()) {
            to.setDescription(from.getDescription());
        }
        if (from.isSetDevicePassthrough()) {
            to.setDevicePassthrough(adaptOut(from.getDevicePassthrough()));
        }
        if (from.isSetDisplay()) {
            to.setDisplay(adaptOut(from.getDisplay()));
        }
        if (from.isSetExternalHostProvider()) {
            to.setExternalHostProvider(adaptOut(from.getExternalHostProvider()));
        }
        if (from.isSetExternalStatus()) {
            to.setExternalStatus(adaptOut(from.getExternalStatus()));
        }
        if (from.isSetHardwareInformation()) {
            to.setHardwareInformation(adaptOut(from.getHardwareInformation()));
        }
        if (from.isSetHooks()) {
            to.setHooks(new V3Hooks());
            to.getHooks().getHooks().addAll(adaptOut(from.getHooks().getHooks()));
        }
        if (from.isSetHostedEngine()) {
            to.setHostedEngine(adaptOut(from.getHostedEngine()));
        }
        if (from.isSetId()) {
            to.setId(from.getId());
        }
        if (from.isSetHref()) {
            to.setHref(from.getHref());
        }
        if (from.isSetIscsi()) {
            to.setIscsi(adaptOut(from.getIscsi()));
        }
        if (from.isSetKatelloErrata()) {
            to.setKatelloErrata(new V3KatelloErrata());
            to.getKatelloErrata().getKatelloErrata().addAll(adaptOut(from.getKatelloErrata().getKatelloErrata()));
        }
        if (from.isSetKdumpStatus()) {
            to.setKdumpStatus(from.getKdumpStatus());
        }
        if (from.isSetKsm()) {
            to.setKsm(adaptOut(from.getKsm()));
        }
        if (from.isSetLibvirtVersion()) {
            to.setLibvirtVersion(adaptOut(from.getLibvirtVersion()));
        }
        if (from.isSetLiveSnapshotSupport()) {
            to.setLiveSnapshotSupport(from.isLiveSnapshotSupport());
        }
        if (from.isSetMaxSchedulingMemory()) {
            to.setMaxSchedulingMemory(from.getMaxSchedulingMemory());
        }
        if (from.isSetMemory()) {
            to.setMemory(from.getMemory());
        }
        if (from.isSetName()) {
            to.setName(from.getName());
        }
        if (from.isSetNumaSupported()) {
            to.setNumaSupported(from.isNumaSupported());
        }
        if (from.isSetOs()) {
            to.setOs(adaptOut(from.getOs()));
        }
        if (from.isSetOverrideIptables()) {
            to.setOverrideIptables(from.isOverrideIptables());
        }
        if (from.isSetPort()) {
            to.setPort(from.getPort());
        }
        if (from.isSetPowerManagement()) {
            to.setPowerManagement(adaptOut(from.getPowerManagement()));
        }
        if (from.isSetProtocol()) {
            to.setProtocol(from.getProtocol());
        }
        if (from.isSetRootPassword()) {
            to.setRootPassword(from.getRootPassword());
        }
        if (from.isSetSelinux()) {
            to.setSelinux(adaptOut(from.getSelinux()));
        }
        if (from.isSetSpm()) {
            to.setSpm(adaptOut(from.getSpm()));
        }
        if (from.isSetSsh()) {
            to.setSsh(adaptOut(from.getSsh()));
        }
        if (from.isSetStatistics()) {
            to.setStatistics(new V3Statistics());
            to.getStatistics().getStatistics().addAll(adaptOut(from.getStatistics().getStatistics()));
        }
        if (from.isSetStatus()) {
            to.setStatus(adaptOut(from.getStatus()));
        }
        if (from.isSetStorageConnectionExtensions()) {
            to.setStorageConnectionExtensions(new V3StorageConnectionExtensions());
            to.getStorageConnectionExtensions().getStorageConnectionExtension().addAll(adaptOut(from.getStorageConnectionExtensions().getStorageConnectionExtensions()));
        }
        if (from.isSetSummary()) {
            to.setSummary(adaptOut(from.getSummary()));
        }
        if (from.isSetTransparentHugepages()) {
            to.setTransparentHugepages(adaptOut(from.getTransparentHugepages()));
        }
        if (from.isSetType()) {
            to.setType(from.getType());
        }
        if (from.isSetUpdateAvailable()) {
            to.setUpdateAvailable(from.isUpdateAvailable());
        }
        if (from.isSetVersion()) {
            to.setVersion(adaptOut(from.getVersion()));
        }
        return to;
    }
}
