/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.v3.adapters;

import org.ovirt.engine.api.model.SpecialObjects;
import org.ovirt.engine.api.model.Tag;
import org.ovirt.engine.api.model.Template;
import org.ovirt.engine.api.v3.V3Adapter;
import org.ovirt.engine.api.v3.types.V3Link;
import org.ovirt.engine.api.v3.types.V3SpecialObjects;

public class V3SpecialObjectsOutAdapter implements V3Adapter<SpecialObjects, V3SpecialObjects> {
    @Override
    public V3SpecialObjects adapt(SpecialObjects from) {
        V3SpecialObjects to = new V3SpecialObjects();
        Template blankTemplate = from.getBlankTemplate();
        if (blankTemplate != null) {
            V3Link blankTemplateLink = new V3Link();
            blankTemplateLink.setRel("templates/blank");
            blankTemplateLink.setHref(blankTemplate.getHref());
            to.getLinks().add(blankTemplateLink);
        }
        Tag rootTag = from.getRootTag();
        if (rootTag != null) {
            V3Link rootTagLink = new V3Link();
            rootTagLink.setRel("tags/root");
            rootTagLink.setHref(rootTag.getHref());
            to.getLinks().add(rootTagLink);
        }
        return to;
    }
}
