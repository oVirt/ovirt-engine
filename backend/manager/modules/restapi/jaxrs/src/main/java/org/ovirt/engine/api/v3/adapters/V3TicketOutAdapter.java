/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.v3.adapters;

import org.ovirt.engine.api.model.Ticket;
import org.ovirt.engine.api.v3.V3Adapter;
import org.ovirt.engine.api.v3.types.V3Ticket;

public class V3TicketOutAdapter implements V3Adapter<Ticket, V3Ticket> {
    @Override
    public V3Ticket adapt(Ticket from) {
        V3Ticket to = new V3Ticket();
        if (from.isSetExpiry()) {
            to.setExpiry(from.getExpiry());
        }
        if (from.isSetValue()) {
            to.setValue(from.getValue());
        }
        return to;
    }
}
