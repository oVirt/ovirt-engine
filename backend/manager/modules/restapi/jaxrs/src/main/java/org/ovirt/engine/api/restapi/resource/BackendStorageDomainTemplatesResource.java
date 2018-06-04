package org.ovirt.engine.api.restapi.resource;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.ovirt.engine.api.model.Template;
import org.ovirt.engine.api.model.Templates;
import org.ovirt.engine.api.resource.StorageDomainTemplateResource;
import org.ovirt.engine.api.resource.StorageDomainTemplatesResource;
import org.ovirt.engine.api.restapi.util.ParametersHelper;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.queries.GetAllFromExportDomainQueryParameters;
import org.ovirt.engine.core.common.queries.GetVmTemplatesFromStorageDomainParameters;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.QueryType;
import org.ovirt.engine.core.compat.Guid;

public class BackendStorageDomainTemplatesResource
        extends AbstractBackendStorageDomainContentsResource<Templates, Template, VmTemplate>
        implements StorageDomainTemplatesResource {

    public BackendStorageDomainTemplatesResource(Guid storageDomainId) {
        super(storageDomainId, Template.class, VmTemplate.class);
    }

    @Override
    public Templates list() {
        Templates templates = new Templates();
        boolean unregistered = ParametersHelper.getBooleanParameter(httpHeaders, uriInfo, UNREGISTERED_CONSTRAINT_PARAMETER, true, false);
        if (unregistered) {
            List<org.ovirt.engine.core.common.businessentities.VmTemplate> unregisteredTemplates =
                    getBackendCollection(QueryType.GetUnregisteredVmTemplates,
                            new IdQueryParameters(storageDomainId));
            List<Template> collection = new ArrayList<>();
            for (org.ovirt.engine.core.common.businessentities.VmTemplate entity : unregisteredTemplates) {
                Template vmTemplate = map(entity);
                collection.add(addLinks(populate(vmTemplate, entity)));
            }
            templates.getTemplates().addAll(collection);
        } else {
            templates.getTemplates().addAll(getCollection());
        }
        return templates;
    }

    @Override
    protected Template addParents(Template template) {
        template.setStorageDomain(getStorageDomainModel());
        return template;
    }

    @SuppressWarnings("unchecked")
    @Override
    protected Collection<VmTemplate> getEntitiesFromExportDomain() {
        GetAllFromExportDomainQueryParameters params =
            new GetAllFromExportDomainQueryParameters(getDataCenterId(storageDomainId), storageDomainId);

        Map<VmTemplate, List<DiskImage>> ret = getEntity(HashMap.class,
                                                         QueryType.GetTemplatesFromExportDomain,
                                                         params,
                                                         "Templates under storage domain id : " + storageDomainId.toString());
        return ret.keySet();
    }

    @Override
    protected Collection<VmTemplate> getEntitiesFromDataDomain() {
        GetVmTemplatesFromStorageDomainParameters params =
                new GetVmTemplatesFromStorageDomainParameters(storageDomainId, false);
        return getBackendCollection(QueryType.GetVmTemplatesFromStorageDomain, params);
    }

    @Override
    public StorageDomainTemplateResource getTemplateResource(String id) {
        return inject(new BackendStorageDomainTemplateResource(this, id));
    }
}
