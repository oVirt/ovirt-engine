package org.ovirt.engine.api.restapi.resource;

import static org.ovirt.engine.api.restapi.resource.BackendTemplatesResource.SUB_COLLECTIONS;

import java.util.List;

import javax.ws.rs.core.Response;

import org.ovirt.engine.api.model.Action;
import org.ovirt.engine.api.model.Console;
import org.ovirt.engine.api.model.Template;
import org.ovirt.engine.api.model.VirtioScsi;
import org.ovirt.engine.api.resource.ActionResource;
import org.ovirt.engine.api.resource.AssignedPermissionsResource;
import org.ovirt.engine.api.resource.AssignedTagsResource;
import org.ovirt.engine.api.resource.CreationResource;
import org.ovirt.engine.api.resource.GraphicsConsolesResource;
import org.ovirt.engine.api.resource.TemplateCdromsResource;
import org.ovirt.engine.api.resource.TemplateDiskAttachmentsResource;
import org.ovirt.engine.api.resource.TemplateDisksResource;
import org.ovirt.engine.api.resource.TemplateNicsResource;
import org.ovirt.engine.api.resource.TemplateResource;
import org.ovirt.engine.api.resource.TemplateWatchdogsResource;
import org.ovirt.engine.api.restapi.logging.Messages;
import org.ovirt.engine.api.restapi.types.RngDeviceMapper;
import org.ovirt.engine.api.restapi.types.VmMapper;
import org.ovirt.engine.api.restapi.util.DisplayHelper;
import org.ovirt.engine.api.restapi.util.IconHelper;
import org.ovirt.engine.api.restapi.util.VmHelper;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.MoveOrCopyParameters;
import org.ovirt.engine.core.common.action.UpdateVmTemplateParameters;
import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VmTemplateParametersBase;
import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.businessentities.VmRngDevice;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.queries.GetPermissionsForObjectParameters;
import org.ovirt.engine.core.common.queries.GetVmTemplateParameters;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.Guid;

