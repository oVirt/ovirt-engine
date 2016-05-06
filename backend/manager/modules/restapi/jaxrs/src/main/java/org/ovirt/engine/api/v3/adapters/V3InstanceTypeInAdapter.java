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

import org.ovirt.engine.api.model.CustomProperties;
import org.ovirt.engine.api.model.InstanceType;
import org.ovirt.engine.api.model.TemplateStatus;
import org.ovirt.engine.api.model.TimeZone;
import org.ovirt.engine.api.model.VmType;
import org.ovirt.engine.api.v3.V3Adapter;
import org.ovirt.engine.api.v3.types.V3InstanceType;

public class V3InstanceTypeInAdapter implements V3Adapter<V3InstanceType, InstanceType> {
    @Override
    public InstanceType adapt(V3InstanceType from) {
        InstanceType to = new InstanceType();
        if (from.isSetLinks()) {
            to.getLinks().addAll(adaptIn(from.getLinks()));
        }
        if (from.isSetActions()) {
            to.setActions(adaptIn(from.getActions()));
        }
        if (from.isSetBios()) {
            to.setBios(adaptIn(from.getBios()));
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
        if (from.isSetDeleteProtected()) {
            to.setDeleteProtected(from.isDeleteProtected());
        }
        if (from.isSetDescription()) {
            to.setDescription(from.getDescription());
        }
        if (from.isSetDisplay()) {
            to.setDisplay(adaptIn(from.getDisplay()));
        }
        if (from.isSetDomain()) {
            to.setDomain(adaptIn(from.getDomain()));
        }
        if (from.isSetHighAvailability()) {
            to.setHighAvailability(adaptIn(from.getHighAvailability()));
        }
        if (from.isSetId()) {
            to.setId(from.getId());
        }
        if (from.isSetHref()) {
            to.setHref(from.getHref());
        }
        if (from.isSetIo()) {
            to.setIo(adaptIn(from.getIo()));
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
        if (from.isSetOrigin()) {
            to.setOrigin(from.getOrigin());
        }
        if (from.isSetOs()) {
            to.setOs(adaptIn(from.getOs()));
        }
        if (from.isSetRngDevice()) {
            to.setRngDevice(adaptIn(from.getRngDevice()));
        }
        if (from.isSetSerialNumber()) {
            to.setSerialNumber(adaptIn(from.getSerialNumber()));
        }
        if (from.isSetSmallIcon()) {
            to.setSmallIcon(adaptIn(from.getSmallIcon()));
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
        if (from.isSetStateless()) {
            to.setStateless(from.isStateless());
        }
        if (from.isSetStatus() && from.getStatus().isSetState()) {
            to.setStatus(TemplateStatus.fromValue(from.getStatus().getState()));
        }
        if (from.isSetStorageDomain()) {
            to.setStorageDomain(adaptIn(from.getStorageDomain()));
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
        if (from.isSetVersion()) {
            to.setVersion(adaptIn(from.getVersion()));
        }
        if (from.isSetVm()) {
            to.setVm(adaptIn(from.getVm()));
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
