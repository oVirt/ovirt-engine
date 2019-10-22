/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.v3.adapters;

import static org.ovirt.engine.api.v3.adapters.V3InAdapters.adaptIn;

import org.ovirt.engine.api.model.TemplateVersion;
import org.ovirt.engine.api.v3.V3Adapter;
import org.ovirt.engine.api.v3.types.V3TemplateVersion;

public class V3TemplateVersionInAdapter implements V3Adapter<V3TemplateVersion, TemplateVersion> {
    @Override
    public TemplateVersion adapt(V3TemplateVersion from) {
        TemplateVersion to = new TemplateVersion();
        if (from.isSetBaseTemplate()) {
            to.setBaseTemplate(adaptIn(from.getBaseTemplate()));
        }
        if (from.isSetVersionName()) {
            to.setVersionName(from.getVersionName());
        }
        if (from.isSetVersionNumber()) {
            to.setVersionNumber(from.getVersionNumber());
        }
        return to;
    }
}
