/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.v3.adapters;

import static org.ovirt.engine.api.v3.adapters.V3OutAdapters.adaptOut;

import org.ovirt.engine.api.model.IscsiBond;
import org.ovirt.engine.api.v3.V3Adapter;
import org.ovirt.engine.api.v3.types.V3IscsiBond;
import org.ovirt.engine.api.v3.types.V3Networks;
import org.ovirt.engine.api.v3.types.V3StorageConnections;

public class V3IscsiBondOutAdapter implements V3Adapter<IscsiBond, V3IscsiBond> {
    @Override
    public V3IscsiBond adapt(IscsiBond from) {
        V3IscsiBond to = new V3IscsiBond();
        if (from.isSetLinks()) {
            to.getLinks().addAll(adaptOut(from.getLinks()));
        }
        if (from.isSetActions()) {
            to.setActions(adaptOut(from.getActions()));
        }
        if (from.isSetComment()) {
            to.setComment(from.getComment());
        }
        if (from.isSetDataCenter()) {
            to.setDataCenter(adaptOut(from.getDataCenter()));
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
            to.setNetworks(new V3Networks());
            to.getNetworks().getNetworks().addAll(adaptOut(from.getNetworks().getNetworks()));
        }
        if (from.isSetStorageConnections()) {
            to.setStorageConnections(new V3StorageConnections());
            to.getStorageConnections().getStorageConnections().addAll(adaptOut(from.getStorageConnections().getStorageConnections()));
        }
        return to;
    }
}