public class BackendTemplateResource
    extends AbstractBackendActionableResource<Template, VmTemplate>
    implements TemplateResource {

    public BackendTemplateResource(String id) {
        super(id, Template.class, VmTemplate.class, SUB_COLLECTIONS);
    }

    @Override
    public Template get() {
        Template template = performGet(VdcQueryType.GetVmTemplate, new GetVmTemplateParameters(guid));
        if (template != null) {
            DisplayHelper.adjustDisplayData(this, template);
        }
        return template;
    }

    @Override
    public Response remove() {
        get();
        return performAction(VdcActionType.RemoveVmTemplate, new VmTemplateParametersBase(guid));
    }

    @Override
    public Template update(Template incoming) {
        validateIconParams(incoming);
        Template result = performUpdate(
            incoming,
            new QueryIdResolver<>(VdcQueryType.GetVmTemplate, GetVmTemplateParameters.class),
            VdcActionType.UpdateVmTemplate,
            new UpdateParametersProvider()
        );
        if (result != null) {
            DisplayHelper.adjustDisplayData(this, result);
        }
        return result;
    }

    private void validateIconParams(Template incoming) {
        if (!IconHelper.validateIconParameters(incoming)) {
            throw new BaseBackendResource.WebFaultException(null,
                    localize(Messages.INVALID_ICON_PARAMETERS),
                    Response.Status.BAD_REQUEST);
        }
    }

    @Override
    public Response export(Action action) {
        validateParameters(action, "storageDomain.id|name");

        MoveOrCopyParameters params = new MoveOrCopyParameters(guid, getStorageDomainId(action));

        if (action.isSetExclusive() && action.isExclusive()) {
            params.setForceOverride(true);
        }

        return doAction(VdcActionType.ExportVmTemplate, params, action, PollingType.JOB);
    }

    @Override
    public TemplateCdromsResource getCdromsResource() {
        return inject(new BackendTemplateCdromsResource(guid));
    }

    public TemplateDisksResource getDisksResource() {
        return inject(new BackendTemplateDisksResource(guid));
    }

    @Override
    public TemplateDiskAttachmentsResource getDiskAttachmentsResource() {
        return inject(new BackendTemplateDiskAttachmentsResource(guid));
    }

    @Override
    public TemplateNicsResource getNicsResource() {
        return inject(new BackendTemplateNicsResource(guid));
    }

    @Override
    public AssignedTagsResource getTagsResource() {
        return inject(new BackendTemplateTagsResource(id));
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
    public CreationResource getCreationResource(String ids) {
        return inject(new BackendCreationResource(ids));
    }

    @Override
    public ActionResource getActionResource(String action, String ids) {
        return inject(new BackendActionResource(action, ids));
    }

    protected class UpdateParametersProvider implements ParametersProvider<Template, VmTemplate> {
        @Override
        public VdcActionParametersBase getParameters(Template incoming, VmTemplate entity) {
            VmTemplate updated = getMapper(modelType, VmTemplate.class).map(incoming, entity);
            updated.setUsbPolicy(VmMapper.getUsbPolicyOnUpdate(incoming.getUsb(), entity.getUsbPolicy()));
            UpdateVmTemplateParameters params = new UpdateVmTemplateParameters(updated);
            if (incoming.isSetRngDevice()) {
                params.setUpdateRngDevice(true);
                params.setRngDevice(RngDeviceMapper.map(incoming.getRngDevice(), null));
            }
            if(incoming.isSetSoundcardEnabled()) {
                params.setSoundDeviceEnabled(incoming.isSoundcardEnabled());
            }
            if (incoming.isSetVirtioScsi()) {
                if (incoming.getVirtioScsi().isSetEnabled()) {
                    params.setVirtioScsiEnabled(incoming.getVirtioScsi().isEnabled());
                }
            }

            IconHelper.setIconToParams(incoming, params);
            DisplayHelper.setGraphicsToParams(incoming.getDisplay(), params);

            if (incoming.isSetMemoryPolicy() && incoming.getMemoryPolicy().isSetBallooning()) {
                params.setBalloonEnabled(incoming.getMemoryPolicy().isBallooning());
            }

            return getMapper(modelType, UpdateVmTemplateParameters.class).map(incoming, params);
        }
    }

    private Cluster lookupCluster(Guid id) {
        return getEntity(Cluster.class, VdcQueryType.GetClusterByClusterId, new IdQueryParameters(id), "GetClusterByClusterId");
    }

    @Override
    protected Template doPopulate(Template model, VmTemplate entity) {
        if (!model.isSetConsole()) {
            model.setConsole(new Console());
        }
        model.getConsole().setEnabled(!getConsoleDevicesForEntity(entity.getId()).isEmpty());
        if (!model.isSetVirtioScsi()) {
            model.setVirtioScsi(new VirtioScsi());
        }
        model.getVirtioScsi().setEnabled(!VmHelper.getVirtioScsiControllersForEntity(this, entity.getId()).isEmpty());
        model.setSoundcardEnabled(VmHelper.getSoundDevicesForEntity(this, entity.getId()).isEmpty());
        setRngDevice(model);
        return model;
    }

    @Override
    protected Template deprecatedPopulate(Template model, VmTemplate entity) {
        MemoryPolicyHelper.setupMemoryBalloon(model, this);
        return model;
    }

    private void setRngDevice(Template model) {
        List<VmRngDevice> rngDevices = getEntity(List.class,
                VdcQueryType.GetRngDevice,
                new IdQueryParameters(Guid.createGuidFromString(model.getId())),
                "GetRngDevice", true);

        if (rngDevices != null && !rngDevices.isEmpty()) {
            model.setRngDevice(RngDeviceMapper.map(rngDevices.get(0), null));
        }
    }

    @Override
    public TemplateWatchdogsResource getWatchdogsResource() {
        return inject(new BackendTemplateWatchdogsResource(guid));
    }

    @Override
    public GraphicsConsolesResource getGraphicsConsolesResource() {
        return inject(new BackendTemplateGraphicsConsolesResource(guid));
    }

    private List<String> getConsoleDevicesForEntity(Guid id) {
        return getEntity(List.class,
                VdcQueryType.GetConsoleDevices,
                new IdQueryParameters(id),
                "GetConsoleDevices", true);
    }

}

