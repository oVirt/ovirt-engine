/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.v3.adapters;

import org.ovirt.engine.api.model.Link;
import org.ovirt.engine.api.v3.V3Adapter;
import org.ovirt.engine.api.v3.types.V3Link;

public class V3LinkOutAdapter implements V3Adapter<Link, V3Link> {
    @Override
    public V3Link adapt(Link from) {
        V3Link to = new V3Link();
        to.setHref(from.getHref());
        to.setRel(from.getRel());
        return to;
    }
}
