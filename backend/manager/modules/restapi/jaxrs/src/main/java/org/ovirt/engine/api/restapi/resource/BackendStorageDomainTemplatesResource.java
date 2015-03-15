package org.ovirt.engine.api.restapi.resource;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.ws.rs.core.Response;

import org.ovirt.engine.api.common.util.QueryHelper;
import org.ovirt.engine.api.model.Template;
import org.ovirt.engine.api.model.Templates;
import org.ovirt.engine.api.resource.RemovableStorageDomainContentsResource;
import org.ovirt.engine.api.resource.StorageDomainContentResource;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VmTemplateImportExportParameters;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.queries.GetAllFromExportDomainQueryParameters;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.Guid;

public class BackendStorageDomainTemplatesResource
    extends AbstractBackendStorageDomainContentsResource<Templates, Template, VmTemplate>
    implements RemovableStorageDomainContentsResource<Templates, Template> {

    static final String[] SUB_COLLECTIONS = { "disks" };

    public BackendStorageDomainTemplatesResource(Guid storageDomainId) {
        super(storageDomainId, Template.class, VmTemplate.class, SUB_COLLECTIONS);
    }

    @Override
    public Templates list() {
        Templates templates = new Templates();
        if (QueryHelper.hasMatrixParam(getUriInfo(), UNREGISTERED_CONSTRAINT_PARAMETER)) {
            List<org.ovirt.engine.core.common.businessentities.VmTemplate> unregisteredTemplates =
                    getBackendCollection(VdcQueryType.GetUnregisteredVmTemplates,
                            new IdQueryParameters(storageDomainId));
            List<Template> collection = new ArrayList<Template>();
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
                                                         VdcQueryType.GetTemplatesFromExportDomain,
                                                         params,
                                                         "Templates under storage domain id : " + storageDomainId.toString());
        return ret.keySet();
    }

    @Override
    @SingleEntityResource
    public StorageDomainContentResource<Template> getStorageDomainContentSubResource(String id) {
        return inject(new BackendStorageDomainTemplateResource(this, id));
    }

    @Override
    public Response performRemove(String id) {
        return performAction(VdcActionType.RemoveVmTemplateFromImportExport,
                             new VmTemplateImportExportParameters(asGuid(id),
                                                                  storageDomainId,
                                                                  getDataCenterId(storageDomainId)));
    }

    @Override
    protected Template doPopulate(Template model, VmTemplate entity) {
        return model;
    }
}
