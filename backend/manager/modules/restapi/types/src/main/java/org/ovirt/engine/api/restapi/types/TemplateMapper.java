package org.ovirt.engine.api.restapi.types;

import org.ovirt.engine.api.common.util.StatusUtils;
import org.ovirt.engine.api.common.util.TimeZoneMapping;
import org.ovirt.engine.api.model.Boot;
import org.ovirt.engine.api.model.CPU;
import org.ovirt.engine.api.model.Cluster;
import org.ovirt.engine.api.model.CpuTopology;
import org.ovirt.engine.api.model.Display;
import org.ovirt.engine.api.model.DisplayType;
import org.ovirt.engine.api.model.Domain;
import org.ovirt.engine.api.model.HighAvailability;
import org.ovirt.engine.api.model.OperatingSystem;
import org.ovirt.engine.api.model.OsType;
import org.ovirt.engine.api.model.Template;
import org.ovirt.engine.api.model.TemplateStatus;
import org.ovirt.engine.api.model.Usb;
import org.ovirt.engine.api.model.UsbType;
import org.ovirt.engine.api.model.VmType;
import org.ovirt.engine.api.restapi.utils.UsbMapperUtils;
import org.ovirt.engine.core.common.businessentities.OriginType;
import org.ovirt.engine.core.common.businessentities.VmStatic;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.businessentities.VmTemplateStatus;
import org.ovirt.engine.core.compat.Guid;

public class TemplateMapper {

    private static final int BYTES_PER_MB = 1024 * 1024;

    @Mapping(from = Template.class, to = VmTemplate.class)
    public static VmTemplate map(Template model, VmTemplate incoming) {
        VmTemplate entity = incoming != null ? incoming : new VmTemplate();
        if (model.isSetName()) {
            entity.setname(model.getName());
        }
        if (model.isSetId()) {
            entity.setId(new Guid(model.getId()));
        }
        if (model.isSetDescription()) {
            entity.setDescription(model.getDescription());
        }
        if (model.isSetCluster() && model.getCluster().getId() != null) {
            entity.setVdsGroupId(new Guid(model.getCluster().getId()));
        }
        if (model.isSetHighAvailability()) {
            if (model.getHighAvailability().isSetEnabled()) {
                entity.setAutoStartup(model.getHighAvailability().isEnabled());
            }
            if (model.getHighAvailability().isSetPriority()) {
                entity.setPriority(model.getHighAvailability().getPriority());
            }
        }
        if (model.isSetStateless()) {
            entity.setStateless(model.isStateless());
        }
        if (model.isSetDeleteProtected()) {
            entity.setDeleteProtected(model.isDeleteProtected());
        }
        if (model.isSetType()) {
            VmType vmType = VmType.fromValue(model.getType());
            if (vmType != null) {
                entity.setVmType(VmMapper.map(vmType, null));
            }
        }
        if (model.isSetOrigin()) {
            entity.setOrigin(VmMapper.map(model.getOrigin(), (OriginType)null));
        }
        if (model.isSetMemory()) {
            entity.setMemSizeMb((int)(model.getMemory() / BYTES_PER_MB));
        }
        if (model.isSetCpu() && model.getCpu().isSetTopology()) {
            if (model.getCpu().getTopology().getCores()!=null) {
                entity.setCpuPerSocket(model.getCpu().getTopology().getCores());
            }
            if (model.getCpu().getTopology().getSockets()!=null) {
                entity.setNumOfSockets(model.getCpu().getTopology().getSockets());
            }
        }
        if (model.isSetOs()) {
            if (model.getOs().isSetType()) {
                OsType osType = OsType.fromValue(model.getOs().getType());
                if (osType != null) {
                    entity.setOs(VmMapper.map(osType, null));
                 }
            }
            if (model.getOs().isSetBoot() && model.getOs().getBoot().size() > 0) {
                entity.setDefaultBootSequence(VmMapper.map(model.getOs().getBoot(), null));
            }
            if (model.getOs().isSetKernel()) {
                entity.setKernelUrl(model.getOs().getKernel());
            }
            if (model.getOs().isSetInitrd()) {
                entity.setInitrdUrl(model.getOs().getInitrd());
            }
            if (model.getOs().isSetCmdline()) {
                entity.setKernelParams(model.getOs().getCmdline());
            }
        }
        if (model.isSetDisplay()) {
            if (model.getDisplay().isSetType()) {
                DisplayType displayType = DisplayType.fromValue(model.getDisplay().getType());
                if (displayType != null) {
                    entity.setDefaultDisplayType(VmMapper.map(displayType, null));
                }
            }
            if (model.getDisplay().isSetMonitors()) {
                entity.setNumOfMonitors(model.getDisplay().getMonitors());
            }
            if (model.getDisplay().isSetAllowOverride()) {
                entity.setAllowConsoleReconnect(model.getDisplay().isAllowOverride());
            }
            if (model.getDisplay().isSetSmartcardEnabled()) {
                entity.setSmartcardEnabled(model.getDisplay().isSmartcardEnabled());
            }
        }
        if (model.isSetDomain() && model.getDomain().isSetName()) {
            entity.setDomain(model.getDomain().getName());
        }
        if (model.isSetTimezone()) {
            entity.setTimeZone(TimeZoneMapping.getWindows(model.getTimezone()));
        }
        if (model.isSetTunnelMigration()) {
            entity.setTunnelMigration(model.isTunnelMigration());
        }
        return entity;
    }

