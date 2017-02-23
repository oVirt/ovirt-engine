package org.ovirt.engine.api.restapi.types;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.api.model.Cluster;
import org.ovirt.engine.api.model.Display;
import org.ovirt.engine.api.model.VmPool;
import org.ovirt.engine.api.model.VmPoolType;
import org.ovirt.engine.api.restapi.utils.GuidUtils;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VmStatic;

public class VmPoolMapper {

    @Mapping(from = VmPool.class, to = org.ovirt.engine.core.common.businessentities.VmPool.class)
    public static org.ovirt.engine.core.common.businessentities.VmPool map(VmPool model,
            org.ovirt.engine.core.common.businessentities.VmPool template) {
        org.ovirt.engine.core.common.businessentities.VmPool entity =
                template != null ? template : new org.ovirt.engine.core.common.businessentities.VmPool();
        if (model.isSetId()) {
            entity.setVmPoolId(GuidUtils.asGuid(model.getId()));
        }
        if (model.isSetName()) {
            entity.setName(model.getName());
        }
        if (model.isSetDescription()) {
            entity.setVmPoolDescription(model.getDescription());
        }
        if (model.isSetComment()) {
            entity.setComment(model.getComment());
        }
        if (model.isSetSize()) {
            entity.setAssignedVmsCount(model.getSize());
        }
        if (model.isSetCluster()) {
            if (model.getCluster().isSetId()) {
                entity.setClusterId(GuidUtils.asGuid(model.getCluster().getId()));
            } else if (model.getCluster().isSetName()) {
                entity.setClusterName(model.getCluster().getName());
            }
        }
        if (model.isSetPrestartedVms()) {
            entity.setPrestartedVms(model.getPrestartedVms());
        }
        if (model.isSetMaxUserVms()) {
            entity.setMaxAssignedVmsPerUser(model.getMaxUserVms());
        }
        if (model.isSetDisplay() && model.getDisplay().isSetProxy()) {
            entity.setSpiceProxy("".equals(model.getDisplay().getProxy()) ? null : model.getDisplay().getProxy());
        }
        if (model.isSetType()) {
            entity.setVmPoolType(map(model.getType(), null));
        }
        if (model.isSetStateful()) {
            entity.setStateful(model.isStateful());
        }
        if (model.isSetAutoStorageSelect()) {
            entity.setAutoStorageSelect(model.isAutoStorageSelect());
        }
        return entity;
    }

    @Mapping(from = VmPool.class, to = VM.class)
    public static VM map(VmPool model, VM template) {
        VM entity = template != null ? template : new VM();
        entity.setStaticData(map(model, entity.getStaticData()));
        return entity;
    }

    @Mapping(from = VmPool.class, to = VmStatic.class)
    public static VmStatic map(VmPool model, VmStatic template) {
        VmStatic entity = template != null ? template : new VmStatic();
        if (model.getVm() != null) {
            entity = VmMapper.map(model.getVm(), entity);
        }
        entity.setName(model.getName());
        entity.setDescription(model.getDescription());
        if (model.isSetTemplate() &&
                model.getTemplate().isSetId()) {
            entity.setVmtGuid(GuidUtils.asGuid(model.getTemplate().getId()));
        }
        if (model.isSetCluster() &&
                model.getCluster().isSetId()) {
            entity.setClusterId(GuidUtils.asGuid(model.getCluster().getId()));
        }
        if (model.isSetUseLatestTemplateVersion()) {
            entity.setUseLatestVersion(model.isUseLatestTemplateVersion());
        }
        return entity;
    }

    @Mapping(from = org.ovirt.engine.core.common.businessentities.VmPool.class, to = VmPool.class)
    public static VmPool map(org.ovirt.engine.core.common.businessentities.VmPool entity, VmPool template) {
        VmPool model = template != null ? template : new VmPool();
        model.setId(entity.getVmPoolId().toString());
        model.setName(entity.getName());
        model.setDescription(entity.getVmPoolDescription());
        model.setComment(entity.getComment());
        model.setSize(entity.getAssignedVmsCount());
        model.setPrestartedVms(entity.getPrestartedVms());
        if (entity.getClusterId() != null ||
            entity.getClusterName() != null) {
            model.setCluster(new Cluster());
            model.getCluster().setId(entity.getClusterId().toString());
        }
        model.setMaxUserVms(entity.getMaxAssignedVmsPerUser());
        if (StringUtils.isNotBlank(entity.getSpiceProxy())) {
            Display display = new Display();
            display.setProxy(entity.getSpiceProxy());
            model.setDisplay(display);
        }
        model.setType(map(entity.getVmPoolType(), null));
        model.setStateful(entity.isStateful());
        model.setAutoStorageSelect(entity.isAutoStorageSelect());

        return model;
    }

    @Mapping(from = VM.class, to = VmPool.class)
    public static VmPool map(VM vm, VmPool template) {
        VmPool model = template != null ? template : new VmPool();
        org.ovirt.engine.api.model.Vm vmModel = VmMapper.map(vm, (org.ovirt.engine.api.model.Vm) null);
        vmModel.setCluster(null);
        vmModel.setTemplate(null);
        vmModel.setVmPool(null);
        model.setVm(vmModel);
        model.setUseLatestTemplateVersion(vm.isUseLatestVersion());
        return model;
    }

    @Mapping(from = VmPoolType.class, to = org.ovirt.engine.core.common.businessentities.VmPoolType.class)
    public static org.ovirt.engine.core.common.businessentities.VmPoolType map(
            VmPoolType vmPoolType, org.ovirt.engine.core.common.businessentities.VmPoolType incoming) {
        switch (vmPoolType) {
            case AUTOMATIC:
                return org.ovirt.engine.core.common.businessentities.VmPoolType.AUTOMATIC;
            case MANUAL:
                return org.ovirt.engine.core.common.businessentities.VmPoolType.MANUAL;
            default:
                return null;
        }
    }

    @Mapping(from = org.ovirt.engine.core.common.businessentities.VmPoolType.class, to = VmPoolType.class)
    public static VmPoolType map(org.ovirt.engine.core.common.businessentities.VmPoolType vmPoolType, VmPoolType incoming) {
        if (vmPoolType == null) {
            return null;
        }
        switch (vmPoolType) {
            case AUTOMATIC:
                return VmPoolType.AUTOMATIC;
            case MANUAL:
                return VmPoolType.MANUAL;
            default:
                return null;
        }
    }

}
