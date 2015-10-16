package org.ovirt.engine.api.restapi.resource;


import java.util.List;

import javax.ws.rs.core.Response;

import org.ovirt.engine.api.model.Console;
import org.ovirt.engine.api.model.InstanceType;
import org.ovirt.engine.api.model.Nic;
import org.ovirt.engine.api.model.Nics;
import org.ovirt.engine.api.model.VirtioScsi;
import org.ovirt.engine.api.resource.CreationResource;
import org.ovirt.engine.api.resource.DevicesResource;
import org.ovirt.engine.api.resource.GraphicsConsolesResource;
import org.ovirt.engine.api.resource.InstanceTypeResource;
import org.ovirt.engine.api.resource.WatchdogsResource;
import org.ovirt.engine.api.restapi.types.RngDeviceMapper;
import org.ovirt.engine.api.restapi.types.VmMapper;
import org.ovirt.engine.api.restapi.util.DisplayHelper;
import org.ovirt.engine.api.restapi.util.VmHelper;
import org.ovirt.engine.core.common.action.UpdateVmTemplateParameters;
import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VmTemplateParametersBase;
import org.ovirt.engine.core.common.businessentities.VmRngDevice;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.queries.GetVmTemplateParameters;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.Guid;

public class BackendInstanceTypeResource
    extends AbstractBackendActionableResource<InstanceType, org.ovirt.engine.core.common.businessentities.InstanceType>
    implements InstanceTypeResource {

    static final String[] SUB_COLLECTIONS = { "nics", "watchdogs", "graphicsconsoles" };

    public BackendInstanceTypeResource(String id) {
        super(id, InstanceType.class, org.ovirt.engine.core.common.businessentities.InstanceType.class, SUB_COLLECTIONS);
    }

    @Override
    public InstanceType get() {
        InstanceType instanceType = performGet(VdcQueryType.GetInstanceType, new GetVmTemplateParameters(guid));
        DisplayHelper.adjustDisplayData(this, instanceType);
        return instanceType;
    }

    @Override
    public InstanceType update(InstanceType incoming) {
        InstanceType instanceType = performUpdate(incoming,
                new QueryIdResolver<Guid>(VdcQueryType.GetInstanceType, GetVmTemplateParameters.class),
                VdcActionType.UpdateVmTemplate,
                new UpdateParametersProvider());

        if (instanceType != null) {
            DisplayHelper.adjustDisplayData(this, instanceType);
        }

        return instanceType;
    }

    @Override
    public Response remove() {
        get();
        return performAction(VdcActionType.RemoveVmTemplate, new VmTemplateParametersBase(guid));
    }

    @Override
    protected InstanceType doPopulate(InstanceType model, org.ovirt.engine.core.common.businessentities.InstanceType entity) {
        if (!model.isSetConsole()) {
            model.setConsole(new Console());
        }
        model.getConsole().setEnabled(!getConsoleDevicesForEntity(entity.getId()).isEmpty());
        if (!model.isSetVirtioScsi()) {
            model.setVirtioScsi(new VirtioScsi());
        }
        model.getVirtioScsi().setEnabled(!VmHelper.getVirtioScsiControllersForEntity(this, entity.getId()).isEmpty());
        model.setSoundcardEnabled(!VmHelper.getSoundDevicesForEntity(this, entity.getId()).isEmpty());
        setRngDevice(model);
        return model;
    }

    @Override
    public DevicesResource<Nic, Nics> getNicsResource() {
        return inject(new BackendInstanceTypeNicsResource(guid));
    }

    protected InstanceType deprecatedPopulate(InstanceType model, org.ovirt.engine.core.common.businessentities.InstanceType entity) {
        MemoryPolicyHelper.setupMemoryBalloon(model, this);
        return model;
    }

    @Override
    public WatchdogsResource getWatchdogsResource() {
        return inject(new BackendTemplateWatchdogsResource(guid,
                VdcQueryType.GetWatchdog,
                new IdQueryParameters(guid)));
    }

    @Override
    public CreationResource getCreationResource(String oid) {
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

    @Override
    public GraphicsConsolesResource getGraphicsConsolesResource() {
        return inject(new BackendInstanceTypeGraphicsConsolesResource(guid));
    }

    protected class UpdateParametersProvider implements ParametersProvider<InstanceType, org.ovirt.engine.core.common.businessentities.InstanceType> {
        @Override
        public VdcActionParametersBase getParameters(InstanceType incoming, org.ovirt.engine.core.common.businessentities.InstanceType entity) {
            org.ovirt.engine.core.common.businessentities.InstanceType updated = getMapper(modelType, org.ovirt.engine.core.common.businessentities.InstanceType.class).map(incoming, entity);
            updated.setUsbPolicy(VmMapper.getUsbPolicyOnUpdate(incoming.getUsb(), entity.getUsbPolicy()));

            UpdateVmTemplateParameters updateParams = new UpdateVmTemplateParameters((VmTemplate) updated);
            if (incoming.isSetRngDevice()) {
                updateParams.setUpdateRngDevice(true);
                updateParams.setRngDevice(RngDeviceMapper.map(incoming.getRngDevice(), null));
            }
            if(incoming.isSetSoundcardEnabled()) {
                updateParams.setSoundDeviceEnabled(incoming.isSoundcardEnabled());
            }

            DisplayHelper.setGraphicsToParams(incoming.getDisplay(), updateParams);

            if (incoming.isSetMemoryPolicy() && incoming.getMemoryPolicy().isSetBallooning()) {
                updateParams.setBalloonEnabled(incoming.getMemoryPolicy().isBallooning());
            }

            return getMapper(modelType, UpdateVmTemplateParameters.class).map(incoming, updateParams);
        }
    }

}
