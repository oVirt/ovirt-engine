package org.ovirt.engine.api.restapi.types;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.api.common.util.StatusUtils;
import org.ovirt.engine.api.model.Architecture;
import org.ovirt.engine.api.model.Bios;
import org.ovirt.engine.api.model.Boot;
import org.ovirt.engine.api.model.BootMenu;
import org.ovirt.engine.api.model.CPU;
import org.ovirt.engine.api.model.Cluster;
import org.ovirt.engine.api.model.CpuProfile;
import org.ovirt.engine.api.model.CpuTopology;
import org.ovirt.engine.api.model.Display;
import org.ovirt.engine.api.model.DisplayType;
import org.ovirt.engine.api.model.Domain;
import org.ovirt.engine.api.model.HighAvailability;
import org.ovirt.engine.api.model.OperatingSystem;
import org.ovirt.engine.api.model.Template;
import org.ovirt.engine.api.model.TemplateStatus;
import org.ovirt.engine.api.model.TemplateVersion;
import org.ovirt.engine.api.model.Usb;
import org.ovirt.engine.api.model.UsbType;
import org.ovirt.engine.api.model.VmType;
import org.ovirt.engine.api.restapi.utils.GuidUtils;
import org.ovirt.engine.api.restapi.utils.UsbMapperUtils;
import org.ovirt.engine.core.common.action.UpdateVmTemplateParameters;
import org.ovirt.engine.core.common.businessentities.OriginType;
import org.ovirt.engine.core.common.businessentities.VmInit;
import org.ovirt.engine.core.common.businessentities.VmStatic;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.businessentities.VmTemplateStatus;
import org.ovirt.engine.core.common.osinfo.OsRepository;
import org.ovirt.engine.core.common.utils.SimpleDependecyInjector;
import org.ovirt.engine.core.compat.Guid;

import static org.ovirt.engine.api.restapi.types.IntegerMapper.mapNullToMinusOne;
import static org.ovirt.engine.api.restapi.types.IntegerMapper.mapMinusOneToNull;

public class TemplateMapper {

    private static final int BYTES_PER_MB = 1024 * 1024;

