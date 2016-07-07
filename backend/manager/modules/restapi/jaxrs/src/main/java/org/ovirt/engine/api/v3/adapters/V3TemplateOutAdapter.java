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

import static java.util.stream.Collectors.toList;
import static org.ovirt.engine.api.v3.adapters.V3OutAdapters.adaptOut;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.ovirt.engine.api.model.Link;
import org.ovirt.engine.api.model.Template;
import org.ovirt.engine.api.model.TimeZone;
import org.ovirt.engine.api.v3.V3Adapter;
import org.ovirt.engine.api.v3.types.V3CustomProperties;
import org.ovirt.engine.api.v3.types.V3Status;
import org.ovirt.engine.api.v3.types.V3Template;

public class V3TemplateOutAdapter implements V3Adapter<Template, V3Template> {
    // The list of "rels" that should be removed from the set of links created by version 4 of the API, as they are
    // new and shouldn't appear in version 3 of the API:
    private static final Set<String> RELS_TO_REMOVE = new HashSet<>();

    static {
        RELS_TO_REMOVE.add("diskattachments");
    }

    @Override
    public V3Template adapt(Template from) {
        V3Template to = new V3Template();

        // Remove the links for "rels" that are new in version 4 of the API:
        if (from.isSetLinks()) {
            List<Link> links = from.getLinks().stream()
                .filter(link -> !RELS_TO_REMOVE.contains(link.getRel()))
                .collect(toList());
            to.getLinks().addAll(adaptOut(links));
        }

        if (from.isSetActions()) {
            to.setActions(adaptOut(from.getActions()));
        }
        if (from.isSetBios()) {
            to.setBios(adaptOut(from.getBios()));
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
        if (from.isSetDeleteProtected()) {
            to.setDeleteProtected(from.isDeleteProtected());
        }
        if (from.isSetDescription()) {
            to.setDescription(from.getDescription());
        }
        if (from.isSetDisplay()) {
            to.setDisplay(adaptOut(from.getDisplay()));
        }
        if (from.isSetDomain()) {
            to.setDomain(adaptOut(from.getDomain()));
        }
        if (from.isSetHighAvailability()) {
            to.setHighAvailability(adaptOut(from.getHighAvailability()));
        }
        if (from.isSetId()) {
            to.setId(from.getId());
        }
        if (from.isSetHref()) {
            to.setHref(from.getHref());
        }
        if (from.isSetIo()) {
            to.setIo(adaptOut(from.getIo()));
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
        if (from.isSetOrigin()) {
            to.setOrigin(from.getOrigin());
        }
        if (from.isSetOs()) {
            to.setOs(adaptOut(from.getOs()));
        }
        if (from.isSetRngDevice()) {
            to.setRngDevice(adaptOut(from.getRngDevice()));
        }
        if (from.isSetSerialNumber()) {
            to.setSerialNumber(adaptOut(from.getSerialNumber()));
        }
        if (from.isSetSmallIcon()) {
            to.setSmallIcon(adaptOut(from.getSmallIcon()));
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
        if (from.isSetStateless()) {
            to.setStateless(from.isStateless());
        }
        if (from.isSetStatus()) {
            V3Status status = new V3Status();
            status.setState(from.getStatus().value());
            to.setStatus(status);
        }
        if (from.isSetStorageDomain()) {
            to.setStorageDomain(adaptOut(from.getStorageDomain()));
        }
        if (from.isSetTimeZone()) {
            to.setTimeZone(adaptOut(from.getTimeZone()));
        }
        if (from.isSetTunnelMigration()) {
            to.setTunnelMigration(from.isTunnelMigration());
        }
        if (from.isSetType()) {
            to.setType(from.getType().value());
        }
        if (from.isSetUsb()) {
            to.setUsb(adaptOut(from.getUsb()));
        }
        if (from.isSetVersion()) {
            to.setVersion(adaptOut(from.getVersion()));
        }
        if (from.isSetVirtioScsi()) {
            to.setVirtioScsi(adaptOut(from.getVirtioScsi()));
        }
        if (from.isSetVm()) {
            to.setVm(adaptOut(from.getVm()));
        }

        // V3 of the API supports a "timezone" element containing a single string, but V4 has replaced that with a
        // new structured "time_zone" element containing the name of the time zone and the UTC offset:
        if (from.isSetTimeZone() && !to.isSetTimezone()) {
            TimeZone timeZone = from.getTimeZone();
            if (timeZone.isSetName()) {
                to.setTimezone(timeZone.getName());
            }
        }

        return to;
    }
}
