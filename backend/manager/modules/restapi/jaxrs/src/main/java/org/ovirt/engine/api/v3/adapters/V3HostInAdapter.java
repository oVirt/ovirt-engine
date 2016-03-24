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

import static org.ovirt.engine.api.v3.adapters.V3InAdapters.adaptIn;

import org.ovirt.engine.api.model.AutoNumaStatus;
import org.ovirt.engine.api.model.Hooks;
import org.ovirt.engine.api.model.Host;
import org.ovirt.engine.api.model.HostProtocol;
import org.ovirt.engine.api.model.KatelloErrata;
import org.ovirt.engine.api.model.KdumpStatus;
import org.ovirt.engine.api.model.Statistics;
import org.ovirt.engine.api.model.StorageConnectionExtensions;
import org.ovirt.engine.api.v3.V3Adapter;
import org.ovirt.engine.api.v3.types.V3Host;

public class V3HostInAdapter implements V3Adapter<V3Host, Host> {
    @Override
    public Host adapt(V3Host from) {
        Host to = new Host();
        if (from.isSetLinks()) {
            to.getLinks().addAll(adaptIn(from.getLinks()));
        }
        if (from.isSetActions()) {
            to.setActions(adaptIn(from.getActions()));
        }
        if (from.isSetAddress()) {
            to.setAddress(from.getAddress());
        }
        if (from.isSetAutoNumaStatus()) {
            to.setAutoNumaStatus(AutoNumaStatus.fromValue(from.getAutoNumaStatus()));
        }
        if (from.isSetCertificate()) {
            to.setCertificate(adaptIn(from.getCertificate()));
        }
        if (from.isSetCluster()) {
            to.setCluster(adaptIn(from.getCluster()));
        }
        if (from.isSetComment()) {
            to.setComment(from.getComment());
        }
        if (from.isSetCpu()) {
            to.setCpu(adaptIn(from.getCpu()));
        }
        if (from.isSetDescription()) {
            to.setDescription(from.getDescription());
        }
        if (from.isSetDevicePassthrough()) {
            to.setDevicePassthrough(adaptIn(from.getDevicePassthrough()));
        }
        if (from.isSetDisplay()) {
            to.setDisplay(adaptIn(from.getDisplay()));
        }
        if (from.isSetExternalHostProvider()) {
            to.setExternalHostProvider(adaptIn(from.getExternalHostProvider()));
        }
        if (from.isSetExternalStatus()) {
            to.setExternalStatus(adaptIn(from.getExternalStatus()));
        }
        if (from.isSetHardwareInformation()) {
            to.setHardwareInformation(adaptIn(from.getHardwareInformation()));
        }
        if (from.isSetHooks()) {
            to.setHooks(new Hooks());
            to.getHooks().getHooks().addAll(adaptIn(from.getHooks().getHooks()));
        }
        if (from.isSetHostedEngine()) {
            to.setHostedEngine(adaptIn(from.getHostedEngine()));
        }
        if (from.isSetId()) {
            to.setId(from.getId());
        }
        if (from.isSetHref()) {
            to.setHref(from.getHref());
        }
        if (from.isSetIscsi()) {
            to.setIscsi(adaptIn(from.getIscsi()));
        }
        if (from.isSetKatelloErrata()) {
            to.setKatelloErrata(new KatelloErrata());
            to.getKatelloErrata().getKatelloErrata().addAll(adaptIn(from.getKatelloErrata().getKatelloErrata()));
        }
        if (from.isSetKdumpStatus()) {
            to.setKdumpStatus(KdumpStatus.fromValue(from.getKdumpStatus()));
        }
        if (from.isSetKsm()) {
            to.setKsm(adaptIn(from.getKsm()));
        }
        if (from.isSetLibvirtVersion()) {
            to.setLibvirtVersion(adaptIn(from.getLibvirtVersion()));
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
            to.setOs(adaptIn(from.getOs()));
        }
        if (from.isSetOverrideIptables()) {
            to.setOverrideIptables(from.isOverrideIptables());
        }
        if (from.isSetPort()) {
            to.setPort(from.getPort());
        }
        if (from.isSetPowerManagement()) {
            to.setPowerManagement(adaptIn(from.getPowerManagement()));
        }
        if (from.isSetProtocol()) {
            to.setProtocol(HostProtocol.fromValue(from.getProtocol()));
        }
        if (from.isSetRootPassword()) {
            to.setRootPassword(from.getRootPassword());
        }
        if (from.isSetSelinux()) {
            to.setSelinux(adaptIn(from.getSelinux()));
        }
        if (from.isSetSpm()) {
            to.setSpm(adaptIn(from.getSpm()));
        }
        if (from.isSetSsh()) {
            to.setSsh(adaptIn(from.getSsh()));
        }
        if (from.isSetStatistics()) {
            to.setStatistics(new Statistics());
            to.getStatistics().getStatistics().addAll(adaptIn(from.getStatistics().getStatistics()));
        }
        if (from.isSetStatus()) {
            to.setStatus(adaptIn(from.getStatus()));
        }
        if (from.isSetStorageConnectionExtensions()) {
            to.setStorageConnectionExtensions(new StorageConnectionExtensions());
            to.getStorageConnectionExtensions().getStorageConnectionExtensions().addAll(adaptIn(from.getStorageConnectionExtensions().getStorageConnectionExtension()));
        }
        if (from.isSetSummary()) {
            to.setSummary(adaptIn(from.getSummary()));
        }
        if (from.isSetTransparentHugepages()) {
            to.setTransparentHugepages(adaptIn(from.getTransparentHugepages()));
        }
        if (from.isSetType()) {
            to.setType(from.getType());
        }
        if (from.isSetUpdateAvailable()) {
            to.setUpdateAvailable(from.isUpdateAvailable());
        }
        if (from.isSetVersion()) {
            to.setVersion(adaptIn(from.getVersion()));
        }
        return to;
    }
}
