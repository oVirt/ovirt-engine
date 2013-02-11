package org.ovirt.engine.core.common.businessentities;

import javax.validation.constraints.Size;
import javax.validation.groups.Default;

import org.ovirt.engine.core.common.businessentities.OvfExportOnlyField.ExportOption;
import org.ovirt.engine.core.common.validation.annotation.ValidI18NName;
import org.ovirt.engine.core.common.validation.group.CreateEntity;
import org.ovirt.engine.core.common.validation.group.ImportClonedEntity;
import org.ovirt.engine.core.common.validation.group.UpdateEntity;
import org.ovirt.engine.core.compat.Guid;

public class VmStatic extends VmBase {
    private static final long serialVersionUID = -2753306386502558044L;

    private Guid vmtGuid = new Guid();

    private boolean initialized;

    @EditableOnVmStatusField
    @OvfExportOnlyField(exportOption = ExportOption.EXPORT_NON_IGNORED_VALUES)
    @Size(max = BusinessEntitiesDefinitions.GENERAL_MAX_SIZE)
    private String userDefinedProperties;

    @EditableOnVmStatusField
    @OvfExportOnlyField(exportOption = ExportOption.EXPORT_NON_IGNORED_VALUES)
    @Size(max = BusinessEntitiesDefinitions.GENERAL_MAX_SIZE)
    private String predefinedProperties;

    /**
     * Disk size in sectors of 512 bytes
     */
    private int diskSize;

    @EditableField
    private int minAllocatedMem;

    @EditableOnVmStatusField
    private String customProperties;

    @EditableField
    @OvfExportOnlyField(exportOption = ExportOption.EXPORT_NON_IGNORED_VALUES)
    @Size(max = BusinessEntitiesDefinitions.GENERAL_MAX_SIZE)
    private String cpuPinning;

    @EditableField
    private boolean useHostCpuFlags = false;

    public VmStatic() {
        setNumOfMonitors(1);
        initialized = false;
        setAutoSuspend(false);
        setNiceLevel(0);
        setDefaultBootSequence(BootSequence.C);
        defaultDisplayType = DisplayType.qxl;
        setVmType(VmType.Desktop);
    }

    public VmStatic(VmStatic vmStatic) {
        super(vmStatic.getId(),
                vmStatic.getVdsGroupId(),
                vmStatic.getOs(),
                vmStatic.getCreationDate(),
                vmStatic.getDescription(),
                vmStatic.getMemSizeMb(),
                vmStatic.getNumOfSockets(),
                vmStatic.getCpuPerSocket(),
                vmStatic.getNumOfMonitors(),
                vmStatic.getDomain(),
                vmStatic.getTimeZone(),
                vmStatic.getVmType(),
                vmStatic.getUsbPolicy(),
                vmStatic.isFailBack(),
                vmStatic.getDefaultBootSequence(),
                vmStatic.getNiceLevel(),
                vmStatic.isAutoSuspend(),
                vmStatic.getPriority(),
                vmStatic.isAutoStartup(),
                vmStatic.isStateless(),
                vmStatic.getIsoPath(),
                vmStatic.getOrigin(),
                vmStatic.getKernelUrl(),
                vmStatic.getKernelParams(),
                vmStatic.getInitrdUrl(),
                vmStatic.getQuotaId(),
                vmStatic.isSmartcardEnabled(),
                vmStatic.isDeleteProtected(),
                vmStatic.getTunnelMigration(),
                vmStatic.getVncKeyboardLayout());
        setName(vmStatic.getName());
        vmtGuid = vmStatic.getVmtGuid();
        setCustomProperties(vmStatic.getCustomProperties());
        setNumOfMonitors(vmStatic.getNumOfMonitors());
        setInitialized(vmStatic.isInitialized());
        setDefaultDisplayType(vmStatic.getDefaultDisplayType());
        setDedicatedVmForVds(vmStatic.getDedicatedVmForVds());
        setMigrationSupport(vmStatic.getMigrationSupport());
        setMinAllocatedMem(vmStatic.getMinAllocatedMem());
    }

