/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.v3.adapters;

import org.ovirt.engine.api.model.Link;
import org.ovirt.engine.api.v3.V3Adapter;
import org.ovirt.engine.api.v3.types.V3Link;

public class V3LinkInAdapter implements V3Adapter<V3Link, Link> {
    @Override
    public Link adapt(V3Link from) {
        Link to = new Link();
        to.setHref(from.getHref());
        to.setRel(from.getRel());
        return to;
    }
}
