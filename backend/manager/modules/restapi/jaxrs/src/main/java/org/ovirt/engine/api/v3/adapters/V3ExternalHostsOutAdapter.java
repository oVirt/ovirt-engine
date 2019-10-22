/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.v3.adapters;

import static org.ovirt.engine.api.v3.adapters.V3OutAdapters.adaptOut;

import org.ovirt.engine.api.model.ExternalHosts;
import org.ovirt.engine.api.v3.V3Adapter;
import org.ovirt.engine.api.v3.types.V3ExternalHosts;

public class V3ExternalHostsOutAdapter implements V3Adapter<ExternalHosts, V3ExternalHosts> {
    @Override
    public V3ExternalHosts adapt(ExternalHosts from) {
        V3ExternalHosts to = new V3ExternalHosts();
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
        to.getExternalHosts().addAll(adaptOut(from.getExternalHosts()));
        return to;
    }
}
