package org.ovirt.engine.api.restapi.resource;

import java.util.List;

import javax.ws.rs.core.Response;

import org.ovirt.engine.api.model.Template;
import org.ovirt.engine.api.model.Templates;
import org.ovirt.engine.api.resource.TemplateResource;
import org.ovirt.engine.api.resource.TemplatesResource;

import org.ovirt.engine.core.common.action.AddVmTemplateParameters;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VmTemplateParametersBase;
import org.ovirt.engine.core.common.businessentities.VDSGroup;
import org.ovirt.engine.core.common.businessentities.VmStatic;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.interfaces.SearchType;
import org.ovirt.engine.core.common.queries.GetVmByVmIdParameters;
import org.ovirt.engine.core.common.queries.GetVmTemplateParameters;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.Guid;

public class BackendTemplatesResource
    extends AbstractBackendCollectionResource<Template, VmTemplate>
    implements TemplatesResource {

    static final String[] SUB_COLLECTIONS = { "disks", "nics", "cdroms", "permissions" };

    public BackendTemplatesResource() {
        super(Template.class, VmTemplate.class, SUB_COLLECTIONS);
    }

    @Override
    public Templates list() {
        return mapCollection(getBackendCollection(SearchType.VmTemplate));
    }

    @Override
    @SingleEntityResource
    public TemplateResource getTemplateSubResource(String id) {
        return inject(new BackendTemplateResource(id));
    }

    @Override
    public Response add(Template template) {
        validateParameters(template, "name", "vm.id|name");
        VmStatic staticVm = getMapper(Template.class, VmStatic.class).map(template, getVm(template));
        if (namedCluster(template)) {
            staticVm.setvds_group_id(getClusterId(template));
        }
        // REVISIT: powershell has a IsVmTemlateWithSameNameExist safety check
        AddVmTemplateParameters params = new AddVmTemplateParameters(staticVm,
                                       template.getName(),
                                       template.getDescription());
        if (template.isSetStorageDomain() && template.getStorageDomain().isSetId()) {
            params.setDestinationStorageDomainId(asGuid(template.getStorageDomain().getId()));
        }
        return performCreation(VdcActionType.AddVmTemplate,
                               params,
                               new QueryIdResolver(VdcQueryType.GetVmTemplate,
                                                   GetVmTemplateParameters.class));
    }

    @Override
    public Response performRemove(String id) {
        return performAction(VdcActionType.RemoveVmTemplate, new VmTemplateParametersBase(asGuid(id)));
    }

    protected Templates mapCollection(List<VmTemplate> entities) {
        Templates collection = new Templates();
        for (VmTemplate entity : entities) {
            collection.getTemplates().add(addLinks(map(entity)));
        }
        return collection;
    }

    protected VmStatic getVm(Template template) {
        org.ovirt.engine.core.common.businessentities.VM vm;
        if (template.getVm().isSetId()) {
            vm = getEntity(org.ovirt.engine.core.common.businessentities.VM.class,
                           VdcQueryType.GetVmByVmId,
                           new GetVmByVmIdParameters(asGuid(template.getVm().getId())),
                           template.getVm().getId());
        } else {
            vm = getEntity(org.ovirt.engine.core.common.businessentities.VM.class,
                           SearchType.VM,
                           "VM: name=" + template.getVm().getName());
        }
        return vm.getStaticData();
    }

    protected boolean namedCluster(Template template) {
        return template.isSetCluster() && template.getCluster().isSetName() && !template.getCluster().isSetId();
    }

    protected Guid getClusterId(Template template) {
        return getEntity(VDSGroup.class, SearchType.Cluster,
                         "Cluster: name=" + template.getCluster().getName()).getID();
    }
}
