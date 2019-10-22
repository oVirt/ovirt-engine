/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.v3.adapters;

import static org.ovirt.engine.api.v3.adapters.V3InAdapters.adaptIn;

import java.util.LinkedList;
import java.util.List;

import org.ovirt.engine.api.model.GlusterBricks;
import org.ovirt.engine.api.model.GlusterVolume;
import org.ovirt.engine.api.model.GlusterVolumeStatus;
import org.ovirt.engine.api.model.GlusterVolumeType;
import org.ovirt.engine.api.model.Options;
import org.ovirt.engine.api.model.TransportType;
import org.ovirt.engine.api.v3.V3Adapter;
import org.ovirt.engine.api.v3.types.V3GlusterVolume;

public class V3GlusterVolumeInAdapter implements V3Adapter<V3GlusterVolume, GlusterVolume> {
    @Override
    public GlusterVolume adapt(V3GlusterVolume from) {
        GlusterVolume to = new GlusterVolume();
        if (from.isSetLinks()) {
            to.getLinks().addAll(adaptIn(from.getLinks()));
        }
        if (from.isSetActions()) {
            to.setActions(adaptIn(from.getActions()));
        }
        if (from.isSetBricks()) {
            to.setBricks(new GlusterBricks());
            to.getBricks().getGlusterBricks().addAll(adaptIn(from.getBricks().getGlusterBricks()));
        }
        if (from.isSetCluster()) {
            to.setCluster(adaptIn(from.getCluster()));
        }
        if (from.isSetComment()) {
            to.setComment(from.getComment());
        }
        if (from.isSetDescription()) {
            to.setDescription(from.getDescription());
        }
        if (from.isSetDisperseCount()) {
            to.setDisperseCount(from.getDisperseCount());
        }
        if (from.isSetId()) {
            to.setId(from.getId());
        }
        if (from.isSetHref()) {
            to.setHref(from.getHref());
        }
        if (from.isSetName()) {
            to.setName(from.getName());
        }
        if (from.isSetOptions()) {
            to.setOptions(new Options());
            to.getOptions().getOptions().addAll(adaptIn(from.getOptions().getOptions()));
        }
        if (from.isSetRedundancyCount()) {
            to.setRedundancyCount(from.getRedundancyCount());
        }
        if (from.isSetReplicaCount()) {
            to.setReplicaCount(from.getReplicaCount());
        }
        if (from.isSetStatus() && from.getStatus().isSetState()) {
            to.setStatus(GlusterVolumeStatus.fromValue(from.getStatus().getState()));
        }
        if (from.isSetStripeCount()) {
            to.setStripeCount(from.getStripeCount());
        }
        if (from.isSetTransportTypes()) {
            to.setTransportTypes(new GlusterVolume.TransportTypesList());
            to.getTransportTypes().getTransportTypes().addAll(adaptTransportTypes(from));
        }
        if (from.isSetVolumeType()) {
            to.setVolumeType(GlusterVolumeType.fromValue(from.getVolumeType()));
        }
        return to;
    }

    private List<TransportType> adaptTransportTypes(V3GlusterVolume from) {
        List<TransportType> results = new LinkedList<>();
        for (String s : from.getTransportTypes().getTransportTypes()) {
            results.add(TransportType.fromValue(s));
            }
        return results;
    }
}
