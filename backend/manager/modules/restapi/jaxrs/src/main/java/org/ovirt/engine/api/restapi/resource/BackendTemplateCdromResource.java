/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.restapi.resource;

import org.ovirt.engine.api.model.Cdrom;
import org.ovirt.engine.api.model.Template;
import org.ovirt.engine.api.resource.CreationResource;
import org.ovirt.engine.api.resource.TemplateCdromResource;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.queries.GetVmTemplateParameters;
import org.ovirt.engine.core.common.queries.QueryType;
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
           QueryType.GetVmTemplate,
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
