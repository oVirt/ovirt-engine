/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.v3.adapters;

import static org.ovirt.engine.api.v3.adapters.V3OutAdapters.adaptOut;

import org.ovirt.engine.api.model.StorageDomains;
import org.ovirt.engine.api.v3.V3Adapter;
import org.ovirt.engine.api.v3.types.V3StorageDomains;

public class V3StorageDomainsOutAdapter implements V3Adapter<StorageDomains, V3StorageDomains> {
    @Override
    public V3StorageDomains adapt(StorageDomains from) {
        V3StorageDomains to = new V3StorageDomains();
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
        to.getStorageDomains().addAll(adaptOut(from.getStorageDomains()));
        return to;
    }
}
