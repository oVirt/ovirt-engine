/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.v3.adapters;

import static org.ovirt.engine.api.v3.adapters.V3OutAdapters.adaptOut;

import org.ovirt.engine.api.model.TemplateVersion;
import org.ovirt.engine.api.v3.V3Adapter;
import org.ovirt.engine.api.v3.types.V3TemplateVersion;

public class V3TemplateVersionOutAdapter implements V3Adapter<TemplateVersion, V3TemplateVersion> {
    @Override
    public V3TemplateVersion adapt(TemplateVersion from) {
        V3TemplateVersion to = new V3TemplateVersion();
        if (from.isSetBaseTemplate()) {
            to.setBaseTemplate(adaptOut(from.getBaseTemplate()));
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
