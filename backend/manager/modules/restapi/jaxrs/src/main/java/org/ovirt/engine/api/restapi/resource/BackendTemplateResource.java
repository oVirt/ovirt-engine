package org.ovirt.engine.api.restapi.resource;


import javax.ws.rs.core.Response;

import org.ovirt.engine.api.model.Action;
import org.ovirt.engine.api.model.CdRom;
import org.ovirt.engine.api.model.CdRoms;
import org.ovirt.engine.api.model.NIC;
import org.ovirt.engine.api.model.Nics;
import org.ovirt.engine.api.model.Template;
import org.ovirt.engine.api.resource.ActionResource;
import org.ovirt.engine.api.resource.AssignedPermissionsResource;
import org.ovirt.engine.api.resource.CreationResource;
import org.ovirt.engine.api.resource.DevicesResource;
import org.ovirt.engine.api.resource.ReadOnlyDevicesResource;
import org.ovirt.engine.api.resource.TemplateDisksResource;
import org.ovirt.engine.api.resource.TemplateResource;
import org.ovirt.engine.api.restapi.types.VmMapper;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.MoveVmParameters;
import org.ovirt.engine.core.common.action.UpdateVmTemplateParameters;
import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.businessentities.VDSGroup;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.queries.GetPermissionsForObjectParameters;
import org.ovirt.engine.core.common.queries.GetVdsGroupByVdsGroupIdParameters;
import org.ovirt.engine.core.common.queries.GetVmTemplateParameters;
import org.ovirt.engine.core.common.queries.GetVmTemplatesDisksParameters;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.Guid;

public class BackendTemplateResource
    extends AbstractBackendActionableResource<Template, VmTemplate>
    implements TemplateResource {

    static final String[] SUB_COLLECTIONS = { "disks", "nics", "cdroms", "permissions" };

    public BackendTemplateResource(String id) {
        super(id, Template.class, VmTemplate.class, SUB_COLLECTIONS);
    }

    @Override
    public Template get() {
        return performGet(VdcQueryType.GetVmTemplate, new GetVmTemplateParameters(guid));
    }

    @Override
    public Template update(Template incoming) {
        validateEnums(Template.class, incoming);
        return performUpdate(incoming,
                             new QueryIdResolver<Guid>(VdcQueryType.GetVmTemplate, GetVmTemplateParameters.class),
                             VdcActionType.UpdateVmTemplate,
                             new UpdateParametersProvider());
    }

    @Override
    public Response export(Action action) {
        validateParameters(action, "storageDomain.id|name");

        MoveVmParameters params = new MoveVmParameters(guid, getStorageDomainId(action));

        if (action.isSetExclusive() && action.isExclusive()) {
            params.setForceOverride(true);
        }

        return doAction(VdcActionType.ExportVmTemplate, params, action, PollingType.JOB);
    }

    @Override
    public ReadOnlyDevicesResource<CdRom, CdRoms> getCdRomsResource() {
        return inject(new BackendReadOnlyCdRomsResource<VmTemplate>
                                        (VmTemplate.class,
                                         guid,
                                         VdcQueryType.GetVmTemplate,
                                         new GetVmTemplateParameters(guid)));
    }

    @Override
    public TemplateDisksResource getDisksResource() {
        return inject(new BackendTemplateDisksResource(guid,
                                                       VdcQueryType.GetVmTemplatesDisks,
                                                       new GetVmTemplatesDisksParameters(guid)));
    }

    @Override
    public DevicesResource<NIC, Nics> getNicsResource() {
        return inject(new BackendTemplateNicsResource(guid));
    }

    @Override
    public AssignedPermissionsResource getPermissionsResource() {
        return inject(new BackendAssignedPermissionsResource(guid,
                                                             VdcQueryType.GetPermissionsForObject,
                                                             new GetPermissionsForObjectParameters(guid),
                                                             Template.class,
                                                             VdcObjectType.VmTemplate));
    }

    @Override
    public CreationResource getCreationSubresource(String ids) {
        return inject(new BackendCreationResource(ids));
    }

    @Override
    public ActionResource getActionSubresource(String action, String ids) {
        return inject(new BackendActionResource(action, ids));
    }

    protected class UpdateParametersProvider implements ParametersProvider<Template, VmTemplate> {
        @Override
        public VdcActionParametersBase getParameters(Template incoming, VmTemplate entity) {
            VmTemplate updated = getMapper(modelType, VmTemplate.class).map(incoming, entity);
            updated.setUsbPolicy(VmMapper.getUsbPolicyOnUpdate(incoming.getUsb(), entity.getUsbPolicy(),
                    lookupCluster(updated.getVdsGroupId())));

            return new UpdateVmTemplateParameters(updated);
        }
    }

    private VDSGroup lookupCluster(Guid id) {
        return getEntity(VDSGroup.class, VdcQueryType.GetVdsGroupByVdsGroupId, new GetVdsGroupByVdsGroupIdParameters(id), "GetVdsGroupByVdsGroupId");
    }

    @Override
    protected Template doPopulate(Template model, VmTemplate entity) {
        return model;
    }

}
