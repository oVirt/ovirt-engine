/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.v3.adapters;

import org.ovirt.engine.api.model.Package;
import org.ovirt.engine.api.v3.V3Adapter;
import org.ovirt.engine.api.v3.types.V3Package;

public class V3PackageInAdapter implements V3Adapter<V3Package, Package> {
    @Override
    public Package adapt(V3Package from) {
        Package to = new Package();
        if (from.isSetName()) {
            to.setName(from.getName());
        }
        return to;
    }
}
