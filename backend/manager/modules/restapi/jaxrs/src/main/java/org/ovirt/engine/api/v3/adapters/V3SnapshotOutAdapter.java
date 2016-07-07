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

import org.ovirt.engine.api.model.Disk;
import org.ovirt.engine.api.model.DiskAttachment;
import org.ovirt.engine.api.model.Snapshot;
import org.ovirt.engine.api.v3.V3Adapter;
import org.ovirt.engine.api.v3.types.V3CdRoms;
import org.ovirt.engine.api.v3.types.V3CustomProperties;
import org.ovirt.engine.api.v3.types.V3Disk;
import org.ovirt.engine.api.v3.types.V3Disks;
import org.ovirt.engine.api.v3.types.V3Floppies;
import org.ovirt.engine.api.v3.types.V3KatelloErrata;
import org.ovirt.engine.api.v3.types.V3Nics;
import org.ovirt.engine.api.v3.types.V3Payloads;
import org.ovirt.engine.api.v3.types.V3Permissions;
import org.ovirt.engine.api.v3.types.V3ReportedDevices;
import org.ovirt.engine.api.v3.types.V3Snapshot;
import org.ovirt.engine.api.v3.types.V3Snapshots;
import org.ovirt.engine.api.v3.types.V3Statistics;
import org.ovirt.engine.api.v3.types.V3Status;
import org.ovirt.engine.api.v3.types.V3Tags;
import org.ovirt.engine.api.v3.types.V3WatchDogs;

