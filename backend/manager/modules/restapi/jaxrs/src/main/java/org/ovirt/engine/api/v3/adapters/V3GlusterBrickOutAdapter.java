/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.v3.adapters;

import static org.ovirt.engine.api.v3.adapters.V3OutAdapters.adaptOut;

import org.ovirt.engine.api.model.GlusterBrick;
import org.ovirt.engine.api.v3.V3Adapter;
import org.ovirt.engine.api.v3.types.V3GlusterBrick;
import org.ovirt.engine.api.v3.types.V3GlusterClients;
import org.ovirt.engine.api.v3.types.V3GlusterMemoryPools;
import org.ovirt.engine.api.v3.types.V3Status;

public class V3GlusterBrickOutAdapter implements V3Adapter<GlusterBrick, V3GlusterBrick> {
    @Override
    public V3GlusterBrick adapt(GlusterBrick from) {
        V3GlusterBrick to = new V3GlusterBrick();
        if (from.isSetLinks()) {
            to.getLinks().addAll(adaptOut(from.getLinks()));
        }
        if (from.isSetActions()) {
            to.setActions(adaptOut(from.getActions()));
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
            to.setGlusterClients(new V3GlusterClients());
            to.getGlusterClients().getGlusterClients().addAll(adaptOut(from.getGlusterClients().getGlusterClients()));
        }
        if (from.isSetGlusterVolume()) {
            to.setGlusterVolume(adaptOut(from.getGlusterVolume()));
        }
        if (from.isSetId()) {
            to.setId(from.getId());
        }
        if (from.isSetHref()) {
            to.setHref(from.getHref());
        }
        if (from.isSetMemoryPools()) {
            to.setMemoryPools(new V3GlusterMemoryPools());
            to.getMemoryPools().getGlusterMemoryPools().addAll(adaptOut(from.getMemoryPools().getGlusterMemoryPools()));
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
        if (from.isSetStatus()) {
            V3Status status = new V3Status();
            status.setState(from.getStatus().value());
            to.setStatus(status);
        }
        return to;
    }
}
