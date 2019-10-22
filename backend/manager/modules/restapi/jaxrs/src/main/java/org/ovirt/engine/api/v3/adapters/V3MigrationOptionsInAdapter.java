/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.v3.adapters;

import org.ovirt.engine.api.model.InheritableBoolean;
import org.ovirt.engine.api.model.MigrationOptions;
import org.ovirt.engine.api.v3.V3Adapter;
import org.ovirt.engine.api.v3.types.V3MigrationOptions;

public class V3MigrationOptionsInAdapter implements V3Adapter<V3MigrationOptions, MigrationOptions> {
    @Override
    public MigrationOptions adapt(V3MigrationOptions from) {
        MigrationOptions to = new MigrationOptions();
        if (from.isSetAutoConverge()) {
            to.setAutoConverge(InheritableBoolean.fromValue(from.getAutoConverge()));
        }
        if (from.isSetCompressed()) {
            to.setCompressed(InheritableBoolean.fromValue(from.getCompressed()));
        }
        return to;
    }
}
