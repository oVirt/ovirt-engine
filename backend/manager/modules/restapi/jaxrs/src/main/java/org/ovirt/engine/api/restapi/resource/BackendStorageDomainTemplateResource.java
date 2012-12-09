package org.ovirt.engine.api.restapi.resource;

import javax.ws.rs.core.Response;
import org.ovirt.engine.api.model.Action;
import org.ovirt.engine.api.model.Template;
import org.ovirt.engine.api.model.Templates;
import org.ovirt.engine.api.resource.ActionResource;
import org.ovirt.engine.api.resource.StorageDomainContentResource;
import org.ovirt.engine.core.common.action.ImportVmTemplateParameters;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.queries.GetVmTemplateParameters;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.Guid;

public class BackendStorageDomainTemplateResource
    extends AbstractBackendStorageDomainContentResource<Templates, Template, VmTemplate>
    implements StorageDomainContentResource<Template> {

    public BackendStorageDomainTemplateResource(BackendStorageDomainTemplatesResource parent, String templateId) {
        super(templateId, parent, Template.class, VmTemplate.class);
    }

    @Override
    protected Template getFromDataDomain() {
        return performGet(VdcQueryType.GetVmTemplate, new GetVmTemplateParameters(guid));
    }

    @Override
    public Response doImport(Action action) {
        validateParameters(action, "cluster.id|name");

        Guid destStorageDomainId = getDestStorageDomainId(action);

        ImportVmTemplateParameters params = new ImportVmTemplateParameters(parent.getDataCenterId(destStorageDomainId),
                                                                           parent.getStorageDomainId(),
                                                                           destStorageDomainId,
                                                                           getClusterId(action),
                                                                           getEntity());
        params.setImageToDestinationDomainMap(getDiskToDestinationMap(action));
        params.setForceOverride(action.isSetExclusive() ? action.isExclusive() : false);

        if (action.isSetClone()){
            params.setImportAsNewEntity(action.isClone());
            if(action.isSetTemplate() && action.getTemplate().isSetName()) {
                params.getVmTemplate().setname(action.getTemplate().getName());
            }
        }

        return doAction(VdcActionType.ImportVmTemplate, params, action);
    }

    @Override
    public ActionResource getActionSubresource(String action, String ids) {
        return inject(new BackendActionResource(action, ids));
    }

    @Override
    protected Template addParents(Template template) {
        template.setStorageDomain(parent.getStorageDomainModel());
        return template;
    }

    protected VmTemplate getEntity() {
        for (VmTemplate entity : parent.getEntitiesFromExportDomain()) {
            if (guid.equals(entity.getId())) {
                return entity;
            }
        }
        return entityNotFound();
    }

    @Override
    protected Template doPopulate(Template model, VmTemplate entity) {
        return model;
    }
}
