/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.v3.adapters;

import org.ovirt.engine.api.model.ProxyTicket;
import org.ovirt.engine.api.v3.V3Adapter;
import org.ovirt.engine.api.v3.types.V3ProxyTicket;

public class V3ProxyTicketInAdapter implements V3Adapter<V3ProxyTicket, ProxyTicket> {
    @Override
    public ProxyTicket adapt(V3ProxyTicket from) {
        ProxyTicket to = new ProxyTicket();
        if (from.isSetValue()) {
            to.setValue(from.getValue());
        }
        return to;
    }
}
