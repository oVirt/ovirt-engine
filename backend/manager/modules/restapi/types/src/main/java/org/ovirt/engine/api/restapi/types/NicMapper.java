package org.ovirt.engine.api.restapi.types;

import org.ovirt.engine.api.model.Mac;
import org.ovirt.engine.api.model.NIC;
import org.ovirt.engine.api.model.NicInterface;
import org.ovirt.engine.api.model.VM;
import org.ovirt.engine.api.model.VnicProfile;
import org.ovirt.engine.api.restapi.utils.GuidUtils;
import org.ovirt.engine.core.common.businessentities.network.VmNetworkInterface;

public class NicMapper {
    @Mapping(from = NIC.class, to = VmNetworkInterface.class)
    public static VmNetworkInterface map(NIC model, VmNetworkInterface template) {
        VmNetworkInterface entity = template != null ? template : new VmNetworkInterface();
        if (model.isSetVm() && model.getVm().isSetId()) {
            entity.setVmId(GuidUtils.asGuid(model.getVm().getId()));
        }
        if (model.isSetId()) {
            entity.setId(GuidUtils.asGuid(model.getId()));
        }
        if (model.isSetName()) {
            entity.setName(model.getName());
        }
        if (model.isSetMac() && model.getMac().isSetAddress()) {
            entity.setMacAddress(model.getMac().getAddress());
        }
        if (model.isSetLinked()) {
            entity.setLinked(model.isLinked());
        }
        if (model.isSetInterface()) {
            NicInterface nicType = NicInterface.fromValue(model.getInterface());
            if (nicType != null) {
                entity.setType(map(nicType));
            }
        }
        if (model.isSetPlugged()) {
            entity.setPlugged(model.isPlugged());
        }

        if (model.isSetVnicProfile()) {
            if (model.getVnicProfile().isSetId()) {
                entity.setVnicProfileId(GuidUtils.asGuid(model.getVnicProfile().getId()));
            } else {
                entity.setVnicProfileId(null);
            }
        }

        return entity;
    }

    @Mapping(from = VmNetworkInterface.class, to = NIC.class)
    public static NIC map(VmNetworkInterface entity, NIC template) {
        NIC model = template != null ? template : new NIC();

        if (entity.getVmId() != null) {
            model.setVm(new VM());
            model.getVm().setId(entity.getVmId().toString());
        }
        if (entity.getId() != null) {
            model.setId(entity.getId().toString());
        }
        if (entity.getName() != null) {
            model.setName(entity.getName());
        }
        if (entity.getMacAddress() != null) {
            model.setMac(new Mac());
            model.getMac().setAddress(entity.getMacAddress());
        }

        model.setLinked(entity.isLinked());
        model.setInterface(map(entity.getType()));
        model.setPlugged(entity.isPlugged());

        if (entity.getVnicProfileId() != null) {
            model.setVnicProfile(new VnicProfile());
            model.getVnicProfile().setId(entity.getVnicProfileId().toString());
        }

        return model;
    }

    @Mapping(from = NicInterface.class, to = Integer.class)
    public static Integer map(NicInterface type) {
        switch (type) {
        case RTL8139_VIRTIO:
            return 0;
        case RTL8139:
            return 1;
        case E1000:
            return 2;
        case VIRTIO:
            return 3;
        case SPAPR_VLAN:
            return 4;
        case PCI_PASSTHROUGH:
            return 5;
        default:
            return -1;
        }
    }

    @Mapping(from = Integer.class, to = String.class)
    public static String map(Integer type) {
        switch (type) {
        case 0:
            return NicInterface.RTL8139_VIRTIO.value();
        case 1:
            return NicInterface.RTL8139.value();
        case 2:
            return NicInterface.E1000.value();
        case 3:
            return NicInterface.VIRTIO.value();
        case 4:
            return NicInterface.SPAPR_VLAN.value();
        case 5:
            return NicInterface.PCI_PASSTHROUGH.value();
        default:
            return null;
        }
    }
}
