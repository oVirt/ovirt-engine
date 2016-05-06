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

import java.util.LinkedList;
import java.util.List;

import org.ovirt.engine.api.model.GlusterVolume;
import org.ovirt.engine.api.model.TransportType;
import org.ovirt.engine.api.v3.V3Adapter;
import org.ovirt.engine.api.v3.types.V3GlusterBricks;
import org.ovirt.engine.api.v3.types.V3GlusterVolume;
import org.ovirt.engine.api.v3.types.V3Options;
import org.ovirt.engine.api.v3.types.V3Status;
import org.ovirt.engine.api.v3.types.V3TransportTypes;

public class V3GlusterVolumeOutAdapter implements V3Adapter<GlusterVolume, V3GlusterVolume> {
    @Override
    public V3GlusterVolume adapt(GlusterVolume from) {
        V3GlusterVolume to = new V3GlusterVolume();
        if (from.isSetLinks()) {
            to.getLinks().addAll(adaptOut(from.getLinks()));
        }
        if (from.isSetActions()) {
            to.setActions(adaptOut(from.getActions()));
        }
        if (from.isSetBricks()) {
            to.setBricks(new V3GlusterBricks());
            to.getBricks().getGlusterBricks().addAll(adaptOut(from.getBricks().getGlusterBricks()));
        }
        if (from.isSetCluster()) {
            to.setCluster(adaptOut(from.getCluster()));
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
            to.setOptions(new V3Options());
            to.getOptions().getOptions().addAll(adaptOut(from.getOptions().getOptions()));
        }
        if (from.isSetRedundancyCount()) {
            to.setRedundancyCount(from.getRedundancyCount());
        }
        if (from.isSetReplicaCount()) {
            to.setReplicaCount(from.getReplicaCount());
        }
        if (from.isSetStatus()) {
            V3Status status = new V3Status();
            status.setState(from.getStatus().value());
            to.setStatus(status);
        }
        if (from.isSetStripeCount()) {
            to.setStripeCount(from.getStripeCount());
        }
        if (from.isSetTransportTypes()) {
            to.setTransportTypes(new V3TransportTypes());
            to.getTransportTypes().getTransportTypes().addAll(adaptTransportTypes(from));
        }
        if (from.isSetVolumeType()) {
            to.setVolumeType(from.getVolumeType().value());
        }
        return to;
    }

    private List<String> adaptTransportTypes(GlusterVolume from) {
        List<String> results = new LinkedList<>();
        for (TransportType transportType : from.getTransportTypes().getTransportTypes()) {
            results.add(transportType.value());
        }
        return results;
    }
}
