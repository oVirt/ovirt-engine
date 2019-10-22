/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.v3.adapters;

import static org.ovirt.engine.api.v3.adapters.V3OutAdapters.adaptOut;

import org.ovirt.engine.api.model.NetworkLabels;
import org.ovirt.engine.api.v3.V3Adapter;
import org.ovirt.engine.api.v3.types.V3Labels;

public class V3NetworkLabelsOutAdapter implements V3Adapter<NetworkLabels, V3Labels> {
    @Override
    public V3Labels adapt(NetworkLabels from) {
        V3Labels to = new V3Labels();
        if (from.isSetActions()) {
            to.setActions(adaptOut(from.getActions()));
        }
        if (from.isSetActive()) {
            to.setActive(from.getActive());
        }
        if (from.isSetSize()) {
            to.setSize(from.getSize());
        }
        if (from.isSetTotal()) {
            to.setTotal(from.getTotal());
        }
        to.getLabels().addAll(adaptOut(from.getNetworkLabels()));
        return to;
    }
}
