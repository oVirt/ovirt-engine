/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.v3.adapters;

import static org.ovirt.engine.api.v3.adapters.V3InAdapters.adaptIn;

import org.ovirt.engine.api.model.StorageDomains;
import org.ovirt.engine.api.v3.V3Adapter;
import org.ovirt.engine.api.v3.types.V3StorageDomains;

public class V3StorageDomainsInAdapter implements V3Adapter<V3StorageDomains, StorageDomains> {
    @Override
    public StorageDomains adapt(V3StorageDomains from) {
        StorageDomains to = new StorageDomains();
        if (from.isSetActions()) {
            to.setActions(adaptIn(from.getActions()));
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
        to.getStorageDomains().addAll(adaptIn(from.getStorageDomains()));
        return to;
    }
}
