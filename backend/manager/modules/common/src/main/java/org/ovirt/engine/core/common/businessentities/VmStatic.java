package org.ovirt.engine.core.common.businessentities;

import java.util.Objects;

import javax.validation.constraints.Size;
import javax.validation.groups.Default;

import org.ovirt.engine.core.common.validation.annotation.ValidI18NName;
import org.ovirt.engine.core.common.validation.group.CreateVm;
import org.ovirt.engine.core.common.validation.group.ImportClonedEntity;
import org.ovirt.engine.core.common.validation.group.ImportEntity;
import org.ovirt.engine.core.common.validation.group.UpdateVm;
import org.ovirt.engine.core.compat.Guid;

public class VmStatic extends VmBase {
    private static final long serialVersionUID = -2753306386502558044L;

    private Guid vmtGuid;

    private boolean initialized;

    private String originalTemplateName;

    private Guid originalTemplateGuid;

    @EditableVmField
    private Guid instanceTypeId;
    private Guid imageTypeId;

    @EditableVmField
    private boolean useLatestVersion;

    @EditableVmField(onHostedEngine = true)
    private Guid providerId;

    private String namespace;

    public VmStatic() {
        setNumOfMonitors(1);
        initialized = false;
        setNiceLevel(0);
        setCpuShares(0);
        setDefaultBootSequence(BootSequence.C);
        setDefaultDisplayType(DisplayType.qxl);
        setVmType(VmType.Server);
        vmtGuid = Guid.Empty;
    }

    public VmStatic(VmStatic vmStatic) {
        this((VmBase)vmStatic);
        vmtGuid = vmStatic.getVmtGuid();
        originalTemplateGuid = vmStatic.originalTemplateGuid;
        originalTemplateName = vmStatic.originalTemplateName;
        setInitialized(vmStatic.isInitialized());
        setUseLatestVersion(vmStatic.isUseLatestVersion());
        setInstanceTypeId(vmStatic.getInstanceTypeId());
        namespace = vmStatic.namespace;
    }

    public VmStatic(VmBase vmBase) {
        super(vmBase);
    }

    public boolean isFirstRun() {
        return !isInitialized();
    }

    @Override
    @Size(min = 1, max = BusinessEntitiesDefinitions.VM_NAME_SIZE,
            groups = { Default.class, ImportEntity.class, ImportClonedEntity.class })
    @ValidI18NName(message = "ACTION_TYPE_FAILED_NAME_MAY_NOT_CONTAIN_SPECIAL_CHARS",
            groups = { CreateVm.class, UpdateVm.class, ImportEntity.class, ImportClonedEntity.class })
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

    @Override
    public Object getQueryableId() {
        return getId();
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                super.hashCode(),
                initialized,
                getName(),
                vmtGuid,
                instanceTypeId,
                imageTypeId,
                originalTemplateGuid,
                originalTemplateName,
                useLatestVersion,
                providerId,
                namespace
        );
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof VmStatic)) {
            return false;
        }
        VmStatic other = (VmStatic) obj;
        return super.equals(obj)
                && initialized == other.initialized
                && Objects.equals(getName(), other.getName())
                && Objects.equals(vmtGuid, other.vmtGuid)
                && Objects.equals(instanceTypeId, other.instanceTypeId)
                && Objects.equals(imageTypeId, other.imageTypeId)
                && Objects.equals(originalTemplateGuid, other.originalTemplateGuid)
                && Objects.equals(originalTemplateName, other.originalTemplateName)
                && useLatestVersion == other.useLatestVersion
                && Objects.equals(providerId, other.providerId)
                && Objects.equals(namespace, other.namespace);
    }

    @Override
    public int getMinAllocatedMem() {
        if (super.getMinAllocatedMem() > 0) {
            return super.getMinAllocatedMem();
        }
        return getMemSizeMb();
    }

    public Guid getInstanceTypeId() {
        return instanceTypeId;
    }

    public void setInstanceTypeId(Guid instanceTypeId) {
        this.instanceTypeId = instanceTypeId;
    }

    public Guid getImageTypeId() {
        return imageTypeId;
    }

    public void setImageTypeId(Guid imageTypeId) {
        this.imageTypeId = imageTypeId;
    }

    public String getOriginalTemplateName() {
        return originalTemplateName;
    }

    public void setOriginalTemplateName(String originalTemplateName) {
        this.originalTemplateName = originalTemplateName;
    }

    public Guid getOriginalTemplateGuid() {
        return originalTemplateGuid;
    }

    public void setOriginalTemplateGuid(Guid originalTemplateGuid) {
        this.originalTemplateGuid = originalTemplateGuid;
    }

    public boolean isUseLatestVersion() {
        return useLatestVersion;
    }

    public void setUseLatestVersion(boolean useLatestVersion) {
        this.useLatestVersion = useLatestVersion;
    }

    public Guid getProviderId() {
        return providerId;
    }

    public void setProviderId(Guid providerId) {
        this.providerId = providerId;
    }

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    @Override
    public boolean isManaged() {
        return getOrigin() != OriginType.KUBEVIRT;
    }
}
