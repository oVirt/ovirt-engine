/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.v3.adapters;

import org.ovirt.engine.api.model.ErrorHandling;
import org.ovirt.engine.api.model.MigrateOnError;
import org.ovirt.engine.api.v3.V3Adapter;
import org.ovirt.engine.api.v3.types.V3ErrorHandling;

public class V3ErrorHandlingInAdapter implements V3Adapter<V3ErrorHandling, ErrorHandling> {
    @Override
    public ErrorHandling adapt(V3ErrorHandling from) {
        ErrorHandling to = new ErrorHandling();
        if (from.isSetOnError()) {
            to.setOnError(MigrateOnError.fromValue(from.getOnError()));
        }
        return to;
    }
}
