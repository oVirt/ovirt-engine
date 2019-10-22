/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.v3.adapters;

import static org.ovirt.engine.api.v3.adapters.V3OutAdapters.adaptOut;

import org.ovirt.engine.api.model.Payloads;
import org.ovirt.engine.api.v3.V3Adapter;
import org.ovirt.engine.api.v3.types.V3Payloads;

public class V3PayloadsOutAdapter implements V3Adapter<Payloads, V3Payloads> {
    @Override
    public V3Payloads adapt(Payloads from) {
        V3Payloads to = new V3Payloads();
        to.getPayload().addAll(adaptOut(from.getPayloads()));
        return to;
    }
}