    @Mapping(from = Template.class, to = VmStatic.class)
    public static VmStatic map(Template model, VmStatic incoming) {
        VmStatic staticVm = incoming != null ? incoming : new VmStatic();
        if (model.isSetName()) {
            staticVm.setVmName(model.getName());
        }
        if (model.isSetId()) {
            staticVm.setId(new Guid(model.getId()));
        }
        if (model.isSetDescription()) {
            staticVm.setDescription(model.getDescription());
        }
        if (model.isSetCluster() && model.getCluster().getId() != null) {
            staticVm.setVdsGroupId(new Guid(model.getCluster().getId()));
        }
        if (model.isSetHighAvailability()) {
            if (model.getHighAvailability().isSetEnabled()) {
                staticVm.setAutoStartup(model.getHighAvailability().isEnabled());
            }
            if (model.getHighAvailability().isSetPriority()) {
                staticVm.setPriority(model.getHighAvailability().getPriority());
            }
        }
        if (model.isSetStateless()) {
            staticVm.setStateless(model.isStateless());
        }
        if (model.isSetDeleteProtected()) {
            staticVm.setDeleteProtected(model.isDeleteProtected());
        }
        if (model.isSetType()) {
            VmType vmType = VmType.fromValue(model.getType());
            if (vmType != null) {
                staticVm.setVmType(VmMapper.map(vmType, null));
            }
        }
        if (model.isSetOrigin()) {
            staticVm.setOrigin(VmMapper.map(model.getOrigin(), (OriginType)null));
        }
        if (model.isSetMemory()) {
            staticVm.setMemSizeMb((int)(model.getMemory() / BYTES_PER_MB));
        }
        if (model.isSetCpu() && model.getCpu().isSetTopology()) {
            if (model.getCpu().getTopology().getCores()!=null) {
                staticVm.setCpuPerSocket(model.getCpu().getTopology().getCores());
            }
            if (model.getCpu().getTopology().getSockets()!=null) {
                staticVm.setNumOfSockets(model.getCpu().getTopology().getSockets());
            }
        }
        if (model.isSetOs()) {
            if (model.getOs().isSetType()) {
                OsType osType = OsType.fromValue(model.getOs().getType());
                if (osType != null) {
                    staticVm.setOs(VmMapper.map(osType, null));
                 }
            }
            if (model.getOs().isSetBoot() && model.getOs().getBoot().size() > 0) {
                staticVm.setDefaultBootSequence(VmMapper.map(model.getOs().getBoot(), null));
            }
            if (model.getOs().isSetKernel()) {
                staticVm.setKernelUrl(model.getOs().getKernel());
            }
            if (model.getOs().isSetInitrd()) {
                staticVm.setInitrdUrl(model.getOs().getInitrd());
            }
            if (model.getOs().isSetCmdline()) {
                staticVm.setKernelParams(model.getOs().getCmdline());
            }
        }
        if (model.isSetDisplay()) {
            if (model.getDisplay().isSetType()) {
                DisplayType displayType = DisplayType.fromValue(model.getDisplay().getType());
                if (displayType != null) {
                    staticVm.setDefaultDisplayType(VmMapper.map(displayType, null));
                }
            }
            if (model.getDisplay().isSetMonitors()) {
                staticVm.setNumOfMonitors(model.getDisplay().getMonitors());
            }
            if (model.getDisplay().isSetAllowOverride()) {
                staticVm.setAllowConsoleReconnect(model.getDisplay().isAllowOverride());
            }
            if (model.getDisplay().isSmartcardEnabled()) {
                staticVm.setSmartcardEnabled(model.getDisplay().isSmartcardEnabled());
            }
        }
        if (model.isSetDomain() && model.getDomain().isSetName()) {
            staticVm.setDomain(model.getDomain().getName());
        }
        if (model.isSetTimezone()) {
            staticVm.setTimeZone(TimeZoneMapping.getWindows(model.getTimezone()));
        }
        if (model.isSetTunnelMigration()) {
            staticVm.setTunnelMigration(model.isTunnelMigration());
        }
        return staticVm;
    }

