/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.v3.adapters;

import static org.ovirt.engine.api.v3.adapters.V3InAdapters.adaptIn;

import org.ovirt.engine.api.model.Payloads;
import org.ovirt.engine.api.v3.V3Adapter;
import org.ovirt.engine.api.v3.types.V3Payloads;

public class V3PayloadsInAdapter implements V3Adapter<V3Payloads, Payloads> {
    @Override
    public Payloads adapt(V3Payloads from) {
        Payloads to = new Payloads();
        to.getPayloads().addAll(adaptIn(from.getPayload()));
        return to;
    }
}