public class V3SnapshotOutAdapter implements V3Adapter<Snapshot, V3Snapshot> {
    @Override
    public V3Snapshot adapt(Snapshot from) {
        V3Snapshot to = new V3Snapshot();
        if (from.isSetLinks()) {
            to.getLinks().addAll(adaptOut(from.getLinks()));
        }
        if (from.isSetActions()) {
            to.setActions(adaptOut(from.getActions()));
        }
        if (from.isSetBios()) {
            to.setBios(adaptOut(from.getBios()));
        }
        if (from.isSetCdroms()) {
            to.setCdroms(new V3CdRoms());
            to.getCdroms().getCdRoms().addAll(adaptOut(from.getCdroms().getCdroms()));
        }
        if (from.isSetCluster()) {
            to.setCluster(adaptOut(from.getCluster()));
        }
        if (from.isSetComment()) {
            to.setComment(from.getComment());
        }
        if (from.isSetConsole()) {
            to.setConsole(adaptOut(from.getConsole()));
        }
        if (from.isSetCpu()) {
            to.setCpu(adaptOut(from.getCpu()));
        }
        if (from.isSetCpuProfile()) {
            to.setCpuProfile(adaptOut(from.getCpuProfile()));
        }
        if (from.isSetCpuShares()) {
            to.setCpuShares(from.getCpuShares());
        }
        if (from.isSetCreationTime()) {
            to.setCreationTime(from.getCreationTime());
        }
        if (from.isSetCustomCpuModel()) {
            to.setCustomCpuModel(from.getCustomCpuModel());
        }
        if (from.isSetCustomEmulatedMachine()) {
            to.setCustomEmulatedMachine(from.getCustomEmulatedMachine());
        }
        if (from.isSetCustomProperties()) {
            to.setCustomProperties(new V3CustomProperties());
            to.getCustomProperties().getCustomProperty().addAll(adaptOut(from.getCustomProperties().getCustomProperties()));
        }
        if (from.isSetDate()) {
            to.setDate(from.getDate());
        }
        if (from.isSetDeleteProtected()) {
            to.setDeleteProtected(from.isDeleteProtected());
        }
        if (from.isSetDescription()) {
            to.setDescription(from.getDescription());
        }
        if (from.isSetDiskAttachments()) {
            V3Disks toDisks = new V3Disks();
            for (DiskAttachment fromDiskAttachment : from .getDiskAttachments().getDiskAttachments()) {
                Disk fromDisk = fromDiskAttachment.getDisk();
                if (fromDisk != null) {
                    V3Disk toDisk = adaptOut(fromDisk);
                    toDisks.getDisks().add(toDisk);
                }
            }
            to.setDisks(toDisks);
        }
        if (from.isSetDisplay()) {
            to.setDisplay(adaptOut(from.getDisplay()));
        }
        if (from.isSetDomain()) {
            to.setDomain(adaptOut(from.getDomain()));
        }
        if (from.isSetExternalHostProvider()) {
            to.setExternalHostProvider(adaptOut(from.getExternalHostProvider()));
        }
        if (from.isSetFloppies()) {
            to.setFloppies(new V3Floppies());
            to.getFloppies().getFloppies().addAll(adaptOut(from.getFloppies().getFloppies()));
        }
        if (from.isSetGuestOperatingSystem()) {
            to.setGuestOperatingSystem(adaptOut(from.getGuestOperatingSystem()));
        }
        if (from.isSetGuestTimeZone()) {
            to.setGuestTimeZone(adaptOut(from.getGuestTimeZone()));
        }
        if (from.isSetHighAvailability()) {
            to.setHighAvailability(adaptOut(from.getHighAvailability()));
        }
        if (from.isSetHost()) {
            to.setHost(adaptOut(from.getHost()));
        }
        if (from.isSetId()) {
            to.setId(from.getId());
        }
        if (from.isSetHref()) {
            to.setHref(from.getHref());
        }
        if (from.isSetInitialization()) {
            to.setInitialization(adaptOut(from.getInitialization()));
        }
        if (from.isSetInstanceType()) {
            to.setInstanceType(adaptOut(from.getInstanceType()));
        }
        if (from.isSetIo()) {
            to.setIo(adaptOut(from.getIo()));
        }
        if (from.isSetKatelloErrata()) {
            to.setKatelloErrata(new V3KatelloErrata());
            to.getKatelloErrata().getKatelloErrata().addAll(adaptOut(from.getKatelloErrata().getKatelloErrata()));
        }
        if (from.isSetLargeIcon()) {
            to.setLargeIcon(adaptOut(from.getLargeIcon()));
        }
        if (from.isSetMemory()) {
            to.setMemory(from.getMemory());
        }
        if (from.isSetMemoryPolicy()) {
            to.setMemoryPolicy(adaptOut(from.getMemoryPolicy()));
        }
        if (from.isSetMigration()) {
            to.setMigration(adaptOut(from.getMigration()));
        }
        if (from.isSetMigrationDowntime()) {
            to.setMigrationDowntime(from.getMigrationDowntime());
        }
        if (from.isSetName()) {
            to.setName(from.getName());
        }
        if (from.isSetNextRunConfigurationExists()) {
            to.setNextRunConfigurationExists(from.isNextRunConfigurationExists());
        }
        if (from.isSetNics()) {
            to.setNics(new V3Nics());
            to.getNics().getNics().addAll(adaptOut(from.getNics().getNics()));
        }
        if (from.isSetNumaTuneMode()) {
            to.setNumaTuneMode(from.getNumaTuneMode().value());
        }
        if (from.isSetOrigin()) {
            to.setOrigin(from.getOrigin());
        }
        if (from.isSetOs()) {
            to.setOs(adaptOut(from.getOs()));
        }
        if (from.isSetPayloads()) {
            to.setPayloads(new V3Payloads());
            to.getPayloads().getPayload().addAll(adaptOut(from.getPayloads().getPayloads()));
        }
        if (from.isSetPermissions()) {
            to.setPermissions(new V3Permissions());
            to.getPermissions().getPermissions().addAll(adaptOut(from.getPermissions().getPermissions()));
        }
        if (from.isSetPersistMemorystate()) {
            to.setPersistMemorystate(from.isPersistMemorystate());
        }
        if (from.isSetPlacementPolicy()) {
            to.setPlacementPolicy(adaptOut(from.getPlacementPolicy()));
        }
        if (from.isSetQuota()) {
            to.setQuota(adaptOut(from.getQuota()));
        }
        if (from.isSetReportedDevices()) {
            to.setReportedDevices(new V3ReportedDevices());
            to.getReportedDevices().getReportedDevices().addAll(adaptOut(from.getReportedDevices().getReportedDevices()));
        }
        if (from.isSetRngDevice()) {
            to.setRngDevice(adaptOut(from.getRngDevice()));
        }
        if (from.isSetRunOnce()) {
            to.setRunOnce(from.isRunOnce());
        }
        if (from.isSetSerialNumber()) {
            to.setSerialNumber(adaptOut(from.getSerialNumber()));
        }
        if (from.isSetSmallIcon()) {
            to.setSmallIcon(adaptOut(from.getSmallIcon()));
        }
        if (from.isSetSnapshotStatus()) {
            to.setSnapshotStatus(from.getSnapshotStatus().value());
        }
        if (from.isSetSnapshots()) {
            to.setSnapshots(new V3Snapshots());
            to.getSnapshots().getSnapshots().addAll(adaptOut(from.getSnapshots().getSnapshots()));
        }
        if (from.isSetSoundcardEnabled()) {
            to.setSoundcardEnabled(from.isSoundcardEnabled());
        }
        if (from.isSetSso()) {
            to.setSso(adaptOut(from.getSso()));
        }
        if (from.isSetStartPaused()) {
            to.setStartPaused(from.isStartPaused());
        }
        if (from.isSetStartTime()) {
            to.setStartTime(from.getStartTime());
        }
        if (from.isSetStateless()) {
            to.setStateless(from.isStateless());
        }
        if (from.isSetStatistics()) {
            to.setStatistics(new V3Statistics());
            to.getStatistics().getStatistics().addAll(adaptOut(from.getStatistics().getStatistics()));
        }
        if (from.isSetStatus()) {
            V3Status status = new V3Status();
            status.setState(from.getStatus().value());
            to.setStatus(status);
        }
        if (from.isSetStopReason()) {
            to.setStopReason(from.getStopReason());
        }
        if (from.isSetStopTime()) {
            to.setStopTime(from.getStopTime());
        }
        if (from.isSetStorageDomain()) {
            to.setStorageDomain(adaptOut(from.getStorageDomain()));
        }
        if (from.isSetTags()) {
            to.setTags(new V3Tags());
            to.getTags().getTags().addAll(adaptOut(from.getTags().getTags()));
        }
        if (from.isSetTemplate()) {
            to.setTemplate(adaptOut(from.getTemplate()));
        }
        if (from.isSetTimeZone()) {
            to.setTimeZone(adaptOut(from.getTimeZone()));
        }
        if (from.isSetTunnelMigration()) {
            to.setTunnelMigration(from.isTunnelMigration());
        }
        if (from.isSetSnapshotType()) {
            to.setType(from.getSnapshotType().value());
        }
        if (from.isSetUsb()) {
            to.setUsb(adaptOut(from.getUsb()));
        }
        if (from.isSetUseLatestTemplateVersion()) {
            to.setUseLatestTemplateVersion(from.isUseLatestTemplateVersion());
        }
        if (from.isSetVirtioScsi()) {
            to.setVirtioScsi(adaptOut(from.getVirtioScsi()));
        }
        if (from.isSetVm()) {
            to.setVm(adaptOut(from.getVm()));
        }
        if (from.isSetVmPool()) {
            to.setVmPool(adaptOut(from.getVmPool()));
        }
        if (from.isSetWatchdogs()) {
            to.setWatchdogs(new V3WatchDogs());
            to.getWatchdogs().getWatchDogs().addAll(adaptOut(from.getWatchdogs().getWatchdogs()));
        }
        return to;
    }
}
