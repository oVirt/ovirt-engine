package org.ovirt.engine.api.restapi.types;

import java.util.ArrayList;
import java.util.List;

import org.ovirt.engine.api.model.Host;
import org.ovirt.engine.api.model.HostDevice;
import org.ovirt.engine.api.model.MDevType;
import org.ovirt.engine.api.model.MDevTypes;
import org.ovirt.engine.api.model.Product;
import org.ovirt.engine.api.model.Vendor;
import org.ovirt.engine.api.model.Vm;
import org.ovirt.engine.api.restapi.utils.HexUtils;
import org.ovirt.engine.core.common.businessentities.HostDeviceView;
import org.ovirt.engine.core.compat.Guid;

public class HostDeviceMapper {

    @Mapping(from = org.ovirt.engine.core.common.businessentities.HostDevice.class, to = HostDevice.class)
    public static HostDevice map(org.ovirt.engine.core.common.businessentities.HostDevice entity, HostDevice model) {
        if (model == null) {
            model = new HostDevice();
        }

        if (!model.isSetHost()) {
            model.setHost(new Host());
        }
        model.getHost().setId(entity.getHostId().toString());

        model.setId(HexUtils.string2hex(entity.getDeviceName()));
        model.setName(entity.getDeviceName());
        model.setCapability(entity.getCapability());
        model.setParentDevice(getSameHostDeviceReference(entity.getHostId(), entity.getParentDeviceName()));
        model.setDriver(entity.getDriver());

        if (entity.getMdevTypes() != null && !entity.getMdevTypes().isEmpty()) {
            List<MDevType> mDevsList = new ArrayList<>();
            for (org.ovirt.engine.core.common.businessentities.MDevType mDevEntity : entity.getMdevTypes()) {
                MDevType mDev = new MDevType();
                mDev.setName(mDevEntity.getName());
                mDev.setHumanReadableName(mDevEntity.getHumanReadableName());
                mDev.setAvailableInstances(mDevEntity.getAvailableInstances());
                mDev.setDescription(mDevEntity.getDescription());
                mDevsList.add(mDev);
            }

            MDevTypes mDevTypes = new MDevTypes();
            mDevTypes.getMDevTypes().addAll(mDevsList);
            model.setMDevTypes(mDevTypes);
        }

        if (entity.getProductId() != null || entity.getProductName() != null) {
            if (!model.isSetProduct()) {
                model.setProduct(new Product());
            }
            model.getProduct().setId(entity.getProductId());
            model.getProduct().setName(entity.getProductName());
        }
        if (entity.getVendorId() != null || entity.getVendorName() != null) {
            if (!model.isSetVendor()) {
                model.setVendor(new Vendor());
            }
            model.getVendor().setId(entity.getVendorId());
            model.getVendor().setName(entity.getVendorName());
        }
        model.setIommuGroup(entity.getIommuGroup());

        if (entity.getParentPhysicalFunction() != null) {
            model.setPhysicalFunction(getSameHostDeviceReference(entity.getHostId(), entity.getParentPhysicalFunction()));
        }

        model.setVirtualFunctions(entity.getTotalVirtualFunctions());
        if (entity.getVmId() != null) {
            model.setVm(new Vm());
            model.getVm().setId(entity.getVmId().toString());
        }
        return model;
    }

    @Mapping(from = HostDeviceView.class, to = HostDevice.class)
    public static HostDevice map(HostDeviceView entity, HostDevice model) {
        model = map((org.ovirt.engine.core.common.businessentities.HostDevice) entity, model);

        if (entity.isIommuPlaceholder()) {
            model.setPlaceholder(true);
        }

        return model;
    }

    private static HostDevice getSameHostDeviceReference(Guid hostId, String deviceName) {
        HostDevice device = new HostDevice();
        device.setHost(new Host());
        device.getHost().setId(hostId.toString());
        device.setId(HexUtils.string2hex(deviceName));
        device.setName(deviceName);

        return device;
    }
}