    @Mapping(from = VmTemplate.class, to = Template.class)
    public static Template map(VmTemplate entity, Template incoming) {
        Template model = incoming != null ? incoming : new Template();
        model.setId(entity.getId().toString());
        model.setName(entity.getname());
        model.setDescription(entity.getDescription());
        model.setMemory((long)entity.getMemSizeMb() * BYTES_PER_MB);
        model.setHighAvailability(new HighAvailability());
        model.getHighAvailability().setEnabled(entity.isAutoStartup());
        model.getHighAvailability().setPriority(entity.getPriority());
        model.setStateless(entity.isStateless());
        model.setDeleteProtected(entity.isDeleteProtected());
        if (entity.getVmType() != null) {
            model.setType(VmMapper.map(entity.getVmType(), null));
        }
        if (entity.getOrigin() != null) {
            model.setOrigin(VmMapper.map(entity.getOrigin(), null));
        }
        if (entity.getstatus() != null) {
            model.setStatus(StatusUtils.create(map(entity.getstatus(), null)));
        }
        if (entity.getOs() != null ||
            entity.getDefaultBootSequence() != null ||
            entity.getKernelUrl() != null ||
            entity.getInitrdUrl() != null ||
            entity.getKernelParams() != null) {
            OperatingSystem os = new OperatingSystem();
            if (entity.getOs() != null) {
                OsType osType = VmMapper.map(entity.getOs(), null);
                if (osType != null) {
                    os.setType(osType.value());
                }
            }
            if (entity.getDefaultBootSequence() != null) {
                for (Boot boot : VmMapper.map(entity.getDefaultBootSequence(), null)) {
                    os.getBoot().add(boot);
                }
            }
            os.setKernel(entity.getKernelUrl());
            os.setInitrd(entity.getInitrdUrl());
            os.setCmdline(entity.getKernelParams());
            model.setOs(os);
        }
        if (entity.getVdsGroupId() != null) {
            Cluster cluster = new Cluster();
            cluster.setId(entity.getVdsGroupId().toString());
            model.setCluster(cluster);
        }
        CpuTopology topology = new CpuTopology();
        topology.setSockets(entity.getNumOfSockets());
        topology.setCores(entity.getNumOfCpus() / entity.getNumOfSockets());
        model.setCpu(new CPU());
        model.getCpu().setTopology(topology);
        if (entity.getDefaultDisplayType() != null) {
            model.setDisplay(new Display());
            model.getDisplay().setType(VmMapper.map(entity.getDefaultDisplayType(), null));
            model.getDisplay().setMonitors(entity.getNumOfMonitors());
            model.getDisplay().setAllowOverride(entity.isAllowConsoleReconnect());
            model.getDisplay().setSmartcardEnabled(entity.isSmartcardEnabled());
        }
        if (entity.getCreationDate() != null) {
            model.setCreationTime(DateMapper.map(entity.getCreationDate(), null));
        }
        if (entity.getDomain()!=null && !entity.getDomain().isEmpty()) {
            Domain domain = new Domain();
            domain.setName(entity.getDomain());
            model.setDomain(domain);
        }
        if (entity.getUsbPolicy()!=null) {
            Usb usb = new Usb();
            usb.setEnabled(UsbMapperUtils.getIsUsbEnabled(entity.getUsbPolicy()));
            UsbType usbType = UsbMapperUtils.getUsbType(entity.getUsbPolicy());
            if (usbType != null) {
                usb.setType(usbType.value());
            }
            model.setUsb(usb);
        }
        model.setTimezone(TimeZoneMapping.getJava(entity.getTimeZone()));
        model.setTunnelMigration(entity.getTunnelMigration());
        return model;
    }

    @Mapping(from = VmTemplateStatus.class, to = TemplateStatus.class)
    public static TemplateStatus map(VmTemplateStatus entityStatus, TemplateStatus incoming) {
        switch (entityStatus) {
        case OK:
            return TemplateStatus.OK;
        case Locked:
            return TemplateStatus.LOCKED;
        case Illegal:
            return TemplateStatus.ILLEGAL;
        default:
            return null;
        }
    }
}
