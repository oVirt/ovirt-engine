package org.ovirt.engine.api.restapi.resource;


import org.ovirt.engine.api.model.Console;
import org.ovirt.engine.api.model.InstanceType;
import org.ovirt.engine.api.model.NIC;
import org.ovirt.engine.api.model.Nics;
import org.ovirt.engine.api.model.VirtIOSCSI;
import org.ovirt.engine.api.resource.CreationResource;
import org.ovirt.engine.api.resource.DevicesResource;
import org.ovirt.engine.api.resource.InstanceTypeResource;
import org.ovirt.engine.api.resource.WatchdogsResource;
import org.ovirt.engine.api.restapi.types.RngDeviceMapper;
import org.ovirt.engine.api.restapi.types.VmMapper;
import org.ovirt.engine.api.restapi.util.VmHelper;
import org.ovirt.engine.core.common.action.UpdateVmTemplateParameters;
import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.businessentities.VmRngDevice;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.queries.GetVmTemplateParameters;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.Version;

import java.util.List;

public class BackendInstanceTypeResource
    extends AbstractBackendActionableResource<InstanceType, org.ovirt.engine.core.common.businessentities.InstanceType>
    implements InstanceTypeResource {

    static final String[] SUB_COLLECTIONS = { "nics", "watchdogs" };

    public BackendInstanceTypeResource(String id) {
        super(id, InstanceType.class, org.ovirt.engine.core.common.businessentities.InstanceType.class, SUB_COLLECTIONS);
    }

    @Override
    public InstanceType get() {
        return performGet(VdcQueryType.GetVmTemplate, new GetVmTemplateParameters(guid));
    }

    @Override
    public InstanceType update(InstanceType incoming) {
        return performUpdate(incoming,
                             new QueryIdResolver<Guid>(VdcQueryType.GetVmTemplate, GetVmTemplateParameters.class),
                             VdcActionType.UpdateVmTemplate,
                             new UpdateParametersProvider());
    }

    @Override
    protected InstanceType doPopulate(InstanceType model, org.ovirt.engine.core.common.businessentities.InstanceType entity) {
        if (!model.isSetConsole()) {
            model.setConsole(new Console());
        }
        model.getConsole().setEnabled(!getConsoleDevicesForEntity(entity.getId()).isEmpty());
        if (!model.isSetVirtioScsi()) {
            model.setVirtioScsi(new VirtIOSCSI());
        }
        model.getVirtioScsi().setEnabled(!VmHelper.getInstance().getVirtioScsiControllersForEntity(entity.getId()).isEmpty());
        model.setSoundcardEnabled(!VmHelper.getInstance().getSoundDevicesForEntity(entity.getId()).isEmpty());
        setRngDevice(model);
        return model;
    }

    @Override
    public DevicesResource<NIC, Nics> getNicsResource() {
        return inject(new BackendTemplateNicsResource(guid));
    }

    @Override
    public WatchdogsResource getWatchdogsResource() {
        return inject(new BackendTemplateWatchdogsResource(guid,
                VdcQueryType.GetWatchdog,
                new IdQueryParameters(guid)));
    }

    @Override
    public CreationResource getCreationSubresource(String oid) {
        return inject(new BackendCreationResource(oid));
    }

    private void setRngDevice(InstanceType model) {
        List<VmRngDevice> rngDevices = getEntity(List.class,
            VdcQueryType.GetRngDevice,
            new IdQueryParameters(Guid.createGuidFromString(model.getId())),
            "GetRngDevice", true);

        if (rngDevices != null && !rngDevices.isEmpty()) {
            model.setRngDevice(RngDeviceMapper.map(rngDevices.get(0), null));
        }
    }

    private List<String> getConsoleDevicesForEntity(Guid id) {
        return getEntity(List.class,
                VdcQueryType.GetConsoleDevices,
                new IdQueryParameters(id),
                "GetConsoleDevices", true);
    }

    protected class UpdateParametersProvider implements ParametersProvider<InstanceType, org.ovirt.engine.core.common.businessentities.InstanceType> {
        @Override
        public VdcActionParametersBase getParameters(InstanceType incoming, org.ovirt.engine.core.common.businessentities.InstanceType entity) {
            org.ovirt.engine.core.common.businessentities.InstanceType updated = getMapper(modelType, org.ovirt.engine.core.common.businessentities.InstanceType.class).map(incoming, entity);
            updated.setUsbPolicy(VmMapper.getUsbPolicyOnUpdate(incoming.getUsb(), entity.getUsbPolicy(), Version.getLast()));

            UpdateVmTemplateParameters updateParams = new UpdateVmTemplateParameters((VmTemplate) updated);
            if (incoming.isSetRngDevice()) {
                updateParams.setUpdateRngDevice(true);
                updateParams.setRngDevice(RngDeviceMapper.map(incoming.getRngDevice(), null));
            }
            if(incoming.isSetSoundcardEnabled()) {
                updateParams.setSoundDeviceEnabled(incoming.isSoundcardEnabled());
            }

            return getMapper(modelType, UpdateVmTemplateParameters.class).map(incoming, updateParams);
        }
    }
}