    @Mapping(from = Template.class, to = VmTemplate.class)
    public static VmTemplate map(Template model, VmTemplate incoming) {
        VmTemplate entity = incoming != null ? incoming : new VmTemplate();
        if (model.isSetName()) {
            entity.setName(model.getName());
        }
        if (model.isSetId()) {
            entity.setId(GuidUtils.asGuid(model.getId()));
        }
        if (model.isSetDescription()) {
            entity.setDescription(model.getDescription());
        }
        if (model.isSetComment()) {
            entity.setComment(model.getComment());
        }
        if (model.isSetCluster() && model.getCluster().getId() != null) {
            entity.setVdsGroupId(GuidUtils.asGuid(model.getCluster().getId()));
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
        if (model.isSetSso() && model.getSso().isSetMethods()) {
            entity.setSsoMethod(SsoMapper.map(model.getSso(), null));
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
        if (model.isSetCpu() && model.getCpu().isSetArchitecture()) {
            Architecture archType = Architecture.fromValue(model.getCpu().getArchitecture());

            if (archType != null) {
                entity.setClusterArch(CPUMapper.map(archType, null));
            }
        }
        if (model.isSetBios()) {
            if (model.getBios().isSetBootMenu()) {
                entity.setBootMenuEnabled(model.getBios().getBootMenu().isEnabled());
            }
        }
        if (model.isSetCpuShares()) {
            entity.setCpuShares(model.getCpuShares());
        }
        if (model.isSetOs()) {
            if (model.getOs().isSetType()) {
                entity.setOsId(VmMapper.mapOsType(model.getOs().getType()));
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
            if (model.getDisplay().isSetSingleQxlPci()) {
                entity.setSingleQxlPci(model.getDisplay().isSingleQxlPci());
            }
            if (model.getDisplay().isSetAllowOverride()) {
                entity.setAllowConsoleReconnect(model.getDisplay().isAllowOverride());
            }
            if (model.getDisplay().isSetSmartcardEnabled()) {
                entity.setSmartcardEnabled(model.getDisplay().isSmartcardEnabled());
            }
            if (model.getDisplay().isSetKeyboardLayout()) {
                String layout = model.getDisplay().getKeyboardLayout();
                if (layout.isEmpty()) {
                    layout = null;  // uniquely represent unset keyboard layout as null
                }
                entity.setVncKeyboardLayout(layout);
            }
            if (model.getDisplay().isSetFileTransferEnabled()) {
                entity.setSpiceFileTransferEnabled(model.getDisplay().isFileTransferEnabled());
            }
            if (model.getDisplay().isSetCopyPasteEnabled()) {
                entity.setSpiceCopyPasteEnabled(model.getDisplay().isCopyPasteEnabled());
            }
        }
        if (model.isSetDomain() && model.getDomain().isSetName()) {
            if (entity.getVmInit() == null) {
                entity.setVmInit(new VmInit());
            }
            entity.getVmInit().setDomain(model.getDomain().getName());
        }
        if (model.isSetTimezone()) {
            String timezone = model.getTimezone();
            if (timezone.isEmpty()) {
                timezone = null;  // normalize default timezone representation
            }
            entity.setTimeZone(timezone);
        }
        if (model.isSetTunnelMigration()) {
            entity.setTunnelMigration(model.isTunnelMigration());
        }
        if (model.isSetMigrationDowntime()) {
            entity.setMigrationDowntime(mapMinusOneToNull(model.getMigrationDowntime()));
        }
        if (model.getVersion() != null) {
            if (model.getVersion().getBaseTemplate() != null
                    && StringUtils.isNotEmpty(model.getVersion().getBaseTemplate().getId())) {
                entity.setBaseTemplateId(Guid.createGuidFromString(model.getVersion().getBaseTemplate().getId()));
            }
            if (model.getVersion().isSetVersionName()) {
                entity.setTemplateVersionName(model.getVersion().getVersionName());
            }
            // numbering is generated in the backend, hence even if user specified version number, we ignore it.
        }

        if (model.isSetSerialNumber()) {
            SerialNumberMapper.copySerialNumber(model.getSerialNumber(), entity);
        }

        if (model.isSetCpuProfile() && model.getCpuProfile().isSetId()) {
            entity.setCpuProfileId(GuidUtils.asGuid(model.getCpuProfile().getId()));
        }

        return entity;
    }

    @Mapping(from = Template.class, to = VmStatic.class)
    public static VmStatic map(Template model, VmStatic incoming) {
        VmStatic staticVm = incoming != null ? incoming : new VmStatic();
        if (model.isSetName()) {
            staticVm.setName(model.getName());
        }
        if (model.isSetId()) {
            staticVm.setId(GuidUtils.asGuid(model.getId()));
        }
        if (model.isSetDescription()) {
            staticVm.setDescription(model.getDescription());
        }
        if (model.isSetComment()) {
            staticVm.setComment(model.getComment());
        }
        if (model.isSetCluster() && model.getCluster().getId() != null) {
            staticVm.setVdsGroupId(GuidUtils.asGuid(model.getCluster().getId()));
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
        if (model.isSetSso() && model.getSso().isSetMethods()) {
            staticVm.setSsoMethod(SsoMapper.map(model.getSso(), null));
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
        if (model.isSetCpuShares()) {
            staticVm.setCpuShares(model.getCpuShares());
        }
        if (model.isSetOs()) {
            if (model.getOs().isSetType()) {
                staticVm.setOsId(VmMapper.mapOsType(model.getOs().getType()));
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
            if (model.getDisplay().isSetSingleQxlPci()) {
                staticVm.setSingleQxlPci(model.getDisplay().isSingleQxlPci());
            }
            if (model.getDisplay().isSetAllowOverride()) {
                staticVm.setAllowConsoleReconnect(model.getDisplay().isAllowOverride());
            }
            if (model.getDisplay().isSmartcardEnabled()) {
                staticVm.setSmartcardEnabled(model.getDisplay().isSmartcardEnabled());
            }
            if (model.getDisplay().isSetKeyboardLayout()) {
                String layout = model.getDisplay().getKeyboardLayout();
                if (layout.isEmpty()) {
                    layout = null;  // uniquely represent unset keyboard layout as null
                }
                staticVm.setVncKeyboardLayout(layout);
            }
            if (model.getDisplay().isSetFileTransferEnabled()) {
                staticVm.setSpiceFileTransferEnabled(model.getDisplay().isFileTransferEnabled());
            }
            if (model.getDisplay().isSetCopyPasteEnabled()) {
                staticVm.setSpiceCopyPasteEnabled(model.getDisplay().isCopyPasteEnabled());
            }
        }
        if (model.isSetDomain() && model.getDomain().isSetName()) {
            if (staticVm.getVmInit() == null) {
                staticVm.setVmInit(new VmInit());
            }
            staticVm.getVmInit().setDomain(model.getDomain().getName());
        }
        if (model.isSetTimezone()) {
            staticVm.setTimeZone(model.getTimezone());
        }
        if (model.isSetTunnelMigration()) {
            staticVm.setTunnelMigration(model.isTunnelMigration());
        }
        if (model.isSetMigrationDowntime()) {
            staticVm.setMigrationDowntime(mapMinusOneToNull(model.getMigrationDowntime()));
        }
        if (model.isSetSerialNumber()) {
            SerialNumberMapper.copySerialNumber(model.getSerialNumber(), staticVm);
        }

        if (model.isSetCpuProfile() && model.getCpuProfile().isSetId()) {
            staticVm.setCpuProfileId(GuidUtils.asGuid(model.getCpuProfile().getId()));
        }

        return staticVm;
    }

    @Mapping(from = VmTemplate.class, to = Template.class)
    public static Template map(VmTemplate entity, Template incoming) {
        Template model = incoming != null ? incoming : new Template();
        model.setId(entity.getId().toString());
        model.setName(entity.getName());
        model.setDescription(entity.getDescription());
        model.setComment(entity.getComment());
        model.setMemory((long) entity.getMemSizeMb() * BYTES_PER_MB);
        model.setHighAvailability(new HighAvailability());
        model.getHighAvailability().setEnabled(entity.isAutoStartup());
        model.getHighAvailability().setPriority(entity.getPriority());
        model.setStateless(entity.isStateless());
        model.setDeleteProtected(entity.isDeleteProtected());
        model.setSso(SsoMapper.map(entity.getSsoMethod(), null));
        if (entity.getVmType() != null) {
            model.setType(VmMapper.map(entity.getVmType(), null));
        }
        if (entity.getOrigin() != null) {
            model.setOrigin(VmMapper.map(entity.getOrigin(), null));
        }
        if (entity.getStatus() != null) {
            model.setStatus(StatusUtils.create(map(entity.getStatus(), null)));
        }
        if (entity.getDefaultBootSequence() != null ||
            entity.getKernelUrl() != null ||
            entity.getInitrdUrl() != null ||
            entity.getKernelParams() != null) {
            OperatingSystem os = new OperatingSystem();

            os.setType(SimpleDependecyInjector.getInstance().get(OsRepository.class).getUniqueOsNames().get(entity.getOsId()));

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
        model.setBios(new Bios());
        model.getBios().setBootMenu(new BootMenu());
        model.getBios().getBootMenu().setEnabled(entity.isBootMenuEnabled());
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
        model.setCpuShares(entity.getCpuShares());
        if (entity.getDefaultDisplayType() != null) {
            model.setDisplay(new Display());
            model.getDisplay().setType(VmMapper.map(entity.getDefaultDisplayType(), null));
            model.getDisplay().setMonitors(entity.getNumOfMonitors());
            model.getDisplay().setSingleQxlPci(entity.getSingleQxlPci());
            model.getDisplay().setAllowOverride(entity.isAllowConsoleReconnect());
            model.getDisplay().setSmartcardEnabled(entity.isSmartcardEnabled());
            model.getDisplay().setKeyboardLayout(entity.getVncKeyboardLayout());
            model.getDisplay().setFileTransferEnabled(entity.isSpiceFileTransferEnabled());
            model.getDisplay().setCopyPasteEnabled(entity.isSpiceCopyPasteEnabled());
        }
        if (entity.getClusterArch() != null) {
            model.getCpu().setArchitecture(CPUMapper.map(entity.getClusterArch(), null));
        }
        if (entity.getCreationDate() != null) {
            model.setCreationTime(DateMapper.map(entity.getCreationDate(), null));
        }
        if (entity.getVmInit() != null && StringUtils.isNotBlank(entity.getVmInit().getDomain())) {
            Domain domain = new Domain();
            domain.setName(entity.getVmInit().getDomain());
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
        model.setTimezone(entity.getTimeZone());
        model.setTunnelMigration(entity.getTunnelMigration());
        model.setMigrationDowntime(mapNullToMinusOne(entity.getMigrationDowntime()));
        // if this is not a base template, that means this is a template version
        // so need to populate template version properties
        if (!entity.isBaseTemplate()) {
            TemplateVersion version = new TemplateVersion();
            version.setVersionName(entity.getTemplateVersionName());
            version.setVersionNumber(entity.getTemplateVersionNumber());
            Template baseTemplate = new Template();
            baseTemplate.setId(entity.getBaseTemplateId().toString());
            version.setBaseTemplate(baseTemplate);
            model.setVersion(version);
        }

        if (entity.getSerialNumberPolicy() != null) {
            model.setSerialNumber(SerialNumberMapper.map(entity, null));
        }

        if (entity.getCpuProfileId() != null) {
            CpuProfile cpuProfile = new CpuProfile();
            cpuProfile.setId(entity.getCpuProfileId().toString());
            model.setCpuProfile(cpuProfile);
        }

        return model;
    }

    @Mapping(from = Template.class, to = UpdateVmTemplateParameters.class)
    public static UpdateVmTemplateParameters map(Template template, UpdateVmTemplateParameters paramsTemplate) {
        UpdateVmTemplateParameters params = paramsTemplate != null ? paramsTemplate : new UpdateVmTemplateParameters();
        if (template.isSetConsole() && template.getConsole().isSetEnabled()) {
            params.setConsoleEnabled(template.getConsole().isEnabled());
        }
        return params;
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
