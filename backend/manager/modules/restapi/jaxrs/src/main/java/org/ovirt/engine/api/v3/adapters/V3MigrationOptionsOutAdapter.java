/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.v3.adapters;

import org.ovirt.engine.api.model.MigrationOptions;
import org.ovirt.engine.api.v3.V3Adapter;
import org.ovirt.engine.api.v3.types.V3MigrationOptions;

public class V3MigrationOptionsOutAdapter implements V3Adapter<MigrationOptions, V3MigrationOptions> {
    @Override
    public V3MigrationOptions adapt(MigrationOptions from) {
        V3MigrationOptions to = new V3MigrationOptions();
        if (from.isSetAutoConverge()) {
            to.setAutoConverge(from.getAutoConverge().value());
        }
        if (from.isSetCompressed()) {
            to.setCompressed(from.getCompressed().value());
        }
        return to;
    }
}
