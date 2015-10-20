/*
Copyright (c) 2015 Red Hat, Inc.

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

package org.ovirt.engine.api.restapi.resource;

import org.ovirt.engine.api.model.Cdrom;
import org.ovirt.engine.api.model.Template;
import org.ovirt.engine.api.resource.CreationResource;
import org.ovirt.engine.api.resource.TemplateCdromResource;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.queries.GetVmTemplateParameters;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.Guid;

public class BackendTemplateCdromResource
        extends AbstractBackendSubResource<Cdrom, VmTemplate>
        implements TemplateCdromResource {

    private Guid templateId;

    public BackendTemplateCdromResource(String cdromId, Guid templateId) {
        super(cdromId, Cdrom.class, VmTemplate.class);
        this.templateId = templateId;
    }

    @Override
    public Cdrom get() {
        GetVmTemplateParameters parameters = new GetVmTemplateParameters(templateId);
        VmTemplate entity = getEntity(
           VmTemplate.class,
           VdcQueryType.GetVmTemplate,
           parameters,
           templateId.toString(),
           true
        );
        return addLinks(populate(map(entity), entity));
    }


    @Override
    public Cdrom addParents(Cdrom entity) {
        Template template = new Template();
        template.setId(templateId.toString());
        entity.setTemplate(template);
        return entity;
    }

    @Override
    public CreationResource getCreationResource(String ids) {
        return inject(new BackendCreationResource(ids));
    }
}
