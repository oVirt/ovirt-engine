/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.v3.adapters;

import static org.ovirt.engine.api.v3.adapters.V3OutAdapters.adaptOut;

import org.ovirt.engine.api.model.OpenStackSubnets;
import org.ovirt.engine.api.v3.V3Adapter;
import org.ovirt.engine.api.v3.types.V3OpenStackSubnets;

public class V3OpenStackSubnetsOutAdapter implements V3Adapter<OpenStackSubnets, V3OpenStackSubnets> {
    @Override
    public V3OpenStackSubnets adapt(OpenStackSubnets from) {
        V3OpenStackSubnets to = new V3OpenStackSubnets();
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
        to.getOpenStackSubnets().addAll(adaptOut(from.getOpenStackSubnets()));
        return to;
    }
}
