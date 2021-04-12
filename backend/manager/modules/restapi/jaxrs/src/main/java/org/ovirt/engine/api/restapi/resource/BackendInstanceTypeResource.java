/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.restapi.resource;

import java.util.List;

import javax.ws.rs.core.Response;

import org.ovirt.engine.api.model.Console;
import org.ovirt.engine.api.model.InstanceType;
import org.ovirt.engine.api.model.VirtioScsi;
import org.ovirt.engine.api.resource.CreationResource;
import org.ovirt.engine.api.resource.InstanceTypeGraphicsConsolesResource;
import org.ovirt.engine.api.resource.InstanceTypeNicsResource;
import org.ovirt.engine.api.resource.InstanceTypeResource;
import org.ovirt.engine.api.resource.InstanceTypeWatchdogsResource;
import org.ovirt.engine.api.restapi.types.RngDeviceMapper;
import org.ovirt.engine.api.restapi.types.VmMapper;
import org.ovirt.engine.api.restapi.util.DisplayHelper;
import org.ovirt.engine.api.restapi.util.VmHelper;
import org.ovirt.engine.core.common.action.ActionParametersBase;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.UpdateVmTemplateParameters;
import org.ovirt.engine.core.common.action.VmTemplateManagementParameters;
import org.ovirt.engine.core.common.businessentities.VmRngDevice;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.queries.GetVmTemplateParameters;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.QueryType;
import org.ovirt.engine.core.compat.Guid;

public class BackendInstanceTypeResource
    extends AbstractBackendActionableResource<InstanceType, org.ovirt.engine.core.common.businessentities.InstanceType>
    implements InstanceTypeResource {

    public BackendInstanceTypeResource(String id) {
        super(id, InstanceType.class, org.ovirt.engine.core.common.businessentities.InstanceType.class);
    }

    @Override
    public InstanceType get() {
        InstanceType instanceType = performGet(QueryType.GetInstanceType, new GetVmTemplateParameters(guid));
        DisplayHelper.adjustDisplayData(this, instanceType);
        return instanceType;
    }

    @Override
    public InstanceType update(InstanceType incoming) {
        InstanceType instanceType = performUpdate(incoming,
                new QueryIdResolver<>(QueryType.GetInstanceType, GetVmTemplateParameters.class),
                ActionType.UpdateVmTemplate,
                new UpdateParametersProvider());

        if (instanceType != null) {
            DisplayHelper.adjustDisplayData(this, instanceType);
        }

        return instanceType;
    }

    @Override
    public Response remove() {
        get();
        return performAction(ActionType.RemoveVmTemplate, new VmTemplateManagementParameters(guid));
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
        model.setTpmEnabled(!VmHelper.getTpmDevicesForEntity(this, entity.getId()).isEmpty());
        setRngDevice(model);
        return model;
    }

    @Override
    public InstanceTypeNicsResource getNicsResource() {
        return inject(new BackendInstanceTypeNicsResource(guid));
    }

    protected InstanceType deprecatedPopulate(InstanceType model, org.ovirt.engine.core.common.businessentities.InstanceType entity) {
        return model;
    }

    @Override
    public InstanceTypeWatchdogsResource getWatchdogsResource() {
        return inject(new BackendInstanceTypeWatchdogsResource(guid));
    }

    @Override
    public CreationResource getCreationResource(String oid) {
        return inject(new BackendCreationResource(oid));
    }

    private void setRngDevice(InstanceType model) {
        List<VmRngDevice> rngDevices = getEntity(List.class,
            QueryType.GetRngDevice,
            new IdQueryParameters(Guid.createGuidFromString(model.getId())),
            "GetRngDevice", true);

        if (rngDevices != null && !rngDevices.isEmpty()) {
            model.setRngDevice(RngDeviceMapper.map(rngDevices.get(0), null));
        }
    }

    private List<String> getConsoleDevicesForEntity(Guid id) {
        return getEntity(List.class,
                QueryType.GetConsoleDevices,
                new IdQueryParameters(id),
                "GetConsoleDevices", true);
    }

    @Override
    public InstanceTypeGraphicsConsolesResource getGraphicsConsolesResource() {
        return inject(new BackendInstanceTypeGraphicsConsolesResource(guid));
    }

    protected class UpdateParametersProvider implements ParametersProvider<InstanceType, org.ovirt.engine.core.common.businessentities.InstanceType> {
        @Override
        public ActionParametersBase getParameters(InstanceType incoming, org.ovirt.engine.core.common.businessentities.InstanceType entity) {
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
            if (incoming.isSetVirtioScsi()) {
                if (incoming.getVirtioScsi().isSetEnabled()) {
                    updateParams.setVirtioScsiEnabled(incoming.getVirtioScsi().isEnabled());
                }
            }
            if (incoming.isSetTpmEnabled()) {
                updateParams.setTpmEnabled(incoming.isTpmEnabled());
            }

            DisplayHelper.setGraphicsToParams(incoming.getDisplay(), updateParams);

            return getMapper(modelType, UpdateVmTemplateParameters.class).map(incoming, updateParams);
        }
    }

}
