/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.v3.adapters;

import static org.ovirt.engine.api.v3.adapters.V3OutAdapters.adaptOut;

import org.ovirt.engine.api.model.KatelloErrata;
import org.ovirt.engine.api.v3.V3Adapter;
import org.ovirt.engine.api.v3.types.V3KatelloErrata;

public class V3KatelloErrataOutAdapter implements V3Adapter<KatelloErrata, V3KatelloErrata> {
    @Override
    public V3KatelloErrata adapt(KatelloErrata from) {
        V3KatelloErrata to = new V3KatelloErrata();
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
        to.getKatelloErrata().addAll(adaptOut(from.getKatelloErrata()));
        return to;
    }
}
