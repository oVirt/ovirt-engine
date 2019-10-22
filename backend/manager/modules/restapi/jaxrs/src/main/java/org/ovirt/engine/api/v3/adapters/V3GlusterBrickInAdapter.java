/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.v3.adapters;

import static org.ovirt.engine.api.v3.adapters.V3InAdapters.adaptIn;

import org.ovirt.engine.api.model.GlusterBrick;
import org.ovirt.engine.api.model.GlusterBrickStatus;
import org.ovirt.engine.api.model.GlusterClients;
import org.ovirt.engine.api.model.GlusterMemoryPools;
import org.ovirt.engine.api.v3.V3Adapter;
import org.ovirt.engine.api.v3.types.V3GlusterBrick;

public class V3GlusterBrickInAdapter implements V3Adapter<V3GlusterBrick, GlusterBrick> {
    @Override
    public GlusterBrick adapt(V3GlusterBrick from) {
        GlusterBrick to = new GlusterBrick();
        if (from.isSetLinks()) {
            to.getLinks().addAll(adaptIn(from.getLinks()));
        }
        if (from.isSetActions()) {
            to.setActions(adaptIn(from.getActions()));
        }
        if (from.isSetBrickDir()) {
            to.setBrickDir(from.getBrickDir());
        }
        if (from.isSetComment()) {
            to.setComment(from.getComment());
        }
        if (from.isSetDescription()) {
            to.setDescription(from.getDescription());
        }
        if (from.isSetDevice()) {
            to.setDevice(from.getDevice());
        }
        if (from.isSetFsName()) {
            to.setFsName(from.getFsName());
        }
        if (from.isSetGlusterClients()) {
            to.setGlusterClients(new GlusterClients());
            to.getGlusterClients().getGlusterClients().addAll(adaptIn(from.getGlusterClients().getGlusterClients()));
        }
        if (from.isSetGlusterVolume()) {
            to.setGlusterVolume(adaptIn(from.getGlusterVolume()));
        }
        if (from.isSetId()) {
            to.setId(from.getId());
        }
        if (from.isSetHref()) {
            to.setHref(from.getHref());
        }
        if (from.isSetMemoryPools()) {
            to.setMemoryPools(new GlusterMemoryPools());
            to.getMemoryPools().getGlusterMemoryPools().addAll(adaptIn(from.getMemoryPools().getGlusterMemoryPools()));
        }
        if (from.isSetMntOptions()) {
            to.setMntOptions(from.getMntOptions());
        }
        if (from.isSetName()) {
            to.setName(from.getName());
        }
        if (from.isSetPid()) {
            to.setPid(from.getPid());
        }
        if (from.isSetPort()) {
            to.setPort(from.getPort());
        }
        if (from.isSetServerId()) {
            to.setServerId(from.getServerId());
        }
        if (from.isSetStatus() && from.getStatus().isSetState()) {
            to.setStatus(GlusterBrickStatus.fromValue(from.getStatus().getState()));
        }
        return to;
    }
}
