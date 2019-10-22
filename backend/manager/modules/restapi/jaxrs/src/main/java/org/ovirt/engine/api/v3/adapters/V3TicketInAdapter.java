/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.v3.adapters;

import org.ovirt.engine.api.model.Ticket;
import org.ovirt.engine.api.v3.V3Adapter;
import org.ovirt.engine.api.v3.types.V3Ticket;

public class V3TicketInAdapter implements V3Adapter<V3Ticket, Ticket> {
    @Override
    public Ticket adapt(V3Ticket from) {
        Ticket to = new Ticket();
        if (from.isSetExpiry()) {
            to.setExpiry(from.getExpiry());
        }
        if (from.isSetValue()) {
            to.setValue(from.getValue());
        }
        return to;
    }
}
