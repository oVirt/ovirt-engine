/*
Copyright (c) 2016 Red Hat, Inc.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

  http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
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
