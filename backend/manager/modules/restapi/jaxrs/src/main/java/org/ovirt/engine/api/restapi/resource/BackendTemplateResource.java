package org.ovirt.engine.api.restapi.resource;

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
import org.ovirt.engine.api.resource.TemplateCdromsResource;
import org.ovirt.engine.api.resource.TemplateDiskAttachmentsResource;
import org.ovirt.engine.api.resource.TemplateDisksResource;
import org.ovirt.engine.api.resource.TemplateGraphicsConsolesResource;
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
import org.ovirt.engine.core.common.action.ActionParametersBase;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.ExportOvaParameters;
import org.ovirt.engine.core.common.action.MoveOrCopyParameters;
import org.ovirt.engine.core.common.action.UpdateVmTemplateParameters;
import org.ovirt.engine.core.common.action.VmTemplateManagementParameters;
import org.ovirt.engine.core.common.businessentities.VmEntityType;
import org.ovirt.engine.core.common.businessentities.VmRngDevice;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.queries.GetPermissionsForObjectParameters;
import org.ovirt.engine.core.common.queries.GetVmTemplateParameters;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.QueryType;
import org.ovirt.engine.core.compat.Guid;

public class BackendTemplateResource
    extends AbstractBackendActionableResource<Template, VmTemplate>
    implements TemplateResource {

    public BackendTemplateResource(String id) {
        super(id, Template.class, VmTemplate.class);
    }

    @Override
    public Template get() {
        Template template = performGet(QueryType.GetVmTemplate, new GetVmTemplateParameters(guid));
        if (template != null) {
            DisplayHelper.adjustDisplayData(this, template);
        }
        return template;
    }

    @Override
    public Response remove() {
        get();
        return performAction(ActionType.RemoveVmTemplate, new VmTemplateManagementParameters(guid));
    }

    @Override
    public Template update(Template incoming) {
        validateIconParams(incoming);
        Template result = performUpdate(
            incoming,
            new QueryIdResolver<>(QueryType.GetVmTemplate, GetVmTemplateParameters.class),
            ActionType.UpdateVmTemplate,
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
    public Response exportToExportDomain(Action action) {
        MoveOrCopyParameters params = new MoveOrCopyParameters(guid, getStorageDomainId(action));

        if (action.isSetExclusive() && action.isExclusive()) {
            params.setForceOverride(true);
        }

        return doAction(ActionType.ExportVmTemplate, params, action, PollingType.JOB);
    }

    @Override
    public Response exportToPathOnHost(Action action) {
        ExportOvaParameters params = new ExportOvaParameters();

        params.setEntityType(VmEntityType.TEMPLATE);
        params.setEntityId(guid);
        params.setProxyHostId(getHostId(action));
        params.setDirectory(action.getDirectory());
        params.setName(action.getFilename());

        return doAction(ActionType.ExportVmTemplateToOva, params, action, PollingType.JOB);
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
                QueryType.GetPermissionsForObject,
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
        public ActionParametersBase getParameters(Template incoming, VmTemplate entity) {
            VmTemplate updated = getMapper(modelType, VmTemplate.class).map(incoming, entity);
            updated.setUsbPolicy(VmMapper.getUsbPolicyOnUpdate(incoming.getUsb(), entity.getUsbPolicy()));
            UpdateVmTemplateParameters params = new UpdateVmTemplateParameters(updated);
            if (incoming.isSetRngDevice()) {
                params.setUpdateRngDevice(true);
                params.setRngDevice(RngDeviceMapper.map(incoming.getRngDevice(), null));
            }
            if (incoming.isSetSoundcardEnabled()) {
                params.setSoundDeviceEnabled(incoming.isSoundcardEnabled());
            }
            if (incoming.isSetVirtioScsi()) {
                if (incoming.getVirtioScsi().isSetEnabled()) {
                    params.setVirtioScsiEnabled(incoming.getVirtioScsi().isEnabled());
                }
            }
            if (incoming.isSetTpmEnabled()) {
                params.setTpmEnabled(incoming.isTpmEnabled());
            }

            IconHelper.setIconToParams(incoming, params);
            DisplayHelper.setGraphicsToParams(incoming.getDisplay(), params);

            return getMapper(modelType, UpdateVmTemplateParameters.class).map(incoming, params);
        }
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
        model.setTpmEnabled(!VmHelper.getTpmDevicesForEntity(this, entity.getId()).isEmpty());
        setRngDevice(model);
        return model;
    }

    @Override
    protected Template deprecatedPopulate(Template model, VmTemplate entity) {
        return model;
    }

    private void setRngDevice(Template model) {
        List<VmRngDevice> rngDevices = getEntity(List.class,
                QueryType.GetRngDevice,
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
    public TemplateGraphicsConsolesResource getGraphicsConsolesResource() {
        return inject(new BackendTemplateGraphicsConsolesResource(guid));
    }

    private List<String> getConsoleDevicesForEntity(Guid id) {
        return getEntity(List.class,
                QueryType.GetConsoleDevices,
                new IdQueryParameters(id),
                "GetConsoleDevices", true);
    }

}

