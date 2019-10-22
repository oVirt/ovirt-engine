/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.restapi.resource;

import org.ovirt.engine.api.model.Cdrom;
import org.ovirt.engine.api.model.Cdroms;
import org.ovirt.engine.api.model.Template;
import org.ovirt.engine.api.resource.TemplateCdromResource;
import org.ovirt.engine.api.resource.TemplateCdromsResource;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.queries.GetVmTemplateParameters;
import org.ovirt.engine.core.common.queries.QueryType;
import org.ovirt.engine.core.compat.Guid;

public class BackendTemplateCdromsResource
        extends AbstractBackendCollectionResource<Cdrom, VmTemplate>
        implements TemplateCdromsResource {

    private Guid templateId;

    public BackendTemplateCdromsResource(Guid templateId) {
        super(Cdrom.class, VmTemplate.class);
        this.templateId = templateId;
    }

    @Override
    public Cdroms list() {
        VmTemplate template = getTemplate();
        return mapCollection(template);
    }

    private Cdroms mapCollection(VmTemplate template) {
        Cdroms collection = new Cdroms();
        collection.getCdroms().add(addLinks(populate(map(template), template)));
        return collection;
    }

    private VmTemplate getTemplate() {
        return getEntity(
            VmTemplate.class,
            QueryType.GetVmTemplate,
            new GetVmTemplateParameters(templateId),
            templateId.toString(),
            true
        );
    }

    @Override
    public Cdrom addParents(Cdrom entity) {
        Template template = new Template();
        template.setId(templateId.toString());
        entity.setTemplate(template);
        return entity;
    }

    @Override
    public TemplateCdromResource getCdromResource(String id) {
        return inject(new BackendTemplateCdromResource(id, templateId));
    }
}
