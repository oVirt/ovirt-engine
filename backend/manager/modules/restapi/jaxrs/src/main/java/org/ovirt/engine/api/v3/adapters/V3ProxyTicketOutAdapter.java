/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.v3.adapters;

import org.ovirt.engine.api.model.ProxyTicket;
import org.ovirt.engine.api.v3.V3Adapter;
import org.ovirt.engine.api.v3.types.V3ProxyTicket;

public class V3ProxyTicketOutAdapter implements V3Adapter<ProxyTicket, V3ProxyTicket> {
    @Override
    public V3ProxyTicket adapt(ProxyTicket from) {
        V3ProxyTicket to = new V3ProxyTicket();
        if (from.isSetValue()) {
            to.setValue(from.getValue());
        }
        return to;
    }
}
