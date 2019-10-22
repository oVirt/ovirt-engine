/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.v3.adapters;

import static org.ovirt.engine.api.v3.adapters.V3InAdapters.adaptIn;

import org.ovirt.engine.api.model.IscsiBond;
import org.ovirt.engine.api.model.Networks;
import org.ovirt.engine.api.model.StorageConnections;
import org.ovirt.engine.api.v3.V3Adapter;
import org.ovirt.engine.api.v3.types.V3IscsiBond;

public class V3IscsiBondInAdapter implements V3Adapter<V3IscsiBond, IscsiBond> {
    @Override
    public IscsiBond adapt(V3IscsiBond from) {
        IscsiBond to = new IscsiBond();
        if (from.isSetLinks()) {
            to.getLinks().addAll(adaptIn(from.getLinks()));
        }
        if (from.isSetActions()) {
            to.setActions(adaptIn(from.getActions()));
        }
        if (from.isSetComment()) {
            to.setComment(from.getComment());
        }
        if (from.isSetDataCenter()) {
            to.setDataCenter(adaptIn(from.getDataCenter()));
        }
        if (from.isSetDescription()) {
            to.setDescription(from.getDescription());
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
        if (from.isSetNetworks()) {
            to.setNetworks(new Networks());
            to.getNetworks().getNetworks().addAll(adaptIn(from.getNetworks().getNetworks()));
        }
        if (from.isSetStorageConnections()) {
            to.setStorageConnections(new StorageConnections());
            to.getStorageConnections().getStorageConnections().addAll(adaptIn(from.getStorageConnections().getStorageConnections()));
        }
        return to;
    }
}
