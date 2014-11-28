package org.ovirt.engine.api.restapi.resource;

import org.ovirt.engine.api.model.Console;
import org.ovirt.engine.api.model.InstanceType;
import org.ovirt.engine.api.model.InstanceTypes;
import org.ovirt.engine.api.model.VirtIOSCSI;
import org.ovirt.engine.api.resource.InstanceTypeResource;
import org.ovirt.engine.api.resource.InstanceTypesResource;
import org.ovirt.engine.api.restapi.types.RngDeviceMapper;
import org.ovirt.engine.api.restapi.util.DisplayHelper;
import org.ovirt.engine.api.restapi.util.VmHelper;
import org.ovirt.engine.core.common.action.AddVmTemplateParameters;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VmTemplateParametersBase;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VmEntityType;
import org.ovirt.engine.core.common.businessentities.VmRngDevice;
import org.ovirt.engine.core.common.businessentities.VmStatic;
import org.ovirt.engine.core.common.interfaces.SearchType;
import org.ovirt.engine.core.common.queries.GetVmTemplateParameters;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryParametersBase;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.Guid;

import java.util.List;
import javax.ws.rs.core.Response;

public class BackendInstanceTypesResource
    extends AbstractBackendCollectionResource<InstanceType, org.ovirt.engine.core.common.businessentities.InstanceType>
    implements InstanceTypesResource {

    static final String[] SUB_COLLECTIONS = {"nics", "watchdogs" };

    public BackendInstanceTypesResource() {
        super(InstanceType.class, org.ovirt.engine.core.common.businessentities.InstanceType.class, SUB_COLLECTIONS);
    }

    @Override
    public InstanceTypes list() {
        if (isFiltered()) {
            return mapCollection(getBackendCollection(VdcQueryType.GetAllInstanceTypes,
                    new VdcQueryParametersBase()));
        } else {
            return mapCollection(getBackendCollection(SearchType.InstanceType));
        }
    }

    @Override
    public Response add(InstanceType instanceType) {
        validateParameters(instanceType, "name");

        VmStatic vmStatic = getMapper(InstanceType.class, VmStatic.class).map(instanceType, new VmStatic());
        VM vm = new VM();
        vm.setStaticData(vmStatic);

        String name = instanceType.getName();
        String description = "";

        if (instanceType.isSetDescription()) {
            description = instanceType.getDescription();
        }

        vm.setVmDescription(description);

        AddVmTemplateParameters addInstanceTypeParameters =
                new AddVmTemplateParameters(vm, name, description);
        addInstanceTypeParameters.setTemplateType(VmEntityType.INSTANCE_TYPE);
        addInstanceTypeParameters.setVmTemplateId(null);
        addInstanceTypeParameters.setPublicUse(true);

        addInstanceTypeParameters.setConsoleEnabled(instanceType.getConsole() != null && instanceType.getConsole().isSetEnabled() ?
                        instanceType.getConsole().isEnabled() :
                        false);
        addInstanceTypeParameters.setVirtioScsiEnabled(instanceType.isSetVirtioScsi() && instanceType.getVirtioScsi().isSetEnabled() ?
                instanceType.getVirtioScsi().isEnabled() : null);

        if(instanceType.isSetSoundcardEnabled()) {
            addInstanceTypeParameters.setSoundDeviceEnabled(instanceType.isSoundcardEnabled());
        }


        return performCreate(VdcActionType.AddVmTemplate,
                               addInstanceTypeParameters,
                               new QueryIdResolver<Guid>(VdcQueryType.GetVmTemplate,
                                                   GetVmTemplateParameters.class));
    }

    @Override
    @SingleEntityResource
    public InstanceTypeResource getInstanceTypeSubResource(String id) {
        return inject(new BackendInstanceTypeResource(id));
    }

    @Override
    public Response performRemove(String id) {
        return performAction(VdcActionType.RemoveVmTemplate, new VmTemplateParametersBase(asGuid(id)));
    }

    protected InstanceTypes mapCollection(List<org.ovirt.engine.core.common.businessentities.InstanceType> entities) {
        InstanceTypes collection = new InstanceTypes();
        for (org.ovirt.engine.core.common.businessentities.InstanceType entity : entities) {
            InstanceType instanceType = map(entity);
            DisplayHelper.adjustDisplayData(this, instanceType);
            collection.getInstanceTypes().add(addLinks(populate(instanceType, entity)));
        }
        return collection;
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
        model.getVirtioScsi().setEnabled(!VmHelper.getVirtioScsiControllersForEntity(this, entity.getId()).isEmpty());
        model.setSoundcardEnabled(!VmHelper.getSoundDevicesForEntity(this, entity.getId()).isEmpty());
        List<VmRngDevice> rngDevices = getRngDevices(entity.getId());
        if (rngDevices != null && !rngDevices.isEmpty()) {
            model.setRngDevice(RngDeviceMapper.map(rngDevices.get(0), null));
        }

        return model;
    }

    private List<VmRngDevice> getRngDevices(Guid id) {
        return getEntity(List.class,
            VdcQueryType.GetRngDevice,
            new IdQueryParameters(id),
            "GetRngDevice", true);
    }

    private List<String> getConsoleDevicesForEntity(Guid id) {
        return getEntity(List.class,
                VdcQueryType.GetConsoleDevices,
                new IdQueryParameters(id),
                "GetConsoleDevices", true);
    }

}
