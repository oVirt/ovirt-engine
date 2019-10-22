/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.v3.adapters;

import static org.ovirt.engine.api.v3.adapters.V3OutAdapters.adaptOut;

import org.ovirt.engine.api.model.GlusterBrickAdvancedDetails;
import org.ovirt.engine.api.v3.V3Adapter;
import org.ovirt.engine.api.v3.types.V3GlusterBrickAdvancedDetails;
import org.ovirt.engine.api.v3.types.V3GlusterClients;
import org.ovirt.engine.api.v3.types.V3GlusterMemoryPools;

public class V3GlusterBrickAdvancedDetailsOutAdapter implements V3Adapter<GlusterBrickAdvancedDetails, V3GlusterBrickAdvancedDetails> {
    @Override
    public V3GlusterBrickAdvancedDetails adapt(GlusterBrickAdvancedDetails from) {
        V3GlusterBrickAdvancedDetails to = new V3GlusterBrickAdvancedDetails();
        if (from.isSetLinks()) {
            to.getLinks().addAll(adaptOut(from.getLinks()));
        }
        if (from.isSetActions()) {
            to.setActions(adaptOut(from.getActions()));
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
        return to;
    }
}
