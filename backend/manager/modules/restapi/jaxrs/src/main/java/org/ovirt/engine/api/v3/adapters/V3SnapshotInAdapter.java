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

import org.ovirt.engine.api.model.Cdroms;
import org.ovirt.engine.api.model.CustomProperties;
import org.ovirt.engine.api.model.DiskAttachment;
import org.ovirt.engine.api.model.DiskAttachments;
import org.ovirt.engine.api.model.Floppies;
import org.ovirt.engine.api.model.KatelloErrata;
import org.ovirt.engine.api.model.Nics;
import org.ovirt.engine.api.model.NumaTuneMode;
import org.ovirt.engine.api.model.Payloads;
import org.ovirt.engine.api.model.Permissions;
import org.ovirt.engine.api.model.ReportedDevices;
import org.ovirt.engine.api.model.Snapshot;
import org.ovirt.engine.api.model.SnapshotStatus;
import org.ovirt.engine.api.model.SnapshotType;
import org.ovirt.engine.api.model.Snapshots;
import org.ovirt.engine.api.model.Statistics;
import org.ovirt.engine.api.model.Tags;
import org.ovirt.engine.api.model.VmStatus;
import org.ovirt.engine.api.model.Watchdogs;
import org.ovirt.engine.api.v3.V3Adapter;
import org.ovirt.engine.api.v3.types.V3Disk;
import org.ovirt.engine.api.v3.types.V3Snapshot;

