/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.v3.adapters;

import org.ovirt.engine.api.model.Package;
import org.ovirt.engine.api.v3.V3Adapter;
import org.ovirt.engine.api.v3.types.V3Package;

public class V3PackageOutAdapter implements V3Adapter<Package, V3Package> {
    @Override
    public V3Package adapt(Package from) {
        V3Package to = new V3Package();
        if (from.isSetName()) {
            to.setName(from.getName());
        }
        return to;
    }
}
