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

import org.ovirt.engine.api.model.DiskAttachment;
import org.ovirt.engine.api.model.DiskAttachments;
import org.ovirt.engine.api.model.NumaTuneMode;
import org.ovirt.engine.api.model.TimeZone;
import org.ovirt.engine.api.model.Vm;
import org.ovirt.engine.api.model.VmStatus;
import org.ovirt.engine.api.model.VmType;
import org.ovirt.engine.api.v3.V3Adapter;
import org.ovirt.engine.api.v3.types.V3Disk;
import org.ovirt.engine.api.v3.types.V3Status;
import org.ovirt.engine.api.v3.types.V3VM;

public class V3VMInAdapter implements V3Adapter<V3VM, Vm> {
    @Override
    public Vm adapt(V3VM from) {
        Vm to = new Vm();
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
            to.setCdroms(adaptIn(from.getCdroms()));
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
            to.setCustomProperties(adaptIn(from.getCustomProperties()));
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
            to.setFloppies(adaptIn(from.getFloppies()));
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
            to.setKatelloErrata(adaptIn(from.getKatelloErrata()));
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
            to.setNics(adaptIn(from.getNics()));
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
            to.setPayloads(adaptIn(from.getPayloads()));
        }
        if (from.isSetPermissions()) {
            to.setPermissions(adaptIn(from.getPermissions()));
        }
        if (from.isSetPlacementPolicy()) {
            to.setPlacementPolicy(adaptIn(from.getPlacementPolicy()));
        }
        if (from.isSetQuota()) {
            to.setQuota(adaptIn(from.getQuota()));
        }
        if (from.isSetReportedDevices()) {
            to.setReportedDevices(adaptIn(from.getReportedDevices()));
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
        if (from.isSetSnapshots()) {
            to.setSnapshots(adaptIn(from.getSnapshots()));
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
            to.setStatistics(adaptIn(from.getStatistics()));
        }
        V3Status status = from.getStatus();
        if (status != null) {
            if (status.isSetState()) {
                to.setStatus(VmStatus.fromValue(status.getState()));
            }
            if (status.isSetDetail()) {
                to.setStatusDetail(status.getDetail());
            }
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
            to.setTags(adaptIn(from.getTags()));
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
            to.setType(VmType.fromValue(from.getType()));
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
        if (from.isSetVmPool()) {
            to.setVmPool(adaptIn(from.getVmPool()));
        }
        if (from.isSetWatchdogs()) {
            to.setWatchdogs(adaptIn(from.getWatchdogs()));
        }

        // V3 of the API supports a "timezone" element containing a single string, but V4 has replaced that with a
        // new structured "time_zone" element containing the name of the time zone and the UTC offset:
        if (from.isSetTimezone() && !to.isSetTimeZone()) {
            TimeZone timeZone = new TimeZone();
            timeZone.setName(from.getTimezone());
            to.setTimeZone(timeZone);
        }

        return to;
    }
}