public class V3SnapshotInAdapter implements V3Adapter<V3Snapshot, Snapshot> {
    @Override
    public Snapshot adapt(V3Snapshot from) {
        Snapshot to = new Snapshot();
        if (from.isSetLinks()) {
            to.getLinks().addAll(adaptIn(from.getLinks()));
        }
        if (from.isSetActions()) {
            to.setActions(adaptIn(from.getActions()));
        }
        if (from.isSetBios()) {
            to.setBios(adaptIn(from.getBios()));
        }
        if (from.isSetCdroms()) {
            to.setCdroms(new Cdroms());
            to.getCdroms().getCdroms().addAll(adaptIn(from.getCdroms().getCdRoms()));
        }
        if (from.isSetCluster()) {
            to.setCluster(adaptIn(from.getCluster()));
        }
        if (from.isSetComment()) {
            to.setComment(from.getComment());
        }
        if (from.isSetConsole()) {
            to.setConsole(adaptIn(from.getConsole()));
        }
        if (from.isSetCpu()) {
            to.setCpu(adaptIn(from.getCpu()));
        }
        if (from.isSetCpuProfile()) {
            to.setCpuProfile(adaptIn(from.getCpuProfile()));
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
            to.setCustomProperties(new CustomProperties());
            to.getCustomProperties().getCustomProperties().addAll(adaptIn(from.getCustomProperties().getCustomProperty()));
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
        if (from.isSetDisks()) {
            DiskAttachments toAttachments = new DiskAttachments();
            for (V3Disk fromDisk : from.getDisks().getDisks()) {
                DiskAttachment toAttachment = new DiskAttachment();
                toAttachment.setDisk(adaptIn(fromDisk));
                toAttachments.getDiskAttachments().add(toAttachment);
            }
            to.setDiskAttachments(toAttachments);
        }
        if (from.isSetDisplay()) {
            to.setDisplay(adaptIn(from.getDisplay()));
        }
        if (from.isSetDomain()) {
            to.setDomain(adaptIn(from.getDomain()));
        }
        if (from.isSetExternalHostProvider()) {
            to.setExternalHostProvider(adaptIn(from.getExternalHostProvider()));
        }
        if (from.isSetFloppies()) {
            to.setFloppies(new Floppies());
            to.getFloppies().getFloppies().addAll(adaptIn(from.getFloppies().getFloppies()));
        }
        if (from.isSetGuestOperatingSystem()) {
            to.setGuestOperatingSystem(adaptIn(from.getGuestOperatingSystem()));
        }
        if (from.isSetGuestTimeZone()) {
            to.setGuestTimeZone(adaptIn(from.getGuestTimeZone()));
        }
        if (from.isSetHighAvailability()) {
            to.setHighAvailability(adaptIn(from.getHighAvailability()));
        }
        if (from.isSetHost()) {
            to.setHost(adaptIn(from.getHost()));
        }
        if (from.isSetId()) {
            to.setId(from.getId());
        }
        if (from.isSetHref()) {
            to.setHref(from.getHref());
        }
        if (from.isSetInitialization()) {
            to.setInitialization(adaptIn(from.getInitialization()));
        }
        if (from.isSetInstanceType()) {
            to.setInstanceType(adaptIn(from.getInstanceType()));
        }
        if (from.isSetIo()) {
            to.setIo(adaptIn(from.getIo()));
        }
        if (from.isSetKatelloErrata()) {
            to.setKatelloErrata(new KatelloErrata());
            to.getKatelloErrata().getKatelloErrata().addAll(adaptIn(from.getKatelloErrata().getKatelloErrata()));
        }
        if (from.isSetLargeIcon()) {
            to.setLargeIcon(adaptIn(from.getLargeIcon()));
        }
        if (from.isSetMemory()) {
            to.setMemory(from.getMemory());
        }
        if (from.isSetMemoryPolicy()) {
            to.setMemoryPolicy(adaptIn(from.getMemoryPolicy()));
        }
        if (from.isSetMigration()) {
            to.setMigration(adaptIn(from.getMigration()));
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
            to.setNics(new Nics());
            to.getNics().getNics().addAll(adaptIn(from.getNics().getNics()));
        }
        if (from.isSetNumaTuneMode()) {
            to.setNumaTuneMode(NumaTuneMode.fromValue(from.getNumaTuneMode()));
        }
        if (from.isSetOrigin()) {
            to.setOrigin(from.getOrigin());
        }
        if (from.isSetOs()) {
            to.setOs(adaptIn(from.getOs()));
        }
        if (from.isSetPayloads()) {
            to.setPayloads(new Payloads());
            to.getPayloads().getPayloads().addAll(adaptIn(from.getPayloads().getPayload()));
        }
        if (from.isSetPermissions()) {
            to.setPermissions(new Permissions());
            to.getPermissions().getPermissions().addAll(adaptIn(from.getPermissions().getPermissions()));
        }
        if (from.isSetPersistMemorystate()) {
            to.setPersistMemorystate(from.isPersistMemorystate());
        }
        if (from.isSetPlacementPolicy()) {
            to.setPlacementPolicy(adaptIn(from.getPlacementPolicy()));
        }
        if (from.isSetQuota()) {
            to.setQuota(adaptIn(from.getQuota()));
        }
        if (from.isSetReportedDevices()) {
            to.setReportedDevices(new ReportedDevices());
            to.getReportedDevices().getReportedDevices().addAll(adaptIn(from.getReportedDevices().getReportedDevices()));
        }
        if (from.isSetRngDevice()) {
            to.setRngDevice(adaptIn(from.getRngDevice()));
        }
        if (from.isSetRunOnce()) {
            to.setRunOnce(from.isRunOnce());
        }
        if (from.isSetSerialNumber()) {
            to.setSerialNumber(adaptIn(from.getSerialNumber()));
        }
        if (from.isSetSmallIcon()) {
            to.setSmallIcon(adaptIn(from.getSmallIcon()));
        }
        if (from.isSetSnapshotStatus()) {
            to.setSnapshotStatus(SnapshotStatus.fromValue(from.getSnapshotStatus()));
        }
        if (from.isSetSnapshots()) {
            to.setSnapshots(new Snapshots());
            to.getSnapshots().getSnapshots().addAll(adaptIn(from.getSnapshots().getSnapshots()));
        }
        if (from.isSetSoundcardEnabled()) {
            to.setSoundcardEnabled(from.isSoundcardEnabled());
        }
        if (from.isSetSso()) {
            to.setSso(adaptIn(from.getSso()));
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
            to.setStatistics(new Statistics());
            to.getStatistics().getStatistics().addAll(adaptIn(from.getStatistics().getStatistics()));
        }
        if (from.isSetStatus() && from.getStatus().isSetState()) {
            to.setStatus(VmStatus.fromValue(from.getStatus().getState()));
        }
        if (from.isSetStopReason()) {
            to.setStopReason(from.getStopReason());
        }
        if (from.isSetStopTime()) {
            to.setStopTime(from.getStopTime());
        }
        if (from.isSetStorageDomain()) {
            to.setStorageDomain(adaptIn(from.getStorageDomain()));
        }
        if (from.isSetTags()) {
            to.setTags(new Tags());
            to.getTags().getTags().addAll(adaptIn(from.getTags().getTags()));
        }
        if (from.isSetTemplate()) {
            to.setTemplate(adaptIn(from.getTemplate()));
        }
        if (from.isSetTimeZone()) {
            to.setTimeZone(adaptIn(from.getTimeZone()));
        }
        if (from.isSetTunnelMigration()) {
            to.setTunnelMigration(from.isTunnelMigration());
        }
        if (from.isSetType()) {
            to.setSnapshotType(SnapshotType.fromValue(from.getType()));
        }
        if (from.isSetUsb()) {
            to.setUsb(adaptIn(from.getUsb()));
        }
        if (from.isSetUseLatestTemplateVersion()) {
            to.setUseLatestTemplateVersion(from.isUseLatestTemplateVersion());
        }
        if (from.isSetVirtioScsi()) {
            to.setVirtioScsi(adaptIn(from.getVirtioScsi()));
        }
        if (from.isSetVm()) {
            to.setVm(adaptIn(from.getVm()));
        }
        if (from.isSetVmPool()) {
            to.setVmPool(adaptIn(from.getVmPool()));
        }
        if (from.isSetWatchdogs()) {
            to.setWatchdogs(new Watchdogs());
            to.getWatchdogs().getWatchdogs().addAll(adaptIn(from.getWatchdogs().getWatchDogs()));
        }
        return to;
    }
}
