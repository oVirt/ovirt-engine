/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.v3.adapters;

import static org.ovirt.engine.api.v3.adapters.V3OutAdapters.adaptOut;

import org.ovirt.engine.api.model.NetworkAttachments;
import org.ovirt.engine.api.v3.V3Adapter;
import org.ovirt.engine.api.v3.types.V3NetworkAttachments;

public class V3NetworkAttachmentsOutAdapter implements V3Adapter<NetworkAttachments, V3NetworkAttachments> {
    @Override
    public V3NetworkAttachments adapt(NetworkAttachments from) {
        V3NetworkAttachments to = new V3NetworkAttachments();
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
        to.getNetworkAttachments().addAll(adaptOut(from.getNetworkAttachments()));
        return to;
    }
}