    public String getCustomProperties() {
        return customProperties;
    }

    public void setCustomProperties(String customProperties) {
        this.customProperties = customProperties;
    }

    public String getPredefinedProperties() {
        return predefinedProperties;
    }

    public void setPredefinedProperties(String predefinedProperties) {
        this.predefinedProperties = predefinedProperties;
    }

    public String getUserDefinedProperties() {
        return userDefinedProperties;
    }

    public void setUserDefinedProperties(String userDefinedProperties) {
        this.userDefinedProperties = userDefinedProperties;
    }

    public int getDiskSize() {
        return diskSize;
    }

    public void setDiskSize(int value) {
        diskSize = value;
    }

    public boolean isFirstRun() {
        return !isInitialized();
    }

    @Size(min = 1, max = BusinessEntitiesDefinitions.VM_NAME_SIZE, groups = { Default.class, ImportClonedEntity.class })
    @ValidI18NName(message = "ACTION_TYPE_FAILED_NAME_MAY_NOT_CONTAIN_SPECIAL_CHARS",
            groups = { CreateEntity.class, UpdateEntity.class, ImportClonedEntity.class })
    public String getName() {
        return super.getName();
    }

    public Guid getVmtGuid() {
        return this.vmtGuid;
    }

    public void setVmtGuid(Guid value) {
        this.vmtGuid = value;
    }

    public boolean isInitialized() {
        return initialized;
    }

    public void setInitialized(boolean value) {
        initialized = value;
    }

    public int getMinAllocatedMem() {
        if (minAllocatedMem > 0) {
            return minAllocatedMem;
        }
        return getMemSizeMb();
    }

    public void setMinAllocatedMem(int value) {
        minAllocatedMem = value;
    }

    @Override
    public Object getQueryableId() {
        return getId();
    }

    public String getCpuPinning() {
        return cpuPinning;
    }

    public void setCpuPinning(String cpuPinning) {
        this.cpuPinning = cpuPinning;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((defaultDisplayType == null) ? 0 : defaultDisplayType.hashCode());
        result = prime * result + (initialized ? 1231 : 1237);
        result = prime * result + diskSize;
        result = prime * result + ((getName() == null) ? 0 : getName().hashCode());
        result = prime * result + ((predefinedProperties == null) ? 0 : predefinedProperties.hashCode());
        result = prime * result + ((userDefinedProperties == null) ? 0 : userDefinedProperties.hashCode());
        result = prime * result + ((vmtGuid == null) ? 0 : vmtGuid.hashCode());
        result = prime * result + (useHostCpuFlags ? 0 : 1);
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!super.equals(obj)) {
            return false;
        }
        if (!(obj instanceof VmStatic)) {
            return false;
        }
        VmStatic other = (VmStatic) obj;
        if (defaultDisplayType != other.defaultDisplayType) {
            return false;
        }
        if (initialized != other.initialized) {
            return false;
        }
        if (diskSize != other.diskSize) {
            return false;
        }
        if (getName() == null) {
            if (other.getName() != null) {
                return false;
            }
        } else if (!getName().equals(other.getName())) {
            return false;
        }
        if (predefinedProperties == null) {
            if (other.predefinedProperties != null) {
                return false;
            }
        } else if (!predefinedProperties.equals(other.predefinedProperties)) {
            return false;
        }
        if (userDefinedProperties == null) {
            if (other.userDefinedProperties != null) {
                return false;
            }
        } else if (!userDefinedProperties.equals(other.userDefinedProperties)) {
            return false;
        }
        if (vmtGuid == null) {
            if (other.vmtGuid != null) {
                return false;
            }
        } else if (!vmtGuid.equals(other.vmtGuid)) {
            return false;
        }
        if(useHostCpuFlags != other.useHostCpuFlags) {
            return false;
        }
        return true;
    }

    public boolean isUseHostCpuFlags() {
        return useHostCpuFlags;
    }

    public void setUseHostCpuFlags(boolean useHostCpuFlags) {
        this.useHostCpuFlags = useHostCpuFlags;
    }

}
