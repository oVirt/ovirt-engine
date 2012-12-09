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
            entity.setdescription(model.getDescription());
        }
        if (model.isSetCluster() && model.getCluster().getId() != null) {
            entity.setvds_group_id(new Guid(model.getCluster().getId()));
        }
        if (model.isSetHighAvailability()) {
            if (model.getHighAvailability().isSetEnabled()) {
                entity.setauto_startup(model.getHighAvailability().isEnabled());
            }
            if (model.getHighAvailability().isSetPriority()) {
                entity.setpriority(model.getHighAvailability().getPriority());
            }
        }
        if (model.isSetStateless()) {
            entity.setis_stateless(model.isStateless());
        }
        if (model.isSetDeleteProtected()) {
            entity.setDeleteProtected(model.isDeleteProtected());
        }
        if (model.isSetType()) {
            VmType vmType = VmType.fromValue(model.getType());
            if (vmType != null) {
                entity.setvm_type(VmMapper.map(vmType, null));
            }
        }
        if (model.isSetOrigin()) {
            entity.setorigin(VmMapper.map(model.getOrigin(), (OriginType)null));
        }
        if (model.isSetMemory()) {
            entity.setmem_size_mb((int)(model.getMemory() / BYTES_PER_MB));
        }
        if (model.isSetCpu() && model.getCpu().isSetTopology()) {
            if (model.getCpu().getTopology().getCores()!=null) {
                entity.setcpu_per_socket(model.getCpu().getTopology().getCores());
            }
            if (model.getCpu().getTopology().getSockets()!=null) {
                entity.setnum_of_sockets(model.getCpu().getTopology().getSockets());
            }
        }
        if (model.isSetOs()) {
            if (model.getOs().isSetType()) {
                OsType osType = OsType.fromValue(model.getOs().getType());
                if (osType != null) {
                    entity.setos(VmMapper.map(osType, null));
                 }
            }
            if (model.getOs().isSetBoot() && model.getOs().getBoot().size() > 0) {
                entity.setdefault_boot_sequence(VmMapper.map(model.getOs().getBoot(), null));
            }
            if (model.getOs().isSetKernel()) {
                entity.setkernel_url(model.getOs().getKernel());
            }
            if (model.getOs().isSetInitrd()) {
                entity.setinitrd_url(model.getOs().getInitrd());
            }
            if (model.getOs().isSetCmdline()) {
                entity.setkernel_params(model.getOs().getCmdline());
            }
        }
        if (model.isSetDisplay()) {
            if (model.getDisplay().isSetType()) {
                DisplayType displayType = DisplayType.fromValue(model.getDisplay().getType());
                if (displayType != null) {
                    entity.setdefault_display_type(VmMapper.map(displayType, null));
                }
            }
            if (model.getDisplay().isSetMonitors()) {
                entity.setnum_of_monitors(model.getDisplay().getMonitors());
            }
            if (model.getDisplay().isSetAllowOverride()) {
                entity.setAllowConsoleReconnect(model.getDisplay().isAllowOverride());
            }
            if (model.getDisplay().isSetSmartcardEnabled()) {
                entity.setSmartcardEnabled(model.getDisplay().isSmartcardEnabled());
            }
        }
        if (model.isSetDomain() && model.getDomain().isSetName()) {
            entity.setdomain(model.getDomain().getName());
        }
        if (model.isSetTimezone()) {
            entity.settime_zone(TimeZoneMapping.getWindows(model.getTimezone()));
        }
        return entity;
    }

    @Mapping(from = Template.class, to = VmStatic.class)
    public static VmStatic map(Template model, VmStatic incoming) {
        VmStatic staticVm = incoming != null ? incoming : new VmStatic();
        if (model.isSetName()) {
            staticVm.setvm_name(model.getName());
        }
        if (model.isSetId()) {
            staticVm.setId(new Guid(model.getId()));
        }
        if (model.isSetDescription()) {
            staticVm.setdescription(model.getDescription());
        }
        if (model.isSetCluster() && model.getCluster().getId() != null) {
            staticVm.setvds_group_id(new Guid(model.getCluster().getId()));
        }
        if (model.isSetHighAvailability()) {
            if (model.getHighAvailability().isSetEnabled()) {
                staticVm.setauto_startup(model.getHighAvailability().isEnabled());
            }
            if (model.getHighAvailability().isSetPriority()) {
                staticVm.setpriority(model.getHighAvailability().getPriority());
            }
        }
        if (model.isSetStateless()) {
            staticVm.setis_stateless(model.isStateless());
        }
        if (model.isSetDeleteProtected()) {
            staticVm.setDeleteProtected(model.isDeleteProtected());
        }
        if (model.isSetType()) {
            VmType vmType = VmType.fromValue(model.getType());
            if (vmType != null) {
                staticVm.setvm_type(VmMapper.map(vmType, null));
            }
        }
        if (model.isSetOrigin()) {
            staticVm.setorigin(VmMapper.map(model.getOrigin(), (OriginType)null));
        }
        if (model.isSetMemory()) {
            staticVm.setmem_size_mb((int)(model.getMemory() / BYTES_PER_MB));
        }
        if (model.isSetCpu() && model.getCpu().isSetTopology()) {
            if (model.getCpu().getTopology().getCores()!=null) {
                staticVm.setcpu_per_socket(model.getCpu().getTopology().getCores());
            }
            if (model.getCpu().getTopology().getSockets()!=null) {
                staticVm.setnum_of_sockets(model.getCpu().getTopology().getSockets());
            }
        }
        if (model.isSetOs()) {
            if (model.getOs().isSetType()) {
                OsType osType = OsType.fromValue(model.getOs().getType());
                if (osType != null) {
                    staticVm.setos(VmMapper.map(osType, null));
                 }
            }
            if (model.getOs().isSetBoot() && model.getOs().getBoot().size() > 0) {
                staticVm.setdefault_boot_sequence(VmMapper.map(model.getOs().getBoot(), null));
            }
            if (model.getOs().isSetKernel()) {
                staticVm.setkernel_url(model.getOs().getKernel());
            }
            if (model.getOs().isSetInitrd()) {
                staticVm.setinitrd_url(model.getOs().getInitrd());
            }
            if (model.getOs().isSetCmdline()) {
                staticVm.setkernel_params(model.getOs().getCmdline());
            }
        }
        if (model.isSetDisplay()) {
            if (model.getDisplay().isSetType()) {
                DisplayType displayType = DisplayType.fromValue(model.getDisplay().getType());
                if (displayType != null) {
                    staticVm.setdefault_display_type(VmMapper.map(displayType, null));
                }
            }
            if (model.getDisplay().isSetMonitors()) {
                staticVm.setnum_of_monitors(model.getDisplay().getMonitors());
            }
            if (model.getDisplay().isSetAllowOverride()) {
                staticVm.setAllowConsoleReconnect(model.getDisplay().isAllowOverride());
            }
            if (model.getDisplay().isSmartcardEnabled()) {
                staticVm.setSmartcardEnabled(model.getDisplay().isSmartcardEnabled());
            }
        }
        if (model.isSetDomain() && model.getDomain().isSetName()) {
            staticVm.setdomain(model.getDomain().getName());
        }
        if (model.isSetTimezone()) {
            staticVm.settime_zone(TimeZoneMapping.getWindows(model.getTimezone()));
        }
        return staticVm;
    }

    @Mapping(from = VmTemplate.class, to = Template.class)
    public static Template map(VmTemplate entity, Template incoming) {
        Template model = incoming != null ? incoming : new Template();
        model.setId(entity.getId().toString());
        model.setName(entity.getname());
        model.setDescription(entity.getdescription());
        model.setMemory((long)entity.getmem_size_mb() * BYTES_PER_MB);
        model.setHighAvailability(new HighAvailability());
        model.getHighAvailability().setEnabled(entity.getauto_startup());
        model.getHighAvailability().setPriority(entity.getpriority());
        model.setStateless(entity.getis_stateless());
        model.setDeleteProtected(entity.isDeleteProtected());
        if (entity.getvm_type() != null) {
            model.setType(VmMapper.map(entity.getvm_type(), null));
        }
        if (entity.getorigin() != null) {
            model.setOrigin(VmMapper.map(entity.getorigin(), null));
        }
        if (entity.getstatus() != null) {
            model.setStatus(StatusUtils.create(map(entity.getstatus(), null)));
        }
        if (entity.getos() != null ||
            entity.getdefault_boot_sequence() != null ||
            entity.getkernel_url() != null ||
            entity.getinitrd_url() != null ||
            entity.getkernel_params() != null) {
            OperatingSystem os = new OperatingSystem();
            if (entity.getos() != null) {
                OsType osType = VmMapper.map(entity.getos(), null);
                if (osType != null) {
                    os.setType(osType.value());
                }
            }
            if (entity.getdefault_boot_sequence() != null) {
                for (Boot boot : VmMapper.map(entity.getdefault_boot_sequence(), null)) {
                    os.getBoot().add(boot);
                }
            }
            os.setKernel(entity.getkernel_url());
            os.setInitrd(entity.getinitrd_url());
            os.setCmdline(entity.getkernel_params());
            model.setOs(os);
        }
        if (entity.getvds_group_id() != null) {
            Cluster cluster = new Cluster();
            cluster.setId(entity.getvds_group_id().toString());
            model.setCluster(cluster);
        }
        CpuTopology topology = new CpuTopology();
        topology.setSockets(entity.getnum_of_sockets());
        topology.setCores(entity.getnum_of_cpus() / entity.getnum_of_sockets());
        model.setCpu(new CPU());
        model.getCpu().setTopology(topology);
        if (entity.getdefault_display_type() != null) {
            model.setDisplay(new Display());
            model.getDisplay().setType(VmMapper.map(entity.getdefault_display_type(), null));
            model.getDisplay().setMonitors(entity.getnum_of_monitors());
            model.getDisplay().setAllowOverride(entity.getAllowConsoleReconnect());
            model.getDisplay().setSmartcardEnabled(entity.isSmartcardEnabled());
        }
        if (entity.getcreation_date() != null) {
            model.setCreationTime(DateMapper.map(entity.getcreation_date(), null));
        }
        if (entity.getdomain()!=null && !entity.getdomain().isEmpty()) {
            Domain domain = new Domain();
            domain.setName(entity.getdomain());
            model.setDomain(domain);
        }
        if (entity.getusb_policy()!=null) {
            Usb usb = new Usb();
            usb.setEnabled(UsbMapperUtils.getIsUsbEnabled(entity.getusb_policy()));
            UsbType usbType = UsbMapperUtils.getUsbType(entity.getusb_policy());
            if (usbType != null) {
                usb.setType(usbType.value());
            }
            model.setUsb(usb);
        }
        model.setTimezone(TimeZoneMapping.getJava(entity.gettime_zone()));
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
