package org.ovirt.engine.api.restapi.resource;

import javax.ws.rs.core.Response;

import org.ovirt.engine.api.model.Template;
import org.ovirt.engine.api.model.VmMediatedDevice;
import org.ovirt.engine.api.model.VmMediatedDevices;
import org.ovirt.engine.api.resource.TemplateMediatedDeviceResource;
import org.ovirt.engine.api.resource.TemplateMediatedDevicesResource;
import org.ovirt.engine.core.common.businessentities.VmMdevType;
import org.ovirt.engine.core.compat.Guid;

public class BackendTemplateMediatedDevicesResource
    extends AbstractBackendCollectionResource<VmMediatedDevice, VmMdevType>
    implements TemplateMediatedDevicesResource {

    private final Guid templateId;

    public BackendTemplateMediatedDevicesResource(Guid templateId) {
        super(VmMediatedDevice.class, VmMdevType.class);
        this.templateId = templateId;
    }

    @Override
    public TemplateMediatedDeviceResource getDeviceResource(String mdevId) {
        return inject(new BackendTemplateMediatedDeviceResource(this, templateId, mdevId));
    }

    @Override
    public VmMediatedDevices list() {
        return BackendMdevHelper.list(this, templateId);
    }

    @Override
    public Response add(VmMediatedDevice mdev) {
        return BackendMdevHelper.add(this, this::list, mdev, templateId, false);
    }

    @Override
    protected VmMediatedDevice addParents(VmMediatedDevice model) {
        model.setTemplate(new Template());
        model.getTemplate().setId(templateId.toString());
        return model;
    }
}
